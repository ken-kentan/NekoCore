package jp.kentan.minecraft.nekocore.manager

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.SpawnRepository
import jp.kentan.minecraft.nekocore.util.sendSuccessMessage
import jp.kentan.minecraft.nekocore.util.sendWarnMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.Effect
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class SpawnManager(
    private val plugin: NekoCorePlugin
) {

    companion object {
        const val DEFAULT_SPAWN_NAME = "default"
        const val TUTORIAL_SPAWN_NAME = "tutorial"
    }

    private val spawnRepo = SpawnRepository(plugin)

    private val spawnMap: MutableMap<String, Location> = ConcurrentHashMap()
    private val spawnTaskIdMap: MutableMap<Player, Int> = ConcurrentHashMap()

    val spawnNameList: List<String>
        get() = spawnMap.keys.toList()

    init {
        GlobalScope.launch {
            spawnMap.putAll(spawnRepo.getSpawnMap())
        }

        plugin.bukkitEventListener.subscribeSpawnCancel(::onSpawnCanceled)
        plugin.configuration.subscribe(::onConfigReloaded)
    }

    fun spawn(player: Player, name: String) {
        player.teleport(spawnMap[name] ?: return)
    }

    fun spawnDelay(player: Player, name: String?) {
        val location = spawnMap[name ?: DEFAULT_SPAWN_NAME] ?: let {
            player.sendWarnMessage("そのようなスポーンは存在しません.")
            return
        }

        player.apply {
            sendMessage("${NekoCorePlugin.PREFIX}3秒後にスポーンします.")
            world.playEffect(this.location, Effect.MOBSPAWNER_FLAMES, 6)
            playSound(this.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f)
        }

        val prevTaskId = spawnTaskIdMap[player]
        if (prevTaskId != null) {
            Bukkit.getScheduler().cancelTask(prevTaskId)
        }

        val taskId = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            spawnTaskIdMap.remove(player)

            if (player.isOnline) {
                player.teleport(location)
            }
        }, 60L)

        spawnTaskIdMap[player] = taskId
    }

    fun setSpawn(player: Player, name: String) {
        GlobalScope.launch {
            val location = player.location

            if (spawnRepo.addSpawn(name, location)) {
                spawnMap[name] = location

                player.sendSuccessMessage("スポーン($name)をここに設定しました.")
            } else {
                player.sendWarnMessage("スポーンの保存に失敗しました.")
            }
        }
    }

    private fun onSpawnCanceled(player: Player) {
        val taskId = spawnTaskIdMap.remove(player) ?: return

        Bukkit.getScheduler().cancelTask(taskId)
        player.sendMessage("${NekoCorePlugin.PREFIX}スポーンをキャンセルしました.")
    }

    private fun onConfigReloaded() {
        GlobalScope.launch {
            spawnMap.apply {
                val newSpawnMap = spawnRepo.getSpawnMap()
                clear()
                putAll(newSpawnMap)
            }
        }
    }
}
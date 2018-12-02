package jp.kentan.minecraft.nekocore.manager

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.config.ConfigKeys
import jp.kentan.minecraft.nekocore.util.broadcastMessageWithoutMe
import jp.kentan.minecraft.nekocore.util.sendErrorMessage
import jp.kentan.minecraft.nekocore.util.sendWarnMessage
import me.lucko.luckperms.LuckPerms
import me.lucko.luckperms.api.User
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class TutorialManager(
    private val plugin: NekoCorePlugin
) {

    companion object {
        private const val DEFAULT_GROUP_NAME = "default"
        private const val CITIZEN_GROUP_NAME = "citizen"

        private const val GUEST_LOGIN_MESSAGE = "が§6ゲスト§rとしてログインしたにゃ(ﾉ*･ω･)ﾉ*"
        private const val WELCOME_MESSAGE = "§b§lできたてサーバー§a§l（猫）§rへ§6ようこそ！"
        private const val COMPLETE_MESSAGE = "が§9チュートリアル§rを§6完了§rしました！"
        private const val ERROR_MESSAGE = "チュートリアル処理に失敗しました. 運営に報告して下さい."
    }

    private val luckPermsApi = LuckPerms.getApi()

    private val guestGroupNode = luckPermsApi.nodeFactory.makeGroupNode(DEFAULT_GROUP_NAME).build()
    private val citizenGroupNode = luckPermsApi.nodeFactory.makeGroupNode(CITIZEN_GROUP_NAME).build()

    init {
        plugin.bukkitEventListener.subscribePlayerJoin(::onPlayerJoined)
    }

    fun isGuest(player: Player): Boolean {
        val user: User? = luckPermsApi.getUser(player.uniqueId)
        return user != null && user.primaryGroup == DEFAULT_GROUP_NAME
    }

    fun spawn(player: Player) {
        plugin.spawnManager.spawnDelay(player, SpawnManager.TUTORIAL_SPAWN_NAME)
    }

    fun finish(player: Player, keyword: String) {
        if (plugin.configuration.get(ConfigKeys.TUTORIAL_KEYWORD) != keyword) {
            player.sendWarnMessage("キーワードが間違っています. ホームページ( https://minecraft.kentan.jp/rule/ )のルールを確認してください.")
            return
        }

        val user: User? = luckPermsApi.getUser(player.uniqueId)
        if (user == null) {
            plugin.logger.warning("failed to get LuckPermsUser(${player.name}).")
            player.sendErrorMessage(ERROR_MESSAGE)
            return
        }

        val result = user.run {
            unsetPermission(guestGroupNode)
            setPermission(citizenGroupNode)

            return@run setPrimaryGroup(CITIZEN_GROUP_NAME)
        }

        if (result.wasFailure()) {
            plugin.logger.warning("failed to set PrimaryGroup of ${player.name}. (result: $result)")
            player.sendErrorMessage(ERROR_MESSAGE)
            return
        }

        luckPermsApi.userManager.saveUser(user)

        plugin.spawnManager.spawn(player, SpawnManager.DEFAULT_SPAWN_NAME)

        player.playSound(player.location, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f)

        player.sendMessage("${NekoCorePlugin.PREFIX}$WELCOME_MESSAGE")
        player.broadcastMessageWithoutMe("${NekoCorePlugin.PREFIX}${player.displayName}$COMPLETE_MESSAGE")

        player.giveWelcomeItems()
    }

    private fun onPlayerJoined(player: Player) {
        if (isGuest(player)) {
            player.broadcastMessageWithoutMe("${NekoCorePlugin.PREFIX}${player.displayName}$GUEST_LOGIN_MESSAGE")

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
                plugin.spawnManager.spawn(player, SpawnManager.TUTORIAL_SPAWN_NAME)
            }, 10L)
        }
    }

    private fun Player.giveWelcomeItems() {
        inventory.addItem(
            ItemStack(Material.IRON_HELMET),
            ItemStack(Material.IRON_CHESTPLATE),
            ItemStack(Material.IRON_LEGGINGS),
            ItemStack(Material.IRON_BOOTS),
            ItemStack(Material.IRON_AXE),
            ItemStack(Material.IRON_SWORD),
            ItemStack(Material.BAKED_POTATO, 64)
        )
    }
}
package jp.kentan.minecraft.nekocore.listener

import jp.kentan.minecraft.nekocore.data.model.Area
import jp.kentan.minecraft.nekocore.event.ZoneEvent
import jp.kentan.minecraft.nekocore.util.Permissions
import org.bukkit.GameMode
import org.bukkit.block.CreatureSpawner
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.*
import java.util.*

class BukkitEventListener : Listener {

    private var spawnCancelHandler: ((Player) -> Unit)? = null
    private var asyncPlayerChatEventHandler: ((AsyncPlayerChatEvent) -> Unit)? = null
    private var playerPreLoginHandler: ((UUID) -> Unit)? = null
    private val playerJoinHandlerList = mutableListOf<(Player) -> Unit>()
    private val playerQuitHandlerList = mutableListOf<(Player) -> Unit>()

    var zoneEvent: ZoneEvent? = null
        set(value) {
            if (value != null) {
                playerJoinHandlerList.add(value::onPlayerJoin)
            }
            field = value
        }

    fun subscribeSpawnCancel(handler: (Player) -> Unit) {
        spawnCancelHandler = handler
    }

    fun subscribeAsyncPlayerChatEvent(handler: (AsyncPlayerChatEvent) -> Unit) {
        asyncPlayerChatEventHandler = handler
    }

    fun subscribePlayerPreLogin(handler: (UUID) -> Unit) {
        playerPreLoginHandler = handler
    }

    fun subscribePlayerJoin(handler: (Player) -> Unit) {
        playerJoinHandlerList.add(handler)
    }

    fun subscribePlayerQuit(handler: (Player) -> Unit) {
        playerQuitHandlerList.add(handler)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        asyncPlayerChatEventHandler?.invoke(event)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onPlayerPreLogin(event: AsyncPlayerPreLoginEvent) {
        playerPreLoginHandler?.invoke(event.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        playerJoinHandlerList.forEach { it.invoke(event.player) }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playerQuitHandlerList.forEach { it.invoke(event.player) }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerMove(event: PlayerMoveEvent) {
        val to = event.to
        val from = event.from

        if (to == null || to.blockX != from.blockX || to.blockY != from.blockY || to.blockZ != from.blockZ) {
            spawnCancelHandler?.invoke(event.player)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        spawnCancelHandler?.invoke(event.player)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity is Player) {
            spawnCancelHandler?.invoke(entity)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onSignChanged(event: SignChangeEvent) {
        if (event.getLine(0) == "z" && event.player.hasPermission(Permissions.MODERATOR)) {
            zoneEvent?.onSignPlace(event)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val blockState = event.block.state

        if (blockState is Sign && event.player.hasPermission(Permissions.MODERATOR) && blockState.isZoneSign()) {
            zoneEvent?.onSignBreak(event.player, blockState)
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        when (val blockState = event.clickedBlock?.state) {
            is Sign -> {
                if (blockState.isZoneSign()) {
                    if (event.action == Action.RIGHT_CLICK_BLOCK) {
                        zoneEvent?.onSignClick(event.player, blockState)
                    } else if (!event.player.hasPermission(Permissions.MODERATOR)) {
                        event.isCancelled = true
                    }
                }
            }
            is CreatureSpawner -> {
                if (event.player.gameMode != GameMode.CREATIVE && event.material.name.endsWith("_SPAWN_EGG")) {
                    event.isCancelled = true
                }
            }
        }
    }

    private fun Sign.isZoneSign() = getLine(0) == Area.SIGN_INDEX_TEXT
}
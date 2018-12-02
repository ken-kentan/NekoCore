package jp.kentan.minecraft.nekocore.manager

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import me.lucko.luckperms.LuckPerms
import me.lucko.luckperms.api.event.EventHandler
import me.lucko.luckperms.api.event.user.UserDataRecalculateEvent
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Team

class RankManager(
    private val plugin: NekoCorePlugin
) {

    private val luckPermsApi = LuckPerms.getApi()

    private val goldRankGroupNode = luckPermsApi.nodeFactory.makeGroupNode("gold_rank").build()
    private val diamondRankGroupNode = luckPermsApi.nodeFactory.makeGroupNode("diamond_rank").build()
    private val emeraldRankGroupNode = luckPermsApi.nodeFactory.makeGroupNode("emerald_rank").build()

    private val scoreboard = Bukkit.getScoreboardManager().mainScoreboard

    private val eventHandler: EventHandler<UserDataRecalculateEvent>

    init {
        eventHandler = luckPermsApi.eventBus.subscribe(UserDataRecalculateEvent::class.java, ::onUserDataRecalculated)
        plugin.bukkitEventListener.subscribePlayerJoin(::updateRank)
    }

    private fun updateRank(player: Player) {
        val user = luckPermsApi.getUser(player.uniqueId) ?: return

        val rankColor = when {
            user.hasPermission(emeraldRankGroupNode).asBoolean() -> ChatColor.GREEN
            user.hasPermission(diamondRankGroupNode).asBoolean() -> ChatColor.AQUA
            user.hasPermission(goldRankGroupNode).asBoolean() -> ChatColor.GOLD
            else -> ChatColor.RESET
        }

        if (rankColor != ChatColor.RESET) {
            player.displayName = "$rankColor${player.name}§r"
        }

        var team: Team? = scoreboard.getTeam(player.name)

        when {
            player.hasPermission("group.staff") -> {
                if (team == null) {
                    team = createNewTeam(player.name)
                }

                val prefix = ChatColor.translateAlternateColorCodes('&', plugin.chat.getPlayerPrefix(player))
                team?.prefix = "${prefix.dropLast(2)}$rankColor"
            }
            rankColor != ChatColor.RESET -> {
                if (team == null) {
                    team = createNewTeam(player.name)
                }

                team?.prefix = rankColor.toString()
            }
            else -> team?.unregister()
        }
    }

    private fun createNewTeam(playerName: String) =
        scoreboard.registerNewTeam(playerName).apply {
            addEntry(playerName)
        }

    private fun onUserDataRecalculated(event: UserDataRecalculateEvent) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) {
            updateRank(Bukkit.getPlayer(event.user.uuid) ?: return@scheduleSyncDelayedTask)
        }
    }

    fun unregisterLuckPermsEventHandler() = eventHandler.unregister()
}
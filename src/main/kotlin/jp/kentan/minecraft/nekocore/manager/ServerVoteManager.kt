package jp.kentan.minecraft.nekocore.manager

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.config.ConfigKeys
import jp.kentan.minecraft.nekocore.data.PlayerRepository
import jp.kentan.minecraft.nekocore.data.model.VoteReward
import jp.kentan.minecraft.nekocore.util.broadcastMessageWithoutMe
import jp.kentan.minecraft.nekocore.util.sendWarnMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.min

class ServerVoteManager(
    private val plugin: NekoCorePlugin
) {

    companion object {
        private val VOTE_PROMOTE_MESSAGES = arrayOf(
            "§a====================================",
            "投票で§6ゲーム内特典§rをゲットしよう！",
            "§b§nhttps://www.dekitateserver.com/vote/",
            "§a===================================="
        )
    }

    private val playerRepo = PlayerRepository(plugin)

    private val rewardList = mutableListOf<VoteReward>()
    private var voteContinuousLimit: Int = 0

    init {
        plugin.configuration.subscribe(::onConfigReloaded)
        plugin.bukkitEventListener.subscribePlayerJoin(::onPlayerJoined)
        plugin.votifierEventListener.subscribe(::vote)

        rewardList.addAll(plugin.configuration.get(ConfigKeys.VOTE_REWARD_LIST))
        voteContinuousLimit = rewardList.size
    }

    fun vote(username: String) {
        val player: Player? = Bukkit.getOnlinePlayers().find { it.name == username }

        GlobalScope.launch {
            val (playerUniqueId, playerDisplayName) = if (player != null && player.isOnline) {
                player.uniqueId to player.displayName
            } else {
                playerRepo.getUniqueId(username) to username
            }

            if (playerUniqueId == null) {
                plugin.logger.severe("could not resolve Player($username) uuid.")
                return@launch
            }

            val lastVote = playerRepo.getVoteData(playerUniqueId)
            val continuous = lastVote.calcNextContinuous()

            playerRepo.updateLastServerVoteDate(playerUniqueId, continuous)

            val reward = rewardList[continuous - 1]

            if (player != null && player.isOnline) {
                val commandList = reward.commandList.formatCommand(player)

                Bukkit.getServer().apply {
                    scheduler.scheduleSyncDelayedTask(plugin) {
                        commandList.forEach { dispatchCommand(consoleSender, it) }
                    }
                }

                player.sendMessage("${NekoCorePlugin.PREFIX}§6投票ありがとにゃ(｡･ω･｡)")
                player.sendMessage("${NekoCorePlugin.PREFIX}§e特典§r ${reward.name}§r を§dゲット！")
                player.sendMessage("${NekoCorePlugin.PREFIX}§aステータス§7: ${continuous.createRewardStatus()}")
                player.sendMessage("${NekoCorePlugin.PREFIX}§7毎日投票すると特典がアップグレードします！")
            } else {
                playerRepo.addPendingCommandList(playerUniqueId, reward.commandList)
            }

            player.broadcastMessageWithoutMe("${NekoCorePlugin.PREFIX}${playerDisplayName}§7さんが投票で§r ${reward.name}§7 をゲットしました！")
            player.broadcastMessageWithoutMe("${NekoCorePlugin.PREFIX}§3まだ投票をしていませんか？ ↓をクリックしてぜひ投票を！")
            player.broadcastMessageWithoutMe("${NekoCorePlugin.PREFIX}§b§nhttps://www.dekitateserver.com/vote")

            plugin.logger.info("$username voted.")
        }
    }

    fun checkVote(player: Player) {
        GlobalScope.launch {
            val hasVoted = playerRepo.hasVotedOnJapanMinecraftServers(player.uniqueId, player.name)
            if (hasVoted == null) {
                player.sendWarnMessage("JMSの'最近の投票者'に ${player.name} を確認できませんでした.")
                return@launch
            }

            if (!hasVoted) {
                player.sendWarnMessage("すでに特典を受け取っています.")
                return@launch
            }

            vote(player.name)
        }
    }

    private fun onPlayerJoined(player: Player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            if (!player.isOnline || player.isDead) {
                return@Runnable
            }

            if (playerRepo.hasNotVoted(player.uniqueId)) {
                player.sendMessage(VOTE_PROMOTE_MESSAGES)
                return@Runnable
            }

            val commandList = playerRepo.getPendingCommandList(player.uniqueId)
                .formatCommand(player)

            if (commandList.isEmpty()) {
                return@Runnable
            }

            Bukkit.getServer().apply {
                scheduler.scheduleSyncDelayedTask(plugin) {
                    commandList.forEach { dispatchCommand(consoleSender, it) }
                }
            }

            playerRepo.clearPendingCommandList(player.uniqueId)
        }, 20L * 5)
    }

    private fun Pair<Int, Date?>.calcNextContinuous(): Int {
        val (continuous, lastDate) = this

        // 最終投票から2日未満なら
        if (lastDate != null && System.currentTimeMillis() - lastDate.time < 172800000) {
            return min(continuous + 1, voteContinuousLimit)
        }

        return 1
    }

    private fun Int.createRewardStatus(): String {
        val sb = StringBuilder()

        for (day in 1..voteContinuousLimit) {
            sb.append(if (day <= this) "§b${day}日§7[§e★§7] " else "§8${day}日[☆] ")
        }

        return sb.dropLast(1).toString()
    }

    private fun onConfigReloaded() {
        val newRewardList = plugin.configuration.get(ConfigKeys.VOTE_REWARD_LIST)

        rewardList.clear()
        rewardList.addAll(newRewardList)
        voteContinuousLimit = newRewardList.size
    }

    private fun List<String>.formatCommand(player: Player) = map { it.replace("{player}", player.name) }
}
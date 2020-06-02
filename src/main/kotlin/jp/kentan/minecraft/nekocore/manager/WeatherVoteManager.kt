package jp.kentan.minecraft.nekocore.manager

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.model.WeatherState
import jp.kentan.minecraft.nekocore.util.broadcastMessageWithoutMe
import jp.kentan.minecraft.nekocore.util.sendSuccessMessage
import jp.kentan.minecraft.nekocore.util.sendWarnMessage
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.ArrayList

class WeatherVoteManager(
    private val plugin: NekoCorePlugin
) {

    private var voteTask: VoteTask? = null
    private val votedPlayerList = Collections.synchronizedList(ArrayList<Player>())

    fun vote(player: Player) {
        val task = voteTask ?: let {
            player.sendWarnMessage("天候投票は開始されていません.")
            return
        }

        if (votedPlayerList.contains(player)) {
            player.sendWarnMessage("すでに投票しています.")
            return
        }

        votedPlayerList.add(player)
        val voteThreshold = Bukkit.getOnlinePlayers().size / 3.0
        if (votedPlayerList.size >= voteThreshold) {
            val world = task.owner.world

            when (task.weatherState) {
                WeatherState.SUN -> world.apply {
                    setStorm(false)
                    isThundering = false
                }
                WeatherState.RAIN -> world.setStorm(true)
            }

            Bukkit.broadcastMessage("${NekoCorePlugin.PREFIX}投票の結果, 天候を${task.weatherState.displayName}にしました.")
            stopVoteTask()
        } else {
            player.sendSuccessMessage("天候投票に成功しました！")
            sendInfo(player)
        }
    }

    fun startVote(player: Player, state: WeatherState) {
        if (voteTask != null) {
            player.sendWarnMessage("現在, 天候投票中です.")
            return
        }

        if (plugin.economy.withdrawPlayer(player, 100.0).transactionSuccess()) {
            player.sendSuccessMessage("\u00A5100 を支払いました.")
        } else {
            player.sendWarnMessage("所持金が不足しています.")
            return
        }

        val task = VoteTask(player, state)
        task.id = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            if (task.timerSeconds.decrementAndGet() < 0) {
                stopVoteTask()
            }
        }, 20L, 20L).taskId
        voteTask = task

        vote(player)

        player.broadcastMessageWithoutMe("${NekoCorePlugin.PREFIX}${player.displayName}§rが天候投票（${state.displayName}）を開始しました.")
        player.broadcastMessageWithoutMe("${NekoCorePlugin.PREFIX}§7/weathervote で投票に参加します.")
    }

    fun sendInfo(player: Player) {
        val task = voteTask ?: let {
            player.sendWarnMessage("天候投票は開始されていません.")
            return
        }

        player.sendMessage("${NekoCorePlugin.PREFIX}投票主： ${task.owner.displayName}")
        player.sendMessage("${NekoCorePlugin.PREFIX}投票数： §b${votedPlayerList.size}人")
        player.sendMessage("${NekoCorePlugin.PREFIX}残り時間： §a${task.timerSeconds.get()}秒")
        player.sendMessage("${NekoCorePlugin.PREFIX}§7ログインプレイヤーの約1/3が投票すると天候が${task.weatherState.displayName}§7になります。")
    }

    private fun stopVoteTask() {
        voteTask?.apply {
            Bukkit.getScheduler().cancelTask(id)
        }
        voteTask = null

        votedPlayerList.clear()
    }

    private inner class VoteTask(
        val owner: Player,
        val weatherState: WeatherState
    ) {
        var id: Int = 0
        val timerSeconds = AtomicInteger(300)
    }
}
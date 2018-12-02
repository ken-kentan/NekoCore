package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.model.WeatherState
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class WeatherVoteCommand(plugin: NekoCorePlugin) : BaseCommand("weathervote") {

    companion object {
        private val ARGUMENT_LIST = listOf(
            CommandArgument("sun"),
            CommandArgument("rain"),
            CommandArgument("info"),
            CommandArgument("help")
        )
    }

    private val manager = plugin.weatherVoteManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        sender.doIfPlayer { player ->
            if (args.isEmpty()) {
                manager.vote(player)
                return@doIfPlayer
            }

            when (args[0]) {
                "sun" -> manager.startVote(player, WeatherState.SUN)
                "rain" -> manager.startVote(player, WeatherState.RAIN)
                "info" -> manager.sendInfo(player)
                "help" -> sender.sendHelp()
                else -> sender.sendUnknownCommandError()
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        if (args.size == 1) {
            return ARGUMENT_LIST.filter { it.matchFirst(sender, args[0]) }.mapNotNull { it.get(args) }
        }

        return emptyList()
    }

    private fun CommandSender.sendHelp() {
        sendMessage("---------- 天候投票コマンドヘルプ ----------")
        sendMessage("| " + ChatColor.DARK_AQUA + "/weathervote" + ChatColor.RESET + " 投票する.")
        sendMessage("| " + ChatColor.DARK_AQUA + "/weathervote <sun/rain>" + ChatColor.RESET + " 天候投票を開始する.")
        sendMessage("| " + ChatColor.DARK_AQUA + "/weathervote info" + ChatColor.RESET + " 投票状況を表示します.")
        sendMessage("| " + ChatColor.DARK_AQUA + "/weathervote help" + ChatColor.RESET + " ヘルプを表示します.")
        sendMessage("| " + ChatColor.GRAY + "/weathervote は /wvote と省略することも可能です.")
        sendMessage("---------------------------------------")
    }
}
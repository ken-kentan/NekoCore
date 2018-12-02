package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.util.Permissions
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class VoteCommand(plugin: NekoCorePlugin) : BaseCommand("vote") {

    companion object {
        private val ARGUMENT_LIST = listOf(
            CommandArgument("check"),
            CommandArgument("force", CommandArgument.PLAYER, permission = Permissions.ADMIN)
        )
    }

    private val manager = plugin.serverVoteManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage("${NekoCorePlugin.PREFIX}§b§nhttps://minecraft.kentan.jp/vote/")
            sender.sendMessage("${NekoCorePlugin.PREFIX}§7↑のアドレスをクリックして下さい.")
            return true
        }

        when (args[0]) {
            "check" -> sender.doIfPlayer { manager.checkVote(it) }
            "force" -> sender.checkPermission(Permissions.ADMIN) {
                sender.doIfArguments(args, 1) { manager.vote(args[1]) }
            }
            else -> sender.sendUnknownCommandError()
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

        val commandArg = ARGUMENT_LIST.find { it.matchFirst(sender, args[0]) } ?: return emptyList()
        val prefix = args.last()

        return when (commandArg.get(args)) {
            CommandArgument.PLAYER -> getPlayerNameList(prefix)
            else -> emptyList()
        }
    }
}
package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.model.AdvertiseFrequency
import jp.kentan.minecraft.nekocore.util.Permissions
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class AdvertisementCommand(plugin: NekoCorePlugin) : BaseCommand("advertisement") {

    companion object {
        private val ARGUMENT_LIST = listOf(
            CommandArgument("set"),
            CommandArgument("unset"),
            CommandArgument("confirm"),
            CommandArgument("preview"),
            CommandArgument("info"),
            CommandArgument("list"),
            CommandArgument("freq", "[freq]"),
            CommandArgument("help"),
            CommandArgument("sync", permission = Permissions.ADMIN)
        )
    }

    private val manager = plugin.advertisementManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isHelpCommand()) {
            sender.sendHelp()
            return true
        }

        when (args[0]) {
            "set" -> sender.doIfPlayer { player ->
                sender.doIfArguments(args, 2) { manager.addSetAdConfirmTask(player, args[1], args.drop(2)) }
            }
            "unset" -> sender.doIfPlayer { manager.addUnsetAdConfirmTask(it) }
            "confirm" -> sender.doIfPlayer { manager.confirmTask(it) }
            "preview" -> sender.doIfArguments(args, 1) { manager.preview(sender, args.drop(1)) }
            "info" -> sender.doIfPlayer { manager.sendInfo(it) }
            "list" -> manager.sendList(sender)
            "freq" -> sender.doIfPlayer { player ->
                sender.doIfArguments(args, 1) { manager.setAdvertiseFrequency(player, args[1]) }
            }
            "sync" -> sender.checkPermission(Permissions.ADMIN) { manager.sync(sender) }
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
            "[freq]" -> AdvertiseFrequency.values()
                .filter { it.name.startsWith(prefix, ignoreCase = true) }
                .map { it.name.toLowerCase() }
            else -> emptyList()
        }
    }

    private fun CommandSender.sendHelp() {
        sendMessage("---------- 広告コマンドヘルプ ----------")
        sendMessage("| §d/ad set <日数> <内容> §r広告を登録")
        sendMessage("| §d/ad unset §r広告を消去")
        sendMessage("| §d/ad preview <内容> §r広告をプレビュー")
        sendMessage("| §d/ad info §r広告を確認")
        sendMessage("| §d/ad list §r全プレイヤーの広告を確認")
        sendMessage("| §d/ad freq <off|low|middle|high> §r広告の受信頻度を変更")
        sendMessage("| §d/ad help §rヘルプを表示")
        sendMessage("| §7'&'を使用して装飾コードを利用できます.")
        sendMessage("---------------------------------------")
    }
}
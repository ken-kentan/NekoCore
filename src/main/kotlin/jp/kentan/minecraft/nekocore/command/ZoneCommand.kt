package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.util.Permissions
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ZoneCommand(plugin: NekoCorePlugin) : BaseCommand("zone") {

    companion object {
        private val ARGUMENT_LIST = listOf(
            CommandArgument("info", "[area]"),
            CommandArgument("buy", "[area]"),
            CommandArgument("rental", "[area]"),
            CommandArgument("sell", "[area]"),
            CommandArgument("rule", "[area]"),
            CommandArgument("limits"),
            CommandArgument("list"),
            CommandArgument("help"),
            CommandArgument("confirm"),
            CommandArgument("register", permission = Permissions.MODERATOR),
            CommandArgument("remove", "[area]", permission = Permissions.MODERATOR),
            CommandArgument("lock", "[area]", permission = Permissions.MODERATOR),
            CommandArgument("unlock", "[area]", permission = Permissions.MODERATOR),
            CommandArgument("take", "[area]", permission = Permissions.MODERATOR)
        )
    }

    private val manager = plugin.zoneManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isHelpCommand()) {
            sender.sendHelp()
            return true
        }

        val player = sender as Player? ?: return true

        if (sender.hasPermission(Permissions.MODERATOR)) {
            when (args[0]) {
                "register", "rg" -> {
                    sender.doIfArguments(args, 4) {
                        manager.registerArea(player, args[1], args[2], args[3], args[4])
                    }
                    return true
                }
                "remove", "rm" -> {
                    sender.doIfArguments(args, 1) {
                        manager.removeArea(player, args[1])
                    }
                    return true
                }
                "lock" -> {
                    sender.doIfArguments(args, 1) { manager.setAreaLock(player, args[1], true) }
                    return true
                }
                "unlock" -> {
                    sender.doIfArguments(args, 1) { manager.setAreaLock(player, args[1], false) }
                    return true
                }
                "take" -> {
                    sender.doIfArguments(args, 1) { manager.takeArea(player, args[1]) }
                    return true
                }
            }
        }

        when (args[0]) {
            "buy" -> sender.checkPermission("neko.core.zone.buy") {
                sender.doIfArguments(args, 1) {
                    manager.registerBuyTask(player, args[1])
                }
            }
            "rental" -> sender.checkPermission("neko.core.zone.rental") {
                sender.doIfArguments(args, 1) {
                    manager.registerRentalTask(player, args[1])
                }
            }
            "sell" -> sender.checkPermission("neko.core.zone.sell") {
                sender.doIfArguments(args, 1) {
                    manager.registerSellTask(player, args[1])
                }
            }
            "confirm" -> manager.confirmTradeTask(player)
            "info" -> sender.doIfArguments(args, 1) { manager.sendAreaInfo(player, args[1]) }
            "limits" -> manager.sendOwnerLimits(player)
            "list" -> manager.sendOwnerAreaList(player)
            "rule" -> sender.doIfArguments(args, 1) { manager.sendZoneRule(player, args[1]) }
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
            "[area]" -> manager.getAreaNameList((sender as Player?)?.world?.name).filter {
                it.startsWith(
                    prefix,
                    ignoreCase = true
                )
            }
            else -> emptyList()
        }
    }

    private fun CommandSender.sendHelp() {
        sendMessage("---------- 区画コマンドヘルプ ----------")
        sendMessage("| §9/zone info <区画名>   §f区画の情報を表示.")
        sendMessage("| §9/zone buy <区画名>    §f区画を購入.")
        sendMessage("| §9/zone rental <区画名> §f区画を借りる.")
        sendMessage("| §9/zone sell <区画名>   §f区画を売却.")
        sendMessage("| §9/zone rule <区画名>   §f区画規約を表示.")
        sendMessage("| §9/zone limits         §f所有上限を表示.")
        sendMessage("| §9/zone list           §f所有区画一覧を表示.")

        if (hasPermission(Permissions.MODERATOR)) {
            sendMessage("| §9/zone <register|rg> <区画名> <区画親名> <保護ID> <サイズ>  §f登録.")
            sendMessage("| §9/zone <remove|rm> <区画名> §f消去.")
            sendMessage("| §9/zone lock <区画名>        §fロック.")
            sendMessage("| §9/zone unlock <区画名>      §fロック解除.")
            sendMessage("| §9/zone take <区画名>        §f強制所有解除.")
        }

        sendMessage("| §9/zone help           §fヘルプを表示.")
        sendMessage("---------------------------------------")
    }
}
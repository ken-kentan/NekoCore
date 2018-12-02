package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.util.sendErrorMessage
import jp.kentan.minecraft.nekocore.util.sendWarnMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

abstract class BaseCommand(val name: String) : CommandExecutor, TabCompleter {

    protected fun CommandSender.checkPermission(name: String, block: (CommandSender) -> Unit) {
        if (!hasPermission(name)) {
            sendErrorMessage("このコマンドを実行する権限がありません.")
            return
        }

        block(this)
    }

    protected fun CommandSender.doIfPlayer(block: (Player) -> Unit) {
        if (this is Player) {
            block(this)
        } else {
            sendWarnMessage("プレイヤー専用コマンドです")
        }
    }

    fun CommandSender.doIfArguments(args: Array<String>, requireSize: Int, block: (sender: CommandSender) -> Unit) {
        if (args.size - 1 < requireSize) {
            sendWarnMessage("パラメータが不足しています.")
        } else {
            block(this)
        }
    }

    protected fun Array<String>.isHelpCommand() = isEmpty() || get(0) == "help"

    protected fun CommandSender.sendUnknownCommandError() = sendWarnMessage("そのコマンドは存在しません.")

    protected fun getPlayerNameList(filter: String) = Bukkit.getOnlinePlayers()
        .map { it.name }
        .filter { it.startsWith(filter) }
}
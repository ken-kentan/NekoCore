package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class AuthCommand(plugin: NekoCorePlugin) : BaseCommand("auth") {

    private val manager = plugin.antiSpamManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        sender.doIfPlayer { player ->
            if (args.isNotEmpty()) {
                manager.auth(player, args[0])
            }
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender?,
        command: Command?,
        alias: String?,
        args: Array<out String>?
    ): List<String> = emptyList()
}
package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class SpawnCommand(plugin: NekoCorePlugin) : BaseCommand("spawn") {

    private val manager = plugin.spawnManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        sender.doIfPlayer { manager.spawnDelay(it, args.firstOrNull()) }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        if (args.size == 1) {
            return manager.spawnNameList.filter { it.startsWith(args[0], true) }
        }

        return emptyList()
    }
}
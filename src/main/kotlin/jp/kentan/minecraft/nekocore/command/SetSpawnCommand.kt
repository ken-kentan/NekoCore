package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.util.sendWarnMessage
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class SetSpawnCommand(plugin: NekoCorePlugin) : BaseCommand("setspawn") {

    private val manager = plugin.spawnManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        sender.doIfPlayer {
            if (args.isEmpty()) {
                sender.sendWarnMessage("スポーン名を入力してください.")
                return@doIfPlayer
            }

            manager.setSpawn(it, args[0])
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
            return manager.spawnNameList.filter { it.startsWith(args[0], true) }
        }

        return emptyList()
    }
}
package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TutorialCommand(
    plugin: NekoCorePlugin
) : BaseCommand("tutorial") {

    private val manager = plugin.tutorialManager

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        sender.doIfPlayer {
            if (manager.isGuest(it)) {
                if (args.isNotEmpty()) {
                    manager.finish(it, args[0])
                } else {
                    it.sendHelp()
                }
            } else {
                manager.spawn(it)
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

    private fun Player.sendHelp() {
        sendMessage("§6****************************************************")
        sendMessage("§b§lサーバールール§7(§b§nhttps://www.dekitateserver.com/rule/§7)§rを§c確認§rして, §dキーワード§rを入力してください.")
        sendMessage("")
        sendMessage("たとえば§dキーワード§rが§l cat§r の場合は")
        sendMessage("§a /tutorial cat\n")
        sendMessage("とチャットに入力してください.")
        sendMessage("§6****************************************************")
    }
}
package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.util.Permissions
import jp.kentan.minecraft.nekocore.util.sendSuccessMessage
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class NekoCommand(
    private val plugin: NekoCorePlugin
) : BaseCommand("neko") {

    companion object {
        private val ARGUMENT_LIST = listOf(
            CommandArgument("nyan"),
            CommandArgument("hp"),
            CommandArgument("map"),
            CommandArgument("rule"),
            CommandArgument("discord"),
            CommandArgument("twitter"),
            CommandArgument("store"),
            CommandArgument("reload", permission = Permissions.ADMIN)
        )
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isHelpCommand()) {
            sender.sendHelp()
            return true
        }

        when (args[0]) {
            "nyan" -> sender.playNyan()
            "hp" -> sender.sendUrl("https://minecraft.kentan.jp/")
            "map" -> sender.sendUrl("http://minecraft.kentan.jp:8123/")
            "rule" -> sender.sendUrl("https://minecraft.kentan.jp/rule/")
            "discord" -> sender.sendUrl("https://discord.gg/84ABhPK/")
            "twitter" -> sender.sendUrl("https://twitter.com/DekitateServer/")
            "store" -> sender.sendUrl("https://dekitate.buycraft.net/")
            "reload" -> sender.checkPermission(Permissions.ADMIN) {
                plugin.configuration.reload()
                it.sendSuccessMessage("設定を再読込しました.")
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

        return emptyList()
    }

    private fun CommandSender.playNyan() {
        sendMessage(" にゃーんฅ(●´ω｀●)ฅ")
        if (this is Player) {
            playSound(location, Sound.ENTITY_CAT_AMBIENT, 1f, 1f)
        }
    }

    private fun CommandSender.sendUrl(url: String) {
        sendMessage(NekoCorePlugin.PREFIX + ChatColor.AQUA + ChatColor.UNDERLINE + url)
        sendMessage(NekoCorePlugin.PREFIX + ChatColor.GRAY + "↑のアドレスをクリック！")
    }

    private fun CommandSender.sendHelp() {
        sendMessage("---------- NekoCoreコマンドヘルプ ----------")
        sendMessage("| " + ChatColor.GOLD + "/neko nyan" + ChatColor.RESET + " にゃーん.")
        sendMessage("| " + ChatColor.GOLD + "/neko hp" + ChatColor.RESET + " ホームページのURLを表示します.")
        sendMessage("| " + ChatColor.GOLD + "/neko map" + ChatColor.RESET + " WebMapのURLを表示します.")
        sendMessage("| " + ChatColor.GOLD + "/neko rule" + ChatColor.RESET + " サーバールールのURLを表示します.")
        sendMessage("| " + ChatColor.GOLD + "/neko discord" + ChatColor.RESET + " DiscordのURLを表示します.")
        sendMessage("| " + ChatColor.GOLD + "/neko twitter" + ChatColor.RESET + " TwitterのURLを表示します.")
        sendMessage("| " + ChatColor.GOLD + "/neko help" + ChatColor.RESET + " ヘルプを表示します.")
        sendMessage("| " + ChatColor.BLUE + "/zone help" + ChatColor.RESET + " 区画管理のヘルプを表示.")
        sendMessage("| " + ChatColor.LIGHT_PURPLE + "/ad help" + ChatColor.RESET + " 広告のヘルプを表示.")
        sendMessage("| " + ChatColor.DARK_AQUA + "/wvote help" + ChatColor.RESET + " 天候投票のヘルプを表示.")
        sendMessage("| " + ChatColor.GRAY + "/neko は /nk と省略できます.")
        sendMessage("------------------------------------------")
    }
}
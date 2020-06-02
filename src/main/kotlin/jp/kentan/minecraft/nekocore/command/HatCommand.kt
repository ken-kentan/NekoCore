package jp.kentan.minecraft.nekocore.command

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment

class HatCommand(plugin: NekoCorePlugin) : BaseCommand("hat") {

    private val economy = plugin.economy

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        sender.doIfPlayer { player ->
            val inventory = player.inventory

            val headItem = inventory.helmet
            val mainHandItem = inventory.itemInMainHand

            if (mainHandItem.amount < 1) {
                player.sendMessage("§6アイテムをかぶるには、それを手に持つ必要があります.")
                return@doIfPlayer
            }

            if (headItem != null && headItem.getEnchantmentLevel(Enchantment.BINDING_CURSE) > 0) {
                player.sendMessage("§6アイテムに§c束縛の呪い§6がかかっています.")
                return@doIfPlayer
            }

            if (economy.withdrawPlayer(player, 100.0).transactionSuccess()) {
                inventory.helmet = mainHandItem
                inventory.setItemInMainHand(headItem)

                player.sendMessage(" §a\u00A5100 を支払いました.")
            } else {
                player.sendMessage("§6所持金が不足しています.")
            }
        }
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String> =
        emptyList()
}
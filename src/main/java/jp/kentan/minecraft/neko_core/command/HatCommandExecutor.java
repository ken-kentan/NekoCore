package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.bridge.EconomyProvider;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class HatCommandExecutor implements CommandExecutor {

    private final EconomyProvider ECONOMY;

    public HatCommandExecutor(EconomyProvider economyProvider) {
        ECONOMY = economyProvider;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!Util.isPlayer(sender)){
            return true;
        }

        Player player = (Player)sender;

        PlayerInventory inventory = player.getInventory();

        ItemStack headItem = inventory.getHelmet();
        ItemStack mainHandItem = inventory.getItemInMainHand();

        if(mainHandItem == null || mainHandItem.getAmount() <= 0){
            player.sendMessage(MSG_NO_HAND_ITEM);
            return true;
        }

        if(headItem != null && headItem.getEnchantmentLevel(Enchantment.BINDING_CURSE) > 0){
            player.sendMessage(MSG_ITEM_HAS_BINDING_CURSE);
            return true;
        }

        if(ECONOMY.withdraw(player, 100D)) {
            inventory.setHelmet(mainHandItem);
            inventory.setItemInMainHand(headItem);

            player.sendMessage(MSG_WITHDRAW_MONEY);
        }else{
            player.sendMessage(MSG_NOT_ENOUGH_MONEY);
        }

        return true;
    }

    private final static String MSG_NO_HAND_ITEM           = ChatColor.translateAlternateColorCodes('&', "&6アイテムをかぶるには、それを手に持つ必要があります.");
    private final static String MSG_ITEM_HAS_BINDING_CURSE = ChatColor.translateAlternateColorCodes('&', "&6アイテムに&c束縛の呪い&6がかかっています.");
    private final static String MSG_WITHDRAW_MONEY         = ChatColor.translateAlternateColorCodes('&', " &a\u00A5100 を支払いました.");
    private final static String MSG_NOT_ENOUGH_MONEY       = ChatColor.translateAlternateColorCodes('&', "&6所持金が不足しています.");
}

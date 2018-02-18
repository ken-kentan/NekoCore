package jp.kentan.minecraft.neko_core.bridge;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;

public class EconomyProvider {

    private final Economy ECONOMY;

    public EconomyProvider(Economy economy){
        ECONOMY = economy;
    }

    public boolean deposit(Player player, double amount){
        return ECONOMY.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean withdraw(Player player, double amount){
        return ECONOMY.withdrawPlayer(player, amount).transactionSuccess();
    }

    public double getBalance(Player player){
        return ECONOMY.getBalance(player);
    }
}

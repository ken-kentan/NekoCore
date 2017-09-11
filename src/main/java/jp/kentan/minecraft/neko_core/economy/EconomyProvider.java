package jp.kentan.minecraft.neko_core.economy;


import jp.kentan.minecraft.neko_core.utils.Log;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyProvider {

    private static Economy sEconomy;

    public static void setup(){
        detectVault();
    }

    private static void detectVault() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Log.warn("failed to detect Vault.");
            return;
        }

        RegisteredServiceProvider<Economy> serviceProvider = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);

        if (serviceProvider == null) {
            Log.warn("failed to get Economy service.");
            return;
        }
        sEconomy = serviceProvider.getProvider();
    }

    public static boolean deposit(Player player, double amount){
        if(player == null || !sEconomy.hasAccount(player) || amount < 0){
            return false;
        }

        EconomyResponse response = sEconomy.depositPlayer(player, amount);

        return response.transactionSuccess();
    }

    public static boolean withdraw(Player player, double amount){
        if(player == null || !sEconomy.hasAccount(player) || amount < 0){
            return false;
        }

        EconomyResponse response = sEconomy.withdrawPlayer(player, amount);

        return response.transactionSuccess();
    }

    public static double getBalance(Player player){
        if(player == null || !sEconomy.hasAccount(player)){
            return 0;
        }

        return sEconomy.getBalance(player);
    }
}
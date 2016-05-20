package jp.kentan.minecraft.core;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

public class EconomyManager {
	private NekoCore nekoCore;
	private ConfigManager config;
	
	private Economy econ = null;
	private Permission perms = null;
	private Chat chat = null;
	
    EconomyManager(NekoCore _neko, ConfigManager _config){
		nekoCore = _neko;
		config = _config;
		
		if (!setupEconomy()) {
			nekoCore.getLogger().warning("Failed link with Vault.");
            return;
        }
		
        setupPermissions();
        setupChat();
        
        nekoCore.getLogger().warning("Successfully linked with Vault.");
	}
	
	private boolean setupEconomy() {
        if (nekoCore.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = nekoCore.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = nekoCore.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = nekoCore.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    public boolean deposit(String strPlayer, double amount){
    	EconomyResponse r = null;
    	UUID uuid = config.getPlayerUUID(strPlayer);
    	OfflinePlayer player = null;
    	
    	if(uuid != null) player = nekoCore.getServer().getOfflinePlayer(uuid);
    	
    	if(uuid == null || player == null || !econ.hasAccount(player) || amount < 0) return false;
    	
		r = econ.depositPlayer(player, amount);
	
    	if(r.transactionSuccess()) {
    		nekoCore.getLogger().info("deposit " + econ.format(r.amount) + " to " + strPlayer);
    		return true;
    	}else{
    		nekoCore.getLogger().warning(r.errorMessage);
    		return false;
    	}
    }
    
    public boolean withdraw(String strPlayer, double amount){
    	EconomyResponse r = null;
    	UUID uuid = config.getPlayerUUID(strPlayer);
    	OfflinePlayer player = null;
    	
    	if(uuid != null) player = nekoCore.getServer().getOfflinePlayer(uuid);
    	
    	if(uuid == null || player == null || !econ.hasAccount(player) || amount < 0) return false;
    	
		r = econ.withdrawPlayer(player, amount);
    	
    	if(r.transactionSuccess()) {
    		nekoCore.getLogger().info("withdraw " + econ.format(r.amount) + " from " + strPlayer);
    		return true;
    	}else{
    		nekoCore.getLogger().warning(r.errorMessage);
    		return false;
    	}
    }
    
    public int getBalance(String strPlayer){
    	UUID uuid = config.getPlayerUUID(strPlayer);
    	OfflinePlayer player = null;
    	
    	if(uuid != null){
    		player = nekoCore.getServer().getOfflinePlayer(uuid);
    	}else{
    		return -1; //not linked
    	}
    	
    	if(player == null || !econ.hasAccount(player)){
    		return -2; //not has bank account
    	}
    	
    	return (int)econ.getBalance(player);
    }
}

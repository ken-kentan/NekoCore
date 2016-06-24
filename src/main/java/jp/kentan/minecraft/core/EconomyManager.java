package jp.kentan.minecraft.core;

import java.util.UUID;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.permission.Permission;

public class EconomyManager {
	private NekoCore neko = null;
	private ConfigManager config = null;
	
	private Economy econ = null;
	private Permission perms = null;
	private Chat chat = null;
	
    public EconomyManager(NekoCore neko, ConfigManager config){
    	this.neko = neko;
    	this.config = config;
    	
		if (!setupEconomy()) {
			neko.getLogger().warning("Failed link with Vault.");
            return;
        }
		
        setupPermissions();
        setupChat();
        
        neko.getLogger().warning("Successfully linked with Vault.");
	}
	
	private boolean setupEconomy() {
        if (neko.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = neko.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = neko.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = neko.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    
    public boolean deposit(String strPlayer, double amount){
    	EconomyResponse r = null;
    	UUID uuid = config.getPlayerUUID(strPlayer);
    	OfflinePlayer player = null;
    	
    	if(uuid != null) player = neko.getServer().getOfflinePlayer(uuid);
    	
    	if(uuid == null || player == null || !econ.hasAccount(player) || amount < 0) return false;
    	
		r = econ.depositPlayer(player, amount);
	
    	if(r.transactionSuccess()) {
    		neko.getLogger().info("deposit " + econ.format(r.amount) + " to " + strPlayer);
    		return true;
    	}else{
    		neko.getLogger().warning(r.errorMessage);
    		return false;
    	}
    }
    
    public boolean withdraw(String strPlayer, double amount){
    	EconomyResponse r = null;
    	UUID uuid = config.getPlayerUUID(strPlayer);
    	OfflinePlayer player = null;
    	
    	if(uuid != null) player = neko.getServer().getOfflinePlayer(uuid);
    	
    	if(uuid == null || player == null || !econ.hasAccount(player) || amount < 0) return false;
    	
		r = econ.withdrawPlayer(player, amount);
    	
    	if(r.transactionSuccess()) {
    		neko.getLogger().info("withdraw " + econ.format(r.amount) + " from " + strPlayer);
    		return true;
    	}else{
    		neko.getLogger().warning(r.errorMessage);
    		return false;
    	}
    }
    
    public int getBalance(String strPlayer){
    	UUID uuid = config.getPlayerUUID(strPlayer);
    	OfflinePlayer player = null;
    	
    	if(uuid != null){
    		player = neko.getServer().getOfflinePlayer(uuid);
    	}else{
    		return -1; //not linked
    	}
    	
    	if(player == null || !econ.hasAccount(player)){
    		return -2; //not has bank account
    	}
    	
    	return (int)econ.getBalance(player);
    }
}

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
			nekoCore.getLogger().warning("Vaultとの提携に失敗しました.");
            return;
        }
		
        setupPermissions();
        setupChat();
        
        nekoCore.getLogger().warning("Linked to Vault!");
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
    	String strMode = null;
    	
    	if(uuid != null) player = nekoCore.getServer().getOfflinePlayer(uuid);
    	
    	if(uuid == null || player == null || !econ.hasAccount(player)) return false;
    	
    	if(amount >= 0){
    		r = econ.depositPlayer(player, amount);
    		strMode = "Deposit";
    	}else{
    		r = econ.withdrawPlayer(player, Math.abs(amount));
    		strMode = "Withdraw";
    	}
    	
    	if(r.transactionSuccess()) {
    		nekoCore.getLogger().info(strMode + ":" + econ.format(r.amount) + " to " + strPlayer);
    		return true;
    	}else{
    		nekoCore.getLogger().warning(r.errorMessage);
    		return false;
    	}
    }
}

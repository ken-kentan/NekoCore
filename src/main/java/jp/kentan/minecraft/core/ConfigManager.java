package jp.kentan.minecraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ConfigManager {
	private NekoCore nekoCore;
	final private Charset CONFIG_CHAREST = StandardCharsets.UTF_8;
	
	public ConfigManager(NekoCore _neko) {
		nekoCore = _neko;
	}
	
	public void setTwitterBotData(){
		
		String confFilePath= nekoCore.getDataFolder() + File.separator + "config.yml";

		// 設定ファイルを開く
		try(Reader reader = new InputStreamReader(new FileInputStream(confFilePath),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			TwitterBot.consumerKey       = conf.getString("Twitter.consumerKey");
			TwitterBot.consumerSecret    = conf.getString("Twitter.consumerSecret");
			TwitterBot.accessToken       = conf.getString("Twitter.accessToken");
			TwitterBot.accessTokenSecret = conf.getString("Twitter.accessTokenSecret");
			
			//init All List
			TwitterBot.nekoFaceList.clear();
			TwitterBot.msgPlayerActionList.clear();
			TwitterBot.msgUnkownCommandList.clear();
			TwitterBot.msgRejectCommandList.clear();
			TwitterBot.msgThanksList.clear();
			TwitterBot.msgLuckyList.clear();
			TwitterBot.msgMorningList.clear();
			TwitterBot.msgWeatherList.clear();
			TwitterBot.msgNyanList.clear();
			
			TwitterBot.nekoFaceList         = conf.getStringList("Bot.nekoFace");
			TwitterBot.msgPlayerActionList  = conf.getStringList("Bot.msgPlayerAction");
			TwitterBot.msgUnkownCommandList = conf.getStringList("Bot.msgUnknownCommand");
			TwitterBot.msgRejectCommandList = conf.getStringList("Bot.msgRejectCommand");
			TwitterBot.msgThanksList        = conf.getStringList("Bot.msgThanks");
			TwitterBot.msgLuckyList         = conf.getStringList("Bot.msgLucky");
			TwitterBot.msgMorningList       = conf.getStringList("Bot.msgGoodMorning");
			TwitterBot.msgWeatherList       = conf.getStringList("Bot.msgWeather");
			TwitterBot.msgNyanList          = conf.getStringList("Bot.msgNyan");
			
			TwitterBot.gachaSize   = conf.getInt("Gacha.size");
			TwitterBot.gachaCost   = conf.getInt("Gacha.cost");
			TwitterBot.gachaReward = conf.getInt("Gacha.reward");
		}catch(Exception e){
			nekoCore.getLogger().warning(e.toString());
		}
		
		nekoCore.getLogger().info("Twitter設定ファイルを正常に読み込みました。");
	}
	
	public boolean saveLinkedTwitterAccount(Player player, String strTwitterAccount){
		try {
			File configFile = new File(nekoCore.getDataFolder(), "player.yml");
			
			if(configFile != null){
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);
				
				conf.set("Player." + player.getName() + ".TwitterID", strTwitterAccount);
				conf.set("Player." + player.getName() + ".UUID", player.getUniqueId().toString());
				
				conf.save(configFile);
			}
		} catch (Exception e) {
			nekoCore.getLogger().warning(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public boolean isLinkedTwitterAccount(String strMinecraftID, String strTwitterAccount){
		
		String confFilePath= nekoCore.getDataFolder() + File.separator + "player.yml";

		try(Reader reader = new InputStreamReader(new FileInputStream(confFilePath),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			String strLinkedTwitterAccount = conf.getString("Player." + strMinecraftID + ".TwitterID");
			
			if(strLinkedTwitterAccount != null && strTwitterAccount.equals(strLinkedTwitterAccount)){
				return true;
			}
		}catch(Exception e){
			nekoCore.getLogger().warning(e.toString());
		}
		
		return false;
	}
	
	public UUID getPlayerUUID(String strMinecraftID){
		String confFilePath= nekoCore.getDataFolder() + File.separator + "player.yml";

		try(Reader reader = new InputStreamReader(new FileInputStream(confFilePath),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			String uuid = conf.getString("Player." + strMinecraftID + ".UUID");
			
			return UUID.fromString(uuid);
		}catch(Exception e){
			nekoCore.getLogger().warning(e.toString());
		}
		
		return null;
	}
}

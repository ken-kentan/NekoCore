package jp.kentan.minecraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ConfigManager {
	private NekoCore nekoCore;
	final private Charset CONFIG_CHAREST = StandardCharsets.UTF_8;
	
	private String confFilePath = null;
	private String playerFilePath = null;
	private String playerGachaRewardFile = null;
	
	public ConfigManager(NekoCore _neko) {
		nekoCore = _neko;
		
		confFilePath   = nekoCore.getDataFolder() + File.separator + "config.yml";
		playerFilePath = nekoCore.getDataFolder() + File.separator + "player.yml";
		
		playerGachaRewardFile = nekoCore.getDataFolder() + File.separator + "gachaReward.yml";
		
		setTwitterBotData();
	}
	
	public void setTwitterBotData(){
		
		try(Reader reader = new InputStreamReader(new FileInputStream(confFilePath),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			Twitter.consumerKey       = conf.getString("Twitter.consumerKey");
			Twitter.consumerSecret    = conf.getString("Twitter.consumerSecret");
			Twitter.accessToken       = conf.getString("Twitter.accessToken");
			Twitter.accessTokenSecret = conf.getString("Twitter.accessTokenSecret");
			
			//clear All List
			BotManager.nekoFaceList.clear();
			BotManager.msgPlayerActionList.clear();
			BotManager.msgUnkownCommandList.clear();
			BotManager.msgRejectCommandList.clear();
			BotManager.msgThanksList.clear();
			BotManager.msgLuckyList.clear();
			BotManager.msgMorningList.clear();
			BotManager.msgWeatherList.clear();
			BotManager.msgNyanList.clear();
			BotManager.msgGachaMissList.clear();
			BotManager.msgAskYesList.clear();
			BotManager.msgAskNoList.clear();
			
			BotManager.nekoFaceList         = conf.getStringList("Bot.nekoFace");
			BotManager.msgPlayerActionList  = conf.getStringList("Bot.msgPlayerAction");
			BotManager.msgUnkownCommandList = conf.getStringList("Bot.msgUnknownCommand");
			BotManager.msgRejectCommandList = conf.getStringList("Bot.msgRejectCommand");
			BotManager.msgThanksList        = conf.getStringList("Bot.msgThanks");
			BotManager.msgLuckyList         = conf.getStringList("Bot.msgLucky");
			BotManager.msgMorningList       = conf.getStringList("Bot.msgGoodMorning");
			BotManager.msgWeatherList       = conf.getStringList("Bot.msgWeather");
			BotManager.msgNyanList          = conf.getStringList("Bot.msgNyan");
			BotManager.msgGachaMissList     = conf.getStringList("Bot.msgGachaMiss");
			BotManager.msgAskYesList        = conf.getStringList("Bot.msgAskYes");
			BotManager.msgAskNoList         = conf.getStringList("Bot.msgAskNo");
			
			
			GachaManager.sizeMap.clear();
			GachaManager.costMap.clear();
			GachaManager.rewardMap.clear();
			
			GachaManager.sizeMap.put(GachaManager.Type.Money,       conf.getInt("Gacha.typeMoney.size"));
			GachaManager.sizeMap.put(GachaManager.Type.Diamond,     conf.getInt("Gacha.typeDiamond.size"));
			GachaManager.sizeMap.put(GachaManager.Type.EventTicket, conf.getInt("Gacha.typeEventTicket.size"));
			
			GachaManager.costMap.put(GachaManager.Type.Money,       conf.getInt("Gacha.typeMoney.cost"));
			GachaManager.costMap.put(GachaManager.Type.Diamond,     conf.getInt("Gacha.typeDiamond.cost"));
			GachaManager.costMap.put(GachaManager.Type.EventTicket, conf.getInt("Gacha.typeEventTicket.cost"));
			
			GachaManager.rewardMap.put(GachaManager.Type.Money,       conf.getInt("Gacha.typeMoney.reward"));
			GachaManager.rewardMap.put(GachaManager.Type.Diamond,     conf.getInt("Gacha.typeDiamond.reward"));
			GachaManager.rewardMap.put(GachaManager.Type.EventTicket, conf.getInt("Gacha.typeEventTicket.reward"));
			
			
			VoteManager.maxSuccession = conf.getInt("Vote.maxSuccession");
			
			VoteManager.rewardList.clear();
			VoteManager.rewardDetailList.clear();
			
			VoteManager.rewardDetailList = conf.getStringList("Vote.Reward.Detail");
			
			for(int i=1; i <= VoteManager.maxSuccession; ++i){
				VoteManager.rewardList.add(conf.getStringList("Vote.Reward.day" + i));
			}
			
		}catch(Exception e){
			nekoCore.getLogger().warning(e.toString());
			return;
		}
		
		nekoCore.getLogger().info("Successfully read the config.yml file.");
	}
	
	public boolean saveLinkedTwitterAccount(Player player, String strTwitterAccount){
		try {
			File configFile = new File(nekoCore.getDataFolder(), "player.yml");
			
			if(configFile != null){
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);
				
				conf.set("Player." + player.getName() + ".TwitterID", strTwitterAccount);
				conf.set("Player." + player.getName() + ".UUID", player.getUniqueId().toString());
				
				conf.set("Twitter." + strTwitterAccount + ".mcID", player.getName());
				
				conf.save(configFile);
			}
		} catch (Exception e) {
			nekoCore.getLogger().warning(e.getMessage());
			return false;
		}
		
		nekoCore.getLogger().info("Successfully linked " + player.getName() + " <--> @" + strTwitterAccount);
		
		return true;
	}
	
	public boolean isLinkedTwitterAccount(String strMinecraftID, String strTwitterAccount){

		try(Reader reader = new InputStreamReader(new FileInputStream(playerFilePath),CONFIG_CHAREST)){
			
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

		try(Reader reader = new InputStreamReader(new FileInputStream(playerFilePath),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			String uuid = conf.getString("Player." + strMinecraftID + ".UUID");
			
			if(uuid != null){
				return UUID.fromString(uuid);
			}
		}catch(Exception e){
			nekoCore.getLogger().warning(e.toString());
		}
		
		return null;
	}
	
	public String getMinecraftID(String strTwitterAccount){
		
		try(Reader reader = new InputStreamReader(new FileInputStream(playerFilePath),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			String mcID = conf.getString("Twitter." + strTwitterAccount + ".mcID");
			
			if(mcID != null){
				return mcID;
			}
		}catch(Exception e){
			nekoCore.getLogger().warning(e.toString());
		}
		
		return null;
	}
	
	public List<String> getPlayerGachaRewards(String strPlayer){
		
		try(Reader reader = new InputStreamReader(new FileInputStream(playerGachaRewardFile),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			List<String> rewardsList = conf.getStringList(strPlayer + ".gachaRewards");
			
			if(rewardsList != null){
				return rewardsList;
			}
		}catch(Exception e){
			nekoCore.getLogger().warning(e.toString());
		}
		
		return null;
	}
	
	public void addPlayerGachaRewards(String strPlayer, String strCommand){
		
		try {
			File configFile = new File(nekoCore.getDataFolder(), "gachaReward.yml");
			
			if(configFile != null){
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);
				
				List<String> rewardsList = conf.getStringList(strPlayer + ".gachaRewards");
				
				rewardsList.add(strCommand);
				
				conf.set(strPlayer + ".gachaRewards", rewardsList);
				
				conf.save(configFile);
			}
		} catch (Exception e) {
			nekoCore.getLogger().warning(e.getMessage());
		}
		
		return;
	}
	
	public void deletePlayerGachaRewards(String strPlayer){
			
		try {
			File configFile = new File(nekoCore.getDataFolder(), "gachaReward.yml");
			
			if(configFile != null){
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);
				
				conf.set(strPlayer + ".gachaRewards", null);
				
				conf.save(configFile);
			}
		} catch (Exception e) {
			nekoCore.getLogger().warning(e.getMessage());
		}
		
		return;
	}
	
	public Date getLastVotedDate(String strPlayer){
		Date formatDate = null;

		try(Reader reader = new InputStreamReader(new FileInputStream(playerFilePath),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			String strDate = conf.getString("Player." + strPlayer + ".Vote.LastDate");
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	        formatDate = sdf.parse(strDate);
			
		}catch(Exception e){
			return null;
		}
		
		return formatDate;
	}
	
	public boolean saveLastVotedDate(String strPlayer, Date date){
		try {
			File configFile = new File(nekoCore.getDataFolder(), "player.yml");
			
			if(configFile != null){
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				
				conf.set("Player." + strPlayer + ".Vote.LastDate", sdf.format(date));
				
				conf.save(configFile);
			}
		} catch (Exception e) {
			nekoCore.getLogger().warning(e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public int getSuccessionVote(String strPlayer){
		int succession = 1;
		try(Reader reader = new InputStreamReader(new FileInputStream(playerFilePath),CONFIG_CHAREST)){
			
			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);
			
			succession = conf.getInt("Player." + strPlayer + ".Vote.Succession");
			
		}catch(Exception e){
			return 1;
		}
		
		return succession;
	}
	
	public boolean saveSuccessionVote(String strPlayer, int succession){
		try {
			File configFile = new File(nekoCore.getDataFolder(), "player.yml");
			
			if(configFile != null){
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);
				
				conf.set("Player." + strPlayer + ".Vote.Succession", succession);
				
				conf.save(configFile);
			}
		} catch (Exception e) {
			nekoCore.getLogger().warning(e.getMessage());
			return false;
		}
		
		return true;
	}
}

package jp.kentan.minecraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class ConfigManager {
	private NekoCore neko = null;
	private final Charset CONFIG_CHAREST = StandardCharsets.UTF_8;

	private String confFilePath = null;
	private String playerFilePath = null;
	private String playerGachaRewardFile = null;
	
	public ConfigManager(NekoCore neko) {
		this.neko = neko;
		
		confFilePath = neko.getDataFolder() + File.separator + "config.yml";
		playerFilePath = neko.getDataFolder() + File.separator + "player.yml";
		playerGachaRewardFile = neko.getDataFolder() + File.separator + "gachaReward.yml";
	}

	public void load() {

		try (Reader reader = new InputStreamReader(new FileInputStream(confFilePath), CONFIG_CHAREST)) {

			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);

			Twitter.consumerKey = conf.getString("Twitter.consumerKey");
			Twitter.consumerSecret = conf.getString("Twitter.consumerSecret");
			Twitter.accessToken = conf.getString("Twitter.accessToken");
			Twitter.accessTokenSecret = conf.getString("Twitter.accessTokenSecret");
			
			SQLManager.HOST = conf.getString("SQL.host");
			SQLManager.ID = conf.getString("SQL.id");
			SQLManager.PASS = conf.getString("SQL.pass");

			// clear All List
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

			BotManager.nekoFaceList = conf.getStringList("Bot.nekoFace");
			BotManager.msgPlayerActionList = conf.getStringList("Bot.msgPlayerAction");
			BotManager.msgUnkownCommandList = conf.getStringList("Bot.msgUnknownCommand");
			BotManager.msgRejectCommandList = conf.getStringList("Bot.msgRejectCommand");
			BotManager.msgThanksList = conf.getStringList("Bot.msgThanks");
			BotManager.msgLuckyList = conf.getStringList("Bot.msgLucky");
			BotManager.msgMorningList = conf.getStringList("Bot.msgGoodMorning");
			BotManager.msgWeatherList = conf.getStringList("Bot.msgWeather");
			BotManager.msgNyanList = conf.getStringList("Bot.msgNyan");
			BotManager.msgGachaMissList = conf.getStringList("Bot.msgGachaMiss");
			BotManager.msgAskYesList = conf.getStringList("Bot.msgAskYes");
			BotManager.msgAskNoList = conf.getStringList("Bot.msgAskNo");

			GachaManager.sizeMap.clear();
			GachaManager.costMap.clear();
			GachaManager.rewardMap.clear();

			GachaManager.sizeMap.put(GachaManager.Type.Money, conf.getInt("Gacha.typeMoney.size"));
			GachaManager.sizeMap.put(GachaManager.Type.Diamond, conf.getInt("Gacha.typeDiamond.size"));
			GachaManager.sizeMap.put(GachaManager.Type.EventTicket, conf.getInt("Gacha.typeEventTicket.size"));

			GachaManager.costMap.put(GachaManager.Type.Money, conf.getInt("Gacha.typeMoney.cost"));
			GachaManager.costMap.put(GachaManager.Type.Diamond, conf.getInt("Gacha.typeDiamond.cost"));
			GachaManager.costMap.put(GachaManager.Type.EventTicket, conf.getInt("Gacha.typeEventTicket.cost"));

			GachaManager.rewardMap.put(GachaManager.Type.Money, conf.getInt("Gacha.typeMoney.reward"));
			GachaManager.rewardMap.put(GachaManager.Type.Diamond, conf.getInt("Gacha.typeDiamond.reward"));
			GachaManager.rewardMap.put(GachaManager.Type.EventTicket, conf.getInt("Gacha.typeEventTicket.reward"));

			VoteManager.maxSuccession = conf.getInt("Vote.maxSuccession");

			VoteManager.rewardList.clear();
			VoteManager.rewardDetailList.clear();

			VoteManager.rewardDetailList = conf.getStringList("Vote.Reward.Detail");

			for (int i = 1; i <= VoteManager.maxSuccession; ++i) {
				VoteManager.rewardList.add(conf.getStringList("Vote.Reward." + i + "day"));
			}

		} catch (Exception e) {
			neko.getLogger().warning(e.getMessage());
			return;
		}

		neko.getLogger().info("Successfully read the config.yml file.");
	}

	private String readPlayerData(String path) {
		String data = "";

		try (Reader reader = new InputStreamReader(new FileInputStream(playerFilePath), CONFIG_CHAREST)) {

			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);

			data = conf.getString(path);
		} catch (Exception e) {
			return null;
		}
		
		return data;
	}
	
	private int readPlayerData(String path, int def) {
		int data = def;

		try (Reader reader = new InputStreamReader(new FileInputStream(playerFilePath), CONFIG_CHAREST)) {

			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);

			data = conf.getInt(path);
		} catch (Exception e) {
			neko.getLogger().warning(e.getMessage());
		}
		
		return data;
	}
	
	private List<String> readGachaData(String path) {
		List<String> list = null;

		try (Reader reader = new InputStreamReader(new FileInputStream(playerGachaRewardFile), CONFIG_CHAREST)) {

			FileConfiguration conf = new YamlConfiguration();

			conf.load(reader);

			list = conf.getStringList(path);
		} catch (Exception e) {
			neko.getLogger().warning(e.getMessage());
		}

		return list;
	}

	private void savePlayerData(List<String> pathList, List<String> dataList){		
		try {
			File configFile = new File(neko.getDataFolder(), "player.yml");

			if (configFile != null) {
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);

				int index = 0;
				for(String path : pathList){
					conf.set(path, dataList.get(index++));
				}

				conf.save(configFile);
			}
		} catch (Exception e) {
			neko.getLogger().warning(e.getMessage());
		}
	}
	
	private void saveGachaData(String path, String data){
		try {
			File configFile = new File(neko.getDataFolder(), "gachaReward.yml");

			if (configFile != null) {
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);

				List<String> list = conf.getStringList(path);

				if(data != null){
					list.add(data);
				}else{
					list.clear();
				}

				conf.set(path, list);

				conf.save(configFile);
			}
		} catch (Exception e) {
			neko.getLogger().warning(e.getMessage());
		}
	}
	
	public boolean saveLinkedTwitterAccount(Player player, String strTwitterID) {
		List<String> path = new ArrayList<String>();
		List<String> data = new ArrayList<String>();
		
		path.add("Player." + player.getName() + ".TwitterID");
		path.add("Player." + player.getName() + ".UUID");
		path.add("Twitter." + strTwitterID + ".mcID");
		
		data.add(strTwitterID);
		data.add(player.getUniqueId().toString());
		data.add(player.getName());
		
		savePlayerData(path, data);

		neko.getLogger().info("Successfully linked " + player.getName() + " <--> @" + strTwitterID);

		return true;
	}

	public boolean isLinkedTwitter(String strMinecraftID, String strTwitterID) {
		String strLinkedTwitterAccount = readPlayerData("Player." + strMinecraftID + ".TwitterID");
		
		if (strLinkedTwitterAccount != null && strTwitterID.equals(strLinkedTwitterAccount)) {
			return true;
		}

		return false;
	}

	public UUID getPlayerUUID(String strMinecraftID) {
		String strUUID = readPlayerData("Player." + strMinecraftID + ".UUID");
		
		if(strUUID != null){
			return UUID.fromString(strUUID);
		}else{
			return null;
		}
	}
	
	public String getMinecraftID(String strTwitterID) {
		return readPlayerData("Twitter." + strTwitterID + ".mcID");
	}

	public List<String> getPlayerGachaRewards(String strPlayer) {
		return readGachaData(strPlayer + ".gachaRewards");
	}

	public void addPlayerGachaRewards(String strPlayer, String strCommand) {
		saveGachaData(strPlayer + ".gachaRewards", strCommand);
	}

	public void deletePlayerGachaRewards(String strPlayer) {
		saveGachaData(strPlayer + ".gachaRewards", null);
	}
	
	
	/* Vote Data */
	public int getSuccessionVote(String strPlayer) {
		return readPlayerData("Player." + strPlayer + ".Vote.Succession", 1);
	}

	public Date getLastVotedDate(String strPlayer) {
		String strDate = readPlayerData("Player." + strPlayer + ".Vote.LastDate");
		Date formatDate = null;
		
		if(strDate == null){
			neko.getLogger().info("Vote: " + strPlayer + " voted first time.");
			return null;
		}

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			formatDate = sdf.parse(strDate);
		} catch (ParseException e) {
			neko.getLogger().warning(e.getMessage());
			return null;
		}

		return formatDate;
	}

	public void saveLastVotedDate(String strPlayer, Date date) {
		List<String> path = new ArrayList<String>();
		List<String> data = new ArrayList<String>();
		
		path.add("Player." + strPlayer + ".Vote.LastDate");
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		data.add(sdf.format(date));
		
		savePlayerData(path, data);
	}

	public void saveSuccessionVote(String strPlayer, int succession) {
		try {
			File configFile = new File(neko.getDataFolder(), "player.yml");

			if (configFile != null) {
				FileConfiguration conf = new YamlConfiguration();
				conf.load(configFile);

				conf.set("Player." + strPlayer + ".Vote.Succession", succession);

				conf.save(configFile);
			}
		} catch (Exception e) {
			neko.getLogger().warning(e.getMessage());
		}
	}
}

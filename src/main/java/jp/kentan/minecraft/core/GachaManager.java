package jp.kentan.minecraft.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import twitter4j.Status;
import twitter4j.User;

public class GachaManager {
	public enum Type{Money, Diamond, EventTicket}
	
	private NekoCore neko = null;
	private ConfigManager config = null;
	private EconomyManager eco = null;
	private Twitter tw = null;
	
	private Random random = new Random();
	
	private static Map<String, Integer> userMap = new HashMap<String, Integer>();
	private static Map<String, Type> userTypeMap = new HashMap<String, Type>();
	
	public static Map<Type, Integer> sizeMap = new HashMap<Type, Integer>();
	public static Map<Type, Integer> costMap = new HashMap<Type, Integer>();
	public static Map<Type, Integer> rewardMap = new HashMap<Type, Integer>();
	
	public GachaManager(NekoCore neko, ConfigManager config, EconomyManager eco, Twitter tw){
		this.neko = neko;
		this.config = config;
		this.eco = eco;
		this.tw = tw;
		
		userMap.clear();
	}
	
	public void EventHandler(){
		for(Map.Entry<String, Integer> entry : userMap.entrySet()){
			String key = entry.getKey();
			int timer = entry.getValue();
			
			userMap.put(key, ++timer);
			
			if(timer > 300){
				reset(key);
			}
		}
	}
	
	private void gacha(User source, Status status){
		String twitterID    = source.getScreenName();
		String minecraftID  = config.getMinecraftID(twitterID);
		Type type = userTypeMap.get(twitterID);
		
		if(!config.isLinkedTwitter(minecraftID, twitterID)){
			tw.reply(twitterID, "うーん...そのアカウントはまだリンクされていないよ" + tw.bot.getNekoFace() + "\nサーバーにログインして「/nk account " + twitterID + "」と入力してね.", status.getId());		
			
			reset(twitterID);
			return;
		}
		
		if(eco.withdraw(minecraftID, (double)(getCost(type)))){
			int gacha = random.nextInt(getProb(type));
			
			if(gacha == 0){
				reward(type, minecraftID);
				tw.reply(twitterID, "ぐふふ. あったりー" + tw.bot.getNekoFace() + "\nおめでとっ！" + minecraftID +"にこっそり" + getRewardName(type) + "を追加しといたよ" + tw.bot.getNekoFace(), status.getId());
			}else{
				tw.reply(twitterID, tw.bot.getGachaMissMsg() + "\nもう一度挑戦するならこのツイートをいいねしてね" + tw.bot.getNekoFace(), status.getId());
				
				userMap.put(twitterID, 0);
				
				neko.getLogger().info("Gacha result is " + gacha);
				return;
			}
			
		}else{
			tw.reply(twitterID, "あっれー. 何か失敗したーっ.." + tw.bot.getNekoFace(), status.getId());
		}
		
		reset(twitterID);
	}
	
	private void reward(Type type, String minecraftID){
		switch (type) {
		case Money:
			eco.deposit(minecraftID, rewardMap.get(type).doubleValue());
			break;
		case Diamond:
			config.addPlayerGachaRewards(minecraftID, "give " + minecraftID + " minecraft:diamond " + rewardMap.get(type) + " 0");
			break;
		case EventTicket:
			config.addPlayerGachaRewards(minecraftID, "event ticket " + minecraftID + " " + rewardMap.get(type));
			break;
		default:
			break;
		}
	}
	
	public Type create(String user){
		Type type = null;
		int rand = random.nextInt(3);
		
		switch (rand) {
		case 0:
			type = Type.Money;
			break;
		case 1:
			type = Type.Diamond;
			break;
		case 2:
			type = Type.EventTicket;
			break;
		default:
			break;
		}
		
		userMap.put(user, 0);
		userTypeMap.put(user, type);
		
		neko.getLogger().info("Gacha(" + user + "," + type + ") create.");
		
		return type;
	}
	
	public void reset(String entry){
		userMap.remove(entry);
		userTypeMap.remove(entry);
		neko.getLogger().info("Gacha(" + entry + ") was reset.");
	}
	
	public int getCost(Type type){
		return costMap.get(type);
	}
	
	public int getProb(Type type){
		return sizeMap.get(type);
	}
	
	public String getRewardName(Type type){
		String strReward = null;
		
		switch (type) {
		case Money:
			strReward = rewardMap.get(type) + "円";
			break;
		case Diamond:
			strReward = rewardMap.get(type) + "個のダイアモンド";
			break;
		case EventTicket:
			strReward = rewardMap.get(type) + "枚のイベチケ";
			break;
		default:
			break;
		}
		
		return strReward;
	}
	
	public void giveRewards(Player player){
		Server server = neko.getServer();
		String strPlayer = player.getName();
		List<String> rewardsList = config.getPlayerGachaRewards(strPlayer);
		
		if(rewardsList == null || rewardsList.size() <= 0){
			return;
		}
		
		for(String reward : rewardsList){
			server.dispatchCommand(server.getConsoleSender(), reward);
		}
		
		player.sendMessage(NekoCore.nc_tag + "猫botガチャのリワードを入手しました！");

		config.deletePlayerGachaRewards(strPlayer);
	}
	
	public void checkFlag(String user){
		for(Map.Entry<String, Integer> entry : userMap.entrySet()){
			if(user.equals(entry.getKey())){
				reset(user);
				break;
			}
		}
	}
	
	public void trigger(User source, Status favoritedStatus){
		for(Map.Entry<String, Integer> entry : userMap.entrySet()){
			if(source.getScreenName().equals(entry.getKey()) && favoritedStatus.getText().indexOf("このツイートをいいねしてね") != -1){
				gacha(source, favoritedStatus);
				return;
			}
		}
	}
}

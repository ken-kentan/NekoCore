package jp.kentan.minecraft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import twitter4j.Status;
import twitter4j.User;

public class BotManager {
	
	private static enum Command{None, PlayerNum, ServerLoad, findStaff, Reboot, Trigger, Cancel,
		Lucky, Thanks, Morning, Weather, Nyan, Gacha, GetBalance}
	
	private NekoCore nekoCore = null;
	private Twitter  tw       = null;
	
	/* Bot Messages List */
	public static List<String> nekoFaceList         = new ArrayList<String>();
	public static List<String> msgPlayerActionList  = new ArrayList<String>();
	public static List<String> msgUnkownCommandList = new ArrayList<String>();
	public static List<String> msgRejectCommandList = new ArrayList<String>();
	public static List<String> msgThanksList        = new ArrayList<String>();
	public static List<String> msgLuckyList         = new ArrayList<String>();
	public static List<String> msgMorningList       = new ArrayList<String>();
	public static List<String> msgWeatherList       = new ArrayList<String>();
	public static List<String> msgNyanList          = new ArrayList<String>();
	public static List<String> msgGachaMissList     = new ArrayList<String>();
	
	/* Gacha */
	private static List<String>  readyGachaUserList = new ArrayList<String>();
	private static List<Integer> timerGachaUserList = new ArrayList<Integer>();
	
	public static int gachaSize   = 10,
			          gachaCost   = 100,
			          gachaReward = 1000;
	/* Gacha end */
	
	private static Random random = new Random();
	
	private static boolean isReadyReboot  = false;
	
	public BotManager(NekoCore _neko, Twitter _tw) {
		nekoCore = _neko;
		tw = _tw;
		
		nekoCore.getLogger().info("Successfully initialized the Bot Module.");
	}
	
	public void eventHandler(){
		for(int i=0; i<readyGachaUserList.size(); i++) {
			int timer = timerGachaUserList.get(i);
		    timerGachaUserList.set(i, ++timer);
		    
		    if(timer > 60) resetGacha(i);
		}
	}
	
	public void reaction(Status status){
		String user = status.getUser().getScreenName();
		
		switch(typeCommand(status.getText())){
		case PlayerNum:
			tw.reply(user, "現在のプレイヤー数は" + Bukkit.getOnlinePlayers().size() + "人だよ" + getNekoFace(), status.getId());
			break;
		case ServerLoad:
			Double tps = Lag.getTPS();
			
			if(tps > 20.0D) tps = 20.0D;
			String str_per = String.format("%.2f%%", (100.0D - tps * 5.0D));
			
			tw.reply(user, "現在のサーバー負荷率は" + str_per + "だよ" + getNekoFace(), status.getId());
			break;
		case findStaff:
			int cntOP = 0;
			for(Player p : nekoCore.getServer().getOnlinePlayers()){
				if(p.isOp()) cntOP++;
			}
			tw.reply(user, "現在、ログインしている運営は" + cntOP + "人だよ" + getNekoFace(), status.getId());
			break;
		case Reboot:
			if(tw.isOwner(status)){
				isReadyReboot = true;
				tw.reply(user, "ほんとにサーバー再起動するの" + getNekoFace() + "？", status.getId());
			}else{
				tw.reply(user, getRejectCommandMsg().replace("{face}", getNekoFace()), status.getId());
			}
			break;
		case Trigger:
			if(tw.isOwner(status)){
				if(isReadyReboot){
					tw.reply(user, "サーバーを再起動します.", status.getId());
					nekoCore.rebootModule();
				}
			}
			break;
		case Cancel:
			if(tw.isOwner(status)){
				if(isReadyReboot){
					isReadyReboot = false;
					tw.reply(user, "サーバーの再起動を中止しました.", status.getId());
				}
			}
			break;
		case Lucky:
			tw.reply(user, getLuckyMsg().replace("{face}", getNekoFace()), status.getId());
			break;
		case Thanks:					
			tw.reply(user, getThanksMsg().replace("{face}", getNekoFace()), status.getId());
			break;
		case Morning:
			tw.reply(user, getMorningMsg().replace("{face}", getNekoFace()), status.getId());
			break;
		case Weather:
			String tweetMsg = getWeatherMsg();
			
			tweetMsg = tweetMsg.replace("{world}", "猫ワールド");
			
			switch(nekoCore.getWeather()){
			case 0:
				tweetMsg = tweetMsg.replace("{weather}", "晴れ");
				break;
			case 1:
				tweetMsg = tweetMsg.replace("{weather}", "雨");
				break;
			case 2:
				tweetMsg = tweetMsg.replace("{weather}", "雨ときどき雷");
				break;
			}
			
			tweetMsg = tweetMsg.replace("{face}", getNekoFace());
			
			tw.reply(user, tweetMsg, status.getId());
			break;
		case Nyan:
			tw.reply(user, getNyanMsg().replace("{face}", getNekoFace()), status.getId());
			break;
		case Gacha:
			for(String readyUserString : readyGachaUserList){
				if(readyUserString.equals(user)){
					return;
				}
			}
			
			tw.reply(user, "1回鯖ﾏﾈｰ" + gachaCost + "円で猫ガチャするー" + getNekoFace() + "？" + "1/" + gachaSize + "の確率で" + gachaReward +
					"円がもらえるよ" + getNekoFace() + "\nプレイするにはこのツイートをお気に入りしてね！  #猫ガチャ", status.getId());
			
			readyGachaUserList.add(user);
			timerGachaUserList.add(0);
			
			break;
		case GetBalance:
			String minecraftID  = nekoCore.config.getMinecraftID(user);
			
			if(minecraftID != null){
				tw.reply(user, minecraftID + "の現在の所持金は" + nekoCore.economy.getBalance(minecraftID) + "円だよ" + getNekoFace(), status.getId());
			}else{
				tw.reply(user, "うーん...そのアカウントはまだリンクされていないよ" + getNekoFace() + "\nサーバーにログインして「/nk account " + user + "」と入力してね.", status.getId());
			}
			break;
		default:					
			tw.reply(user, getUnkownCommandMsg().replace("{face}", getNekoFace()), status.getId());
			break;
		}
	}
	
	private Command typeCommand(String str){
    	if((str.indexOf("プレイヤー") != -1 || str.indexOf("ログイン") != -1) && (str.indexOf("数") != -1 || str.indexOf("何人") != -1)){
    		return Command.PlayerNum;
    	}
    	if(str.indexOf("サーバー") != -1 && (str.indexOf("負荷") != -1 || str.indexOf("重") != -1)){
    		return Command.ServerLoad;
    	}
    	if(str.indexOf("スタッフ") != -1 || str.indexOf("運営") != -1){
    		return Command.findStaff;
    	}
    	if(str.indexOf("おみくじ") != -1 || str.indexOf("運勢") != -1){
    		return Command.Lucky;
    	}
    	if(str.indexOf("再起動") != -1){
    		return Command.Reboot;
    	}
    	if(str.indexOf("やれ") != -1 || str.indexOf("おｋ") != -1 || str.indexOf("いいよ") != -1){
    		return Command.Trigger;
    	}
    	if(str.indexOf("なし") != -1 || str.indexOf("嘘") != -1 || str.indexOf("中止") != -1){
    		return Command.Cancel;
    	}
    	if(str.indexOf("えらい") != -1 || str.indexOf("あり") != -1 || str.indexOf("かしこい") != -1 || str.indexOf("かわいい") != -1){
    		return Command.Thanks;
    	}
    	if(str.indexOf("おは") != -1){
    		return Command.Morning;
    	}
    	if(str.indexOf("天気") != -1 || str.indexOf("雨") != -1 || str.indexOf("晴れ") != -1){
    		return Command.Weather;
    	}
    	if(str.indexOf("にゃ") != -1 || str.indexOf("猫") != -1){
    		return Command.Nyan;
    	}
    	if(str.indexOf("ガチャ") != -1){
    		return Command.Gacha;
    	}
    	if(str.indexOf("所持金") != -1){
    		return Command.GetBalance;
    	}
    	
    	return Command.None;
    }
	
	/* Gacha system */
	private void gacha(User source, Status status, int indexList){
		String twitterID    = source.getScreenName();
		String minecraftID  = nekoCore.config.getMinecraftID(twitterID);
		
		if(!nekoCore.config.isLinkedTwitterAccount(minecraftID, twitterID)){
			tw.reply(twitterID, "うーん...そのアカウントはまだリンクされていないよ" + getNekoFace() + "\nサーバーにログインして「/nk account " + twitterID + "」と入力してね.", status.getId());		
		}else if(nekoCore.economy.deposit(minecraftID, (double)(-gachaCost))){
			int gacha = random.nextInt(gachaSize);
			switch (gacha) {
			case 1:
				nekoCore.economy.deposit(minecraftID, (double)gachaReward);
				tw.reply(twitterID, "ぐふふ. あったりー" + getNekoFace() + "\nおめでとっ！" + minecraftID +"にこっそり" + gachaReward + "円を追加しといたよ" + getNekoFace(), status.getId());
				break;
			default://Miss
				tw.reply(twitterID, getGachaMissMsg() + "\nもう一度挑戦するならこのツイートをお気に入りしてね" + getNekoFace(), status.getId());
				timerGachaUserList.set(indexList, 0);
				nekoCore.getLogger().info("Gacha result is " + gacha);
				return;
			}
			nekoCore.getLogger().info("Gacha result is " + gacha);
		}else{
			tw.reply(twitterID, "あっれー. 何か失敗したーっ.." + getNekoFace(), status.getId());
		}
		
		resetGacha(indexList);
	}
	
	public void triggerGacha(User source, Status favoritedStatus){
		int index = 0;
		
		for(String readyUserString : readyGachaUserList){
			if(readyUserString.equals(source.getScreenName()) && favoritedStatus.getText().indexOf("このツイートをお気に入りしてね") != -1){
				gacha(source, favoritedStatus, index);
			}
			++index;
		}
	}
	
	private void resetGacha(int index){
		readyGachaUserList.remove(index);
		timerGachaUserList.remove(index);
		
		nekoCore.getLogger().info("Gacha(" + index + ") was reset.");
	}
	/* Gacha end */
	
	/* return messages etc */
	public String getNekoFace(){
    	return nekoFaceList.get(random.nextInt(nekoFaceList.size()));
    }
    
    public String getActionMsg(){
    	return msgPlayerActionList.get(random.nextInt(msgPlayerActionList.size()));
    }
    
    private String getUnkownCommandMsg(){
    	return msgUnkownCommandList.get(random.nextInt(msgUnkownCommandList.size()));
    }
    
    private String getRejectCommandMsg(){
    	return msgRejectCommandList.get(random.nextInt(msgRejectCommandList.size()));
    }
    
    private String getThanksMsg(){
    	return msgThanksList.get(random.nextInt(msgThanksList.size()));
    }
    
    private String getLuckyMsg(){
    	return msgLuckyList.get(random.nextInt(msgLuckyList.size()));
    }
    
    private String getMorningMsg(){
    	return msgMorningList.get(random.nextInt(msgMorningList.size()));
    }
    
    private String getWeatherMsg(){
    	return msgWeatherList.get(random.nextInt(msgWeatherList.size()));
    }
    
    private String getNyanMsg(){
    	return msgNyanList.get(random.nextInt(msgNyanList.size()));
    }
    
    private String getGachaMissMsg(){
    	return msgGachaMissList.get(random.nextInt(msgGachaMissList.size())).replace("{face}", getNekoFace());
    }

}

package jp.kentan.minecraft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import twitter4j.Status;

public class BotManager {
	
	private enum Command{None, PlayerNum, ServerLoad, findStaff, Reboot, Trigger, Cancel,
		Lucky, Thanks, Morning, Weather, Nyan, Gacha, RareGacha, GetBalance, AskOnlinePlayer, Detach}
	
	private NekoCore nekoCore = null;
	private Twitter  tw       = null;
	private GachaManager gacha = null;
	
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
	public static List<String> msgAskYesList        = new ArrayList<String>();
	public static List<String> msgAskNoList         = new ArrayList<String>();
	
	
	private Random random = new Random();
	
	private boolean isReadyReboot = false,
			        isReadyDetach = false;
	
	public BotManager(NekoCore nekoCore, Twitter tw, GachaManager gacha) {
		this.nekoCore = nekoCore;
		this.tw = tw;
		this.gacha = gacha;
		
		nekoCore.getLogger().info("Successfully initialized the Bot Module.");
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
				tw.reply(user, getRejectCommandMsg(), status.getId());
			}
			break;
		case Trigger:
			if(tw.isOwner(status)){
				if(isReadyReboot){
					tw.reply(user, "サーバーを再起動します.", status.getId());
					nekoCore.rebootModule();
				}else if(isReadyDetach){
					tw.reply(user, "NekoCoreをサーバーから切り離します.\n再ロードはコンソールから行ってください.", status.getId());
					nekoCore.getServer().dispatchCommand(nekoCore.getServer().getConsoleSender(), "plugman unload NekoCore");
				}
			}
			break;
		case Cancel:
			if(tw.isOwner(status)){
				if(isReadyReboot){
					isReadyReboot = false;
					tw.reply(user, "サーバーの再起動を中止しました.", status.getId());
				}else if(isReadyDetach){
					isReadyDetach = false;
					tw.reply(user, "NekoCoreの切り離しを中止しました.", status.getId());					
				}
			}
			break;
		case Lucky:
			tw.reply(user, getLuckyMsg(), status.getId());
			break;
		case Thanks:					
			tw.reply(user, getThanksMsg(), status.getId());
			break;
		case Morning:
			tw.reply(user, getMorningMsg(), status.getId());
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
			
			tw.reply(user, tweetMsg, status.getId());
			break;
		case Nyan:
			tw.reply(user, getNyanMsg().replace("{face}", getNekoFace()), status.getId());
			break;
		case Gacha:
			gacha.checkFlag(user);
			
			GachaManager.Type type = gacha.setup(user);
			
			tw.reply(user, "1回鯖ﾏﾈｰ" + gacha.getCost(type) + "円で猫ガチャするー" + getNekoFace() + "？" + "1/" + gacha.getProb(type) + "の確率で" + gacha.getRewardName(type) +
					"がもらえるよ" + getNekoFace() + "\nプレイするにはこのツイートをいいねしてね！  #猫ガチャ", status.getId());
			
			break;
		case GetBalance:
			String minecraftID  = nekoCore.config.getMinecraftID(user);
			
			if(minecraftID != null){
				tw.reply(user, minecraftID + "の現在の所持金は" + nekoCore.economy.getBalance(minecraftID) + "円だよ" + getNekoFace(), status.getId());
			}else{
				tw.reply(user, "うーん...そのアカウントはまだリンクされていないよ" + getNekoFace() + "\nサーバーにログインして「/nk account " + user + "」と入力してね.", status.getId());
			}
			break;
		case AskOnlinePlayer:
			String mcID = getIncludeWord(status.getText());
			
			if(nekoCore.getServer().getPlayer(mcID) != null){
				tw.reply(user, getAskYesMsg().replace("{player}", mcID).replace("{status}", "ログイン"), status.getId());
			}else{
				tw.reply(user, getAskNoMsg().replace("{player}", mcID).replace("{status}", "ログイン"), status.getId());
			}
			break;
		case Detach:
			if(tw.isOwner(status)){
				isReadyDetach = true;
				tw.reply(user, "ほんとにNekoCoreをサーバーか切り離すの" + getNekoFace() + "？", status.getId());
			}else{
				tw.reply(user, getRejectCommandMsg(), status.getId());
			}
			break;
		default:					
			tw.reply(user, getUnkownCommandMsg(), status.getId());
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
    	if(isIncludeWord(str) && (str.indexOf("いる") != -1 || str.indexOf("ログイン") != -1)){
    		return Command.AskOnlinePlayer;
    	}
    	if(str.indexOf("切り離し") != -1){
    		return Command.Detach;
    	}
    	
    	return Command.None;
    }
	
	private boolean isIncludeWord(String str){
		if(str.indexOf("「") != -1 && str.indexOf("」") != -1){
			return true;
		}
		
		return false;
	}
	
	private String getIncludeWord(String str){
		int beginIndex = str.indexOf("「") + 1,
			endIndex   = str.indexOf("」");
		
		return str.substring(beginIndex, endIndex);
	}
	
	/* return messages etc */
	public String getNekoFace(){
    	return nekoFaceList.get(random.nextInt(nekoFaceList.size()));
    }
    
    public String getActionMsg(){
    	return msgPlayerActionList.get(random.nextInt(msgPlayerActionList.size()));
    }
    
    private String getUnkownCommandMsg(){
    	return msgUnkownCommandList.get(random.nextInt(msgUnkownCommandList.size())).replace("{face}", getNekoFace());
    }
    
    private String getRejectCommandMsg(){
    	return msgRejectCommandList.get(random.nextInt(msgRejectCommandList.size())).replace("{face}", getNekoFace());
    }
    
    private String getThanksMsg(){
    	return msgThanksList.get(random.nextInt(msgThanksList.size())).replace("{face}", getNekoFace());
    }
    
    private String getLuckyMsg(){
    	return msgLuckyList.get(random.nextInt(msgLuckyList.size())).replace("{face}", getNekoFace());
    }
    
    private String getMorningMsg(){
    	return msgMorningList.get(random.nextInt(msgMorningList.size())).replace("{face}", getNekoFace());
    }
    
    private String getWeatherMsg(){
    	return msgWeatherList.get(random.nextInt(msgWeatherList.size())).replace("{face}", getNekoFace());
    }
    
    private String getNyanMsg(){
    	return msgNyanList.get(random.nextInt(msgNyanList.size()));
    }
    
    public String getGachaMissMsg(){
    	return msgGachaMissList.get(random.nextInt(msgGachaMissList.size())).replace("{face}", getNekoFace());
    }
    
    private String getAskYesMsg(){
    	return msgAskYesList.get(random.nextInt(msgAskYesList.size())).replace("{face}", getNekoFace());
    }
    
    private String getAskNoMsg(){
    	return msgAskNoList.get(random.nextInt(msgAskNoList.size())).replace("{face}", getNekoFace());
    }

}

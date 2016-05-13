package jp.kentan.minecraft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusUpdate;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterBot {
	enum Command{None, PlayerNum, ServerLoad, findStaff, Reboot, Trigger, Cancel, Lucky, Thanks, Morning, Weather, Nyan, Gacha}
	
	private static NekoCore nekoCore;
	private static EconomyManager economy;
	private static ConfigManager config;
	private static AsyncTwitter twitter;
	private static TwitterStream twitterStream;
	
	public static String consumerKey       = "";
	public static String consumerSecret    = "";
	public static String accessToken       = "";
	public static String accessTokenSecret = "";
	
	public static List<String> nekoFaceList         = new ArrayList<String>();
	public static List<String> msgPlayerActionList  = new ArrayList<String>();
	public static List<String> msgUnkownCommandList = new ArrayList<String>();
	public static List<String> msgRejectCommandList = new ArrayList<String>();
	public static List<String> msgThanksList        = new ArrayList<String>();
	public static List<String> msgLuckyList         = new ArrayList<String>();
	public static List<String> msgMorningList       = new ArrayList<String>();
	public static List<String> msgWeatherList       = new ArrayList<String>();
	public static List<String> msgNyanList          = new ArrayList<String>();
	
	public static int gachaSize   = 10,
			          gachaCost   = 100,
			          gachaReward = 1000;
	
	private static Random random = new Random();
	
	private static boolean isTweenEnable = true,
						   isReadyReboot = false,
						   isReadyGacha  = false;
	
	public static void init(NekoCore _neko, EconomyManager _economy, ConfigManager _config){
		nekoCore = _neko;
		economy = _economy;
		config = _config;
		
		
		twitter = new AsyncTwitterFactory().getInstance();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(new AccessToken(accessToken,accessTokenSecret));
		twitter.addListener(new TwitterAdapter() {
            @Override
            public void updatedStatus(Status status) {
                nekoCore.getLogger().info("Async Tweet:" + status.getText());
            }

            @Override
            public void onException(TwitterException e, TwitterMethod method) {
            	nekoCore.getLogger().warning("Async Tweet Failed " + e.getMessage());
            }
        });
		
		//Stream config
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerKey);
        builder.setOAuthConsumerSecret(consumerSecret);
        builder.setOAuthAccessToken(accessToken);
        builder.setOAuthAccessTokenSecret(accessTokenSecret);

        Configuration conf = builder.build();
        twitterStream = new TwitterStreamFactory(conf).getInstance();
        twitterStream.addListener(new streamListener());

        //start user Stream
        twitterStream.user();
		
		nekoCore.getLogger().info("TwitterBotモジュールを初期化しました。");
	}
	
	public static void closeStream(){
		twitterStream.shutdown();
		nekoCore.getLogger().info("TwitterStreamを停止しました。");
	}
	
	static class streamListener extends StatusAdapter {
	    public void onStatus(Status status) {
	    	if(isReplay(status)){
    			String user = status.getUser().getScreenName();
    			nekoCore.getLogger().info("Twitter:" + user + "からリプライを取得.");
    			
    			switch(typeCommand(status.getText())){
    			case PlayerNum:
    				replyTweet(user, "現在のプレイヤー数は" + Bukkit.getOnlinePlayers().size() + "人だよ" + getNekoFace(), status.getId());
    				break;
    			case ServerLoad:
    				Double tps = Lag.getTPS();
    				
    				if(tps > 20.0D) tps = 20.0D;
    				String str_per = String.format("%.2f%%", (100.0D - tps * 5.0D));
    				
    				replyTweet(user, "現在のサーバー負荷率は" + str_per + "だよ" + getNekoFace(), status.getId());
    				break;
    			case findStaff:
    				int cntOP = 0;
    				for(Player p : nekoCore.getServer().getOnlinePlayers()){
    					if(p.isOp()) cntOP++;
    				}
    				replyTweet(user, "現在、ログインしている運営は" + cntOP + "人だよ" + getNekoFace(), status.getId());
    				break;
    			case Reboot:
    				if(isOwner(status)){
						isReadyReboot = true;
						replyTweet(user, "ほんとにサーバー再起動するの" + getNekoFace() + "？", status.getId());
    				}else{
    					replyTweet(user, getRejectCommandMsg().replace("{face}", getNekoFace()), status.getId());
    				}
    				break;
    			case Trigger:
    				if(isOwner(status)){
						if(isReadyReboot){
							replyTweet(user, "サーバーを再起動します.", status.getId());
							nekoCore.rebootModule();
						}
    				}
    				break;
    			case Cancel:
    				if(isOwner(status)){
						if(isReadyReboot){
							isReadyReboot = false;
							replyTweet(user, "サーバーの再起動を中止しました.", status.getId());
						}
    				}
    				break;
    			case Lucky:
					replyTweet(user, getLuckyMsg().replace("{face}", getNekoFace()), status.getId());
					break;
    			case Thanks:					
					replyTweet(user, getThanksMsg().replace("{face}", getNekoFace()), status.getId());
					break;
    			case Morning:
					replyTweet(user, getMorningMsg().replace("{face}", getNekoFace()), status.getId());
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
    				
					replyTweet(user, tweetMsg, status.getId());
    				break;
    			case Nyan:
    				replyTweet(user, getNyanMsg().replace("{face}", getNekoFace()), status.getId());
    				break;
    			case Gacha:
    				if(!isReadyGacha){
    					replyTweet(user, "猫ガチャ(1回鯖ﾏﾈｰ" + gachaCost + "円)するー" + getNekoFace() + "？" + "\nリプライでマイクラIDを「」に入れて教えてね！(試験機能) #猫ガチャ", status.getId());
    					isReadyGacha = true;
    					return;
    				}
    				
    				String strPlayerID = getIDfromString(status.getText());
    				
    				if(!economy.isPlayer(strPlayerID) && !config.isLinkedTwitterAccount(strPlayerID, status.getUser().getScreenName())){
    					replyTweet(user, "うーん...\nプレイヤー「" + strPlayerID + "」を見つけれなかったよ" + getNekoFace() +
    							"\nまだ、アカウントをリンクしていないなら、サーバーにログインして「/nk account <Twitter ID>」と入力してね.", status.getId());
    					isReadyGacha = false;
    				}else if(economy.deposit(strPlayerID, (double)(-gachaCost))){
    					int gacha = random.nextInt(gachaSize);
    					switch (gacha) {
						case 1:
							economy.deposit(strPlayerID, (double)gachaReward);
							replyTweet(user, "ぐふふ. あったりー" + getNekoFace() + "\nおめでとっ！" + strPlayerID +"にこっそり" + gachaReward + "円を追加しといたよ" + getNekoFace(), status.getId());
							break;
						default:
							replyTweet(user, "はずれーっ.残念..." + getNekoFace(), status.getId());
							break;
						}
    					nekoCore.getLogger().info("Gacha:" + gacha);
    				}else{
    					replyTweet(user, "失敗.." + getNekoFace(), status.getId());
    				}
    				break;
				default:					
					replyTweet(user, getUnkownCommandMsg().replace("{face}", getNekoFace()), status.getId());
					break;
    			}
	    	}
	    }
	}
	
	public static void switchMode(){
		isTweenEnable = !isTweenEnable;
		
		nekoCore.getLogger().info("TwitterBotモジュールを" + isTweenEnable + "にしました。");
	}
	
	public static void tweet(String str){
		if(!isTweenEnable) return;
		
		twitter.updateStatus(str + "\n#猫鯖");
	}
	
    public static void replyTweet (String user, String message, long statusId) {
    	if(!isTweenEnable) return;
		twitter.updateStatus(new StatusUpdate("@" + user + " " + message).inReplyToStatusId(statusId));
    }
    
    public static void sendDM(String user, String str){
    	twitter.sendDirectMessage(user, str);
    	nekoCore.getLogger().info("Twitter DM:Successfully sent to " + user);
    }
	
	static boolean isReplay(Status status){
		if(status.getText().indexOf("@DekitateServer") != -1 && !status.getUser().getScreenName().equals("DekitateServer")) return true;
		
		return false;
	}
	
	static boolean isOwner(Status status){
		if(status.getUser().getScreenName().equals("ken_kentan")) return true;
		
		return false;
	}
    
    static Command typeCommand(String str){
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
    	if(str.indexOf("ガチャ") != -1 || (str.indexOf("「") != -1 && str.indexOf("」") != -1)){
    		if(str.indexOf("「") == -1 && str.indexOf("」") == -1){
    			isReadyGacha = false;
    		}
    		return Command.Gacha;
    	}
    	
    	return Command.None;
    }
    
    static String getIDfromString(String str){

    	int index_top = str.indexOf("「") + 1;
    	int index_bottom = str.indexOf("」");
    	
    	if(index_top == -1 || index_bottom == -1){
    		return "!ERROR!";
    	}
    	
    	return str.substring(index_top,index_bottom);
    }
    
    public static String getNekoFace(){
    	return nekoFaceList.get(random.nextInt(nekoFaceList.size()));
    }
    
    public static String getActionMsg(){
    	return msgPlayerActionList.get(random.nextInt(msgPlayerActionList.size()));
    }
    
    private static String getUnkownCommandMsg(){
    	return msgUnkownCommandList.get(random.nextInt(msgUnkownCommandList.size()));
    }
    
    private static String getRejectCommandMsg(){
    	return msgRejectCommandList.get(random.nextInt(msgRejectCommandList.size()));
    }
    
    private static String getThanksMsg(){
    	return msgThanksList.get(random.nextInt(msgThanksList.size()));
    }
    
    private static String getLuckyMsg(){
    	return msgLuckyList.get(random.nextInt(msgLuckyList.size()));
    }
    
    private static String getMorningMsg(){
    	return msgMorningList.get(random.nextInt(msgMorningList.size()));
    }
    
    private static String getWeatherMsg(){
    	return msgWeatherList.get(random.nextInt(msgWeatherList.size()));
    }
    
    private static String getNyanMsg(){
    	return msgNyanList.get(random.nextInt(msgNyanList.size()));
    }
}
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
	enum Command{None, PlayerNum, ServerLoad, findStaff, Reboot, Trigger, Cancel, Lucky, Thanks, Morning, Weather, Nyan}
	
	private static NekoCore nekoCore;
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
	
	private static Random random = new Random();
	
	private static boolean isTweenEnable = true,
						   isReadyReboot = false;
	
	public static void init(NekoCore _neko){
		nekoCore = _neko;
		
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
		
		//Streamの設定
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(consumerKey);
        builder.setOAuthConsumerSecret(consumerSecret);
        builder.setOAuthAccessToken(accessToken);
        builder.setOAuthAccessTokenSecret(accessTokenSecret);

        Configuration conf = builder.build();
        twitterStream = new TwitterStreamFactory(conf).getInstance();
        twitterStream.addListener(new streamListener());

        // Streamの実行
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
    				
    				if(nekoCore.hasStorm()) tweetMsg = tweetMsg.replace("{weather}", "雨");
    				else                    tweetMsg = tweetMsg.replace("{weather}", "晴れ");
    				
    				tweetMsg = tweetMsg.replace("{face}", getNekoFace());
    				
					replyTweet(user, tweetMsg, status.getId());
    				break;
    			case Nyan:
    				replyTweet(user, getNyanMsg().replace("{face}", getNekoFace()), status.getId());
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
		if(status.getText().indexOf("@DekitateServer") != -1) return true;
		
		return false;
	}
	
	static boolean isOwner(Status status){
		if(status.getUser().getScreenName().equals("ken_kentan")) return true;
		
		return false;
	}
    
    static Command typeCommand(String str){
    	if((str.indexOf("プレイヤー") != -1 || str.indexOf("ログイン") != -1) && (str.indexOf("数") != -1 || str.indexOf("何人") != -1)) return Command.PlayerNum;
    	if(str.indexOf("サーバー") != -1 && (str.indexOf("負荷") != -1 || str.indexOf("重") != -1)) return Command.ServerLoad;
    	if(str.indexOf("スタッフ") != -1 || str.indexOf("運営") != -1) return Command.findStaff;
    	if(str.indexOf("おみくじ") != -1 || str.indexOf("運勢") != -1) return Command.Lucky;
    	if(str.indexOf("再起動") != -1)                              return Command.Reboot;
    	if(str.indexOf("やれ") != -1 || str.indexOf("おｋ") != -1 || str.indexOf("いいよ") != -1)    return Command.Trigger;
    	if(str.indexOf("なし") != -1 || str.indexOf("嘘") != -1 || str.indexOf("中止") != -1)      return Command.Cancel;
    	if(str.indexOf("えらい") != -1 || str.indexOf("あり") != -1 || str.indexOf("かしこい") != -1)  return Command.Thanks;
    	if(str.indexOf("おは") != -1)                                                        return Command.Morning;
    	if(str.indexOf("天気") != -1 || str.indexOf("雨") != -1 || str.indexOf("晴れ") != -1) return Command.Weather;
    	if(str.indexOf("にゃ") != -1 || str.indexOf("猫") != -1) return Command.Nyan;
    	
    	return Command.None;
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
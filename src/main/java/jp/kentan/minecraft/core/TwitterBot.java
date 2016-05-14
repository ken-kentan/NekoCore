package jp.kentan.minecraft.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.DirectMessage;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusUpdate;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterMethod;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserList;
import twitter4j.UserStreamListener;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterBot {
	private static enum Command{None, PlayerNum, ServerLoad, findStaff, Reboot, Trigger, Cancel, Lucky, Thanks, Morning, Weather, Nyan, Gacha}
	
	private static NekoCore nekoCore;
	private static EconomyManager economy;
	private static ConfigManager config;
	private static AsyncTwitter twitter;
	private static TwitterStream twitterStream;
	
	public static String consumerKey       = "";
	public static String consumerSecret    = "";
	public static String accessToken       = "";
	public static String accessTokenSecret = "";
	
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
	
	private static boolean isBotEnable    = true,
						   isReadyReboot  = false;
	
	TwitterBot(NekoCore _neko, EconomyManager _economy, ConfigManager _config){
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
        twitterStream.addListener(listener);

        //start user Stream
        twitterStream.user();
		
		nekoCore.getLogger().info("Successfully initialized the Twitter Module.");
	}
	
	public void eventHandler(){
		for(int i=0; i<readyGachaUserList.size(); i++) {
			int timer = timerGachaUserList.get(i);
		    timerGachaUserList.set(i, ++timer);
		    
		    if(timer > 30) resetGacha(i);
		}
	}
	
	public void closeStream(){
		twitterStream.shutdown();
		nekoCore.getLogger().info("Shutdown the TwitterStream.");
	}
	
	private final UserStreamListener listener = new UserStreamListener() {
		@Override
	    public void onStatus(Status status) {
	    	if(!isReplay(status)) return;
	    	
			String user = status.getUser().getScreenName();
			nekoCore.getLogger().info("Twitter:Get replay from @" + user);
			
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
				for(String readyUserString : readyGachaUserList){
					if(readyUserString.equals(user)){
						return;
					}
				}
				
				replyTweet(user, "1回鯖ﾏﾈｰ" + gachaCost + "円で猫ガチャするー" + getNekoFace() + "？" + "1/" + gachaSize + "の確率で" + gachaReward +
						"円がもらえるよ" + getNekoFace() + "\nプレイするにはこのツイートをお気に入りしてね！  #猫ガチャ", status.getId());
				
				readyGachaUserList.add(user);
				timerGachaUserList.add(0);
				
				break;
			default:					
				replyTweet(user, getUnkownCommandMsg().replace("{face}", getNekoFace()), status.getId());
				break;
			}
	    }

		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			if(!isFavToMe(target)) return;
			
			nekoCore.getLogger().info("Twitter:Get like from @" + source.getScreenName());
			
			int index = 0;
			
			for(String readyUserString : readyGachaUserList){
				if(readyUserString.equals(source.getScreenName()) && favoritedStatus.getText().indexOf("このツイートをお気に入りしてね") != -1){
					gacha(source, favoritedStatus, index);
				}
				++index;
			}
		}
		
		@Override
		public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

		@Override
		public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

		@Override
		public void onScrubGeo(long userId, long upToStatusId) {}

		@Override
		public void onStallWarning(StallWarning warning) {}

		@Override
		public void onException(Exception ex) {}

		@Override
		public void onDeletionNotice(long directMessageId, long userId) {}

		@Override
		public void onFriendList(long[] friendIds) {}

		@Override
		public void onUnfavorite(User source, User target, Status unfavoritedStatus) {}

		@Override
		public void onFollow(User source, User followedUser) {}

		@Override
		public void onUnfollow(User source, User unfollowedUser) {}

		@Override
		public void onDirectMessage(DirectMessage directMessage) {}

		@Override
		public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {}

		@Override
		public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {}

		@Override
		public void onUserListSubscription(User subscriber, User listOwner, UserList list) {}

		@Override
		public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {}

		@Override
		public void onUserListCreation(User listOwner, UserList list) {}

		@Override
		public void onUserListUpdate(User listOwner, UserList list) {}

		@Override
		public void onUserListDeletion(User listOwner, UserList list) {}

		@Override
		public void onUserProfileUpdate(User updatedUser) {}

		@Override
		public void onUserSuspension(long suspendedUser) {}

		@Override
		public void onUserDeletion(long deletedUser) {}

		@Override
		public void onBlock(User source, User blockedUser) {}

		@Override
		public void onUnblock(User source, User unblockedUser) {}

		@Override
		public void onRetweetedRetweet(User source, User target, Status retweetedStatus) {}

		@Override
		public void onFavoritedRetweet(User source, User target, Status favoritedRetweeet) {}

		@Override
		public void onQuotedTweet(User source, User target, Status quotingTweet) {}
	};
	
	private void gacha(User source, Status status, int indexList){
		String twitterID    = source.getScreenName();
		String minecraftID  = config.getMinecraftID(twitterID);
		
		if(!config.isLinkedTwitterAccount(minecraftID, twitterID)){
			replyTweet(twitterID, "うーん...そのアカウントはまだリンクされていないよ" + getNekoFace() + "\nサーバーにログインして「/nk account " + twitterID + "」と入力してね.", status.getId());		
		}else if(economy.deposit(minecraftID, (double)(-gachaCost))){
			int gacha = random.nextInt(gachaSize);
			switch (gacha) {
			case 1:
				economy.deposit(minecraftID, (double)gachaReward);
				replyTweet(twitterID, "ぐふふ. あったりー" + getNekoFace() + "\nおめでとっ！" + minecraftID +"にこっそり" + gachaReward + "円を追加しといたよ" + getNekoFace(), status.getId());
				break;
			default://Miss
				replyTweet(twitterID, getGachaMissMsg() + "\nもう一度挑戦するならこのツイートをお気に入りしてね" + getNekoFace(), status.getId());
				timerGachaUserList.set(indexList, 0);
				return;
			}
			nekoCore.getLogger().info("Gacha result is " + gacha);
		}else{
			replyTweet(twitterID, "あっれー. 何か失敗したーっ.." + getNekoFace(), status.getId());
		}
		
		resetGacha(indexList);
	}
	
	private void resetGacha(int index){
		readyGachaUserList.remove(index);
		timerGachaUserList.remove(index);
		
		nekoCore.getLogger().info("Gacha(" + index + ") was reset.");
	}
	
	public void switchBotStatus(){
		isBotEnable = !isBotEnable;
		String msg = null;
		
		if(isBotEnable){
			msg = "Successfully enabled the Twitter Bot.";
		}else{
			msg = "Successfully disabled the Twitter Bot.";
		}
		nekoCore.getLogger().info(msg);
	}
	
	public void tweet(String str){
		if(!isBotEnable) return;
		
		twitter.updateStatus(str + "\n#猫鯖");
	}
	
    public void replyTweet (String user, String message, long statusId) {
    	if(!isBotEnable) return;
		twitter.updateStatus(new StatusUpdate("@" + user + " " + message).inReplyToStatusId(statusId));
    }
    
    public void sendDM(String user, String str){
    	twitter.sendDirectMessage(user, str);
    	nekoCore.getLogger().info("Twitter DM:Successfully sent to " + user);
    }
	
	private boolean isReplay(Status status){
		if(status.getText().indexOf("@DekitateServer") != -1 && !status.getUser().getScreenName().equals("DekitateServer")) return true;
		
		return false;
	}
	
	private boolean isOwner(Status status){
		if(status.getUser().getScreenName().equals("ken_kentan")) return true;
		
		return false;
	}
	
	static boolean isFavToMe(User targer){
		if(targer.getScreenName().equals("DekitateServer")) return true;
		
		return false;
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
    
    private static String getGachaMissMsg(){
    	return msgGachaMissList.get(random.nextInt(msgGachaMissList.size())).replace("{face}", getNekoFace());
    }
}
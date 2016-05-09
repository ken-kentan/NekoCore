package jp.kentan.minecraft.core;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterBot {
	enum Command{None, PlayerNum, ServerLoad, findStaff, Reboot, Trigger, Cancel, Lucky, Thanks}
	
	private static NekoCore nekoCore;
	private static Twitter twitter;
	private static TwitterStream twitterStream;
	
	private static final String consumerKey       = "";
	private static final String consumerSecret    = "";
	private static final String accessToken       = "";
	private static final String accessTokenSecret = "";
	
	private static Random random = new Random();
	
	private static boolean isTweenEnable = true,
						   isReadyReboot = false;
	
	private static final String neko_face[] = {"しました(^・ω・^ )", "したよฅ(●´ω｀●)ฅ", "したにゃฅ⊱*•ω•*⊰ฅ", "したみたいฅ(^ω^ฅ)", "したﾆｬｰﾝ^ↀᴥↀ^", "したよっ(｡･ω･｡)", "しましたฅ•ω•ฅ", "したっちゃ]*ΦωΦ)ノ", "にゃん(=^･ω･^=)", "したべ(ﾉ*ФωФ)ﾉ", "したって♡￫ω￩♡"};
	
	private static final String unknown_command_msg[] = {"なに(^・ω・^ )?", "わからんฅ(●´ω｀●)ฅ", "にゃーんฅ⊱*•ω•*⊰ฅ", "おいでおいでฅ(^ω^ฅ)", "ほぅほぅ^ↀᴥↀ^"};
	private static final String reject_command_msg[] = {"(ฅ`･ω･´)っ それはできないっ！", "ヤダ(Ф∀Ф)", "だめ(⁎˃ᆺ˂)"};
	private static final String lucky_msg[] = {"ネコ吉(●ↀωↀ●)✧", "大吉ฅ(●´ω｀●)ฅ", "中吉ฅ⊱*•ω•*⊰ฅ", "小吉ฅ(´-ω-`)ฅ", "凶(ノω<。)", "大凶(´; ω ;｀) "};
	private static final String thanks_msg[] = {"('-'*)ｱﾘｶﾞﾄ♪", "てへぺろ(/ω＼)", "いぇいぇ～ゞ(￣ー￣ )", "(///ω///)", "どういたしましてっฅ(●´ω｀●)ฅ", "せやろฅ(^ω^ฅ)"};
	
	public static void init(NekoCore _neko){
		nekoCore = _neko;
		
		twitter = new TwitterFactory().getInstance();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(new AccessToken(accessToken,accessTokenSecret));
		
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
	
	static class streamListener extends StatusAdapter {
	    public void onStatus(Status status) {
	    	if(isReplay(status)){
    			String user = status.getUser().getScreenName();
    			nekoCore.getLogger().info("Twitter:" + user + "からリプライを取得.");
    			
    			switch(typeCommand(status.getText())){
    			case PlayerNum:
    				replyTweet(user, "現在のプレイヤー数は" + Bukkit.getOnlinePlayers().size() + "人です.", status.getId());
    				break;
    			case ServerLoad:
    				Double tps = Lag.getTPS();
    				
    				if(tps > 20.0D) tps = 20.0D;
    				String str_per = String.format("%.2f%%", (100.0D - tps * 5.0D));
    				
    				replyTweet(user, "現在のサーバー負荷率は" + str_per + "です.", status.getId());
    				break;
    			case findStaff:
    				int cntOP = 0;
    				for(Player p : nekoCore.getServer().getOnlinePlayers()){
    					if(p.isOp()) cntOP++;
    				}
    				replyTweet(user, "現在、ログインしている運営は" + cntOP + "人です.", status.getId());
    				break;
    			case Reboot:
    				if(isOwner(status)){
						isReadyReboot = true;
						replyTweet(user, "サーバーを再起動しますか？", status.getId());
    				}else{
    					replyTweet(user, reject_command_msg[random.nextInt(3)], status.getId());
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
					int luck_num = random.nextInt(20);
					
					if(luck_num == 19)       luck_num = 0;
					else if (luck_num >= 17) luck_num = 1;
					else if (luck_num >= 10) luck_num = 2;
					else if (luck_num >=  3) luck_num = 3;
					else if (luck_num >=  1) luck_num = 4;
					else                     luck_num = 5;
					
					replyTweet(user, lucky_msg[luck_num], status.getId());
					break;
    			case Thanks:					
					replyTweet(user, thanks_msg[random.nextInt(6)], status.getId());
					break;
				default:					
					replyTweet(user, unknown_command_msg[random.nextInt(5)], status.getId());
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
		
		try{
			twitter.updateStatus(str + "\n#猫鯖");
			nekoCore.getLogger().info("Tweet:" + str);
		} catch(TwitterException e){
			nekoCore.getLogger().warning("Tweet Failed:" + e.getMessage());
		}
	}
	
	static boolean isReplay(Status status){
		if(status.getText().indexOf("@DekitateServer") != -1) return true;
		
		return false;
	}
	
	static boolean isOwner(Status status){
		if(status.getUser().getScreenName().equals("ken_kentan")) return true;
		
		return false;
	}
	
    public static void replyTweet (String user, String message, long statusId) {
    	try {
			twitter.updateStatus(new StatusUpdate("@" + user + " " + message).inReplyToStatusId(statusId));
			nekoCore.getLogger().info("Tweet:" + "@" + user + " " + message);
		} catch (TwitterException e) {
			System.err.println("Tweet Failed:" + e.getMessage());
		}
    }
    
    static Command typeCommand(String str){
    	if((str.indexOf("プレイヤー") != -1 || str.indexOf("ログイン") != -1) && (str.indexOf("数") != -1 || str.indexOf("何人") != -1)) return Command.PlayerNum;
    	if(str.indexOf("サーバー") != -1 && (str.indexOf("負荷") != -1 || str.indexOf("重") != -1)) return Command.ServerLoad;
    	if(str.indexOf("スタッフ") != -1 || str.indexOf("運営") != -1) return Command.findStaff;
    	if(str.indexOf("おみくじ") != -1 || str.indexOf("運勢") != -1) return Command.Lucky;
    	if(str.indexOf("再起動") != -1) return Command.Reboot;
    	if(str.indexOf("やれ") != -1 || str.indexOf("おｋ") != -1 || str.indexOf("いいよ") != -1) return Command.Trigger;
    	if(str.indexOf("なし") != -1 || str.indexOf("嘘") != -1 || str.indexOf("中止") != -1) return Command.Cancel;
    	if(str.indexOf("えらい") != -1 || str.indexOf("あり") != -1 || str.indexOf("かしこい") != -1) return Command.Thanks;
    	
    	return Command.None;
    }
    
    public static String getNekoFace(){
    	return neko_face[random.nextInt(11)];
    }
}

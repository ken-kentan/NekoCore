package jp.kentan.minecraft.core;

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

public class Twitter {
	
	private NekoCore neko = null;
	private AsyncTwitter twitter;
	private TwitterStream twitterStream;
	
	public BotManager bot = null;
	
	public static String consumerKey       = "";
	public static String consumerSecret    = "";
	public static String accessToken       = "";
	public static String accessTokenSecret = "";
	
	private boolean isBotEnable    = true;
	
	public Twitter(NekoCore neko, ConfigManager config, EconomyManager eco){
		this.neko = neko;
		bot = new BotManager(neko, config, this, eco);
		
		twitter = new AsyncTwitterFactory().getInstance();
		twitter.setOAuthConsumer(consumerKey, consumerSecret);
		twitter.setOAuthAccessToken(new AccessToken(accessToken,accessTokenSecret));
		twitter.addListener(new TwitterAdapter() {
            @Override
            public void updatedStatus(Status status) {
                neko.getLogger().info("Async Tweet:" + status.getText());
            }

            @Override
            public void onException(TwitterException e, TwitterMethod method) {
            	neko.getLogger().warning("Async Tweet Failed " + e.getMessage());
            }
        });
		
		//Stream configuration
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
		
		neko.getLogger().info("Successfully initialized the Twitter Module.");
	}
	
	public void closeStream(){
		twitterStream.shutdown();
		neko.getLogger().info("Shutdown the TwitterStream.");
	}
	
	private final UserStreamListener listener = new UserStreamListener() {
		@Override
	    public void onStatus(Status status) {
	    	if(isReplay(status)){
	    		neko.getLogger().info("Twitter:Get replay from @" + status.getUser().getScreenName());
	    		
	    		bot.reaction(status);
	    	}
	    }

		@Override
		public void onFavorite(User source, User target, Status favoritedStatus) {
			if(isFavToMe(target)){
				neko.getLogger().info("Twitter:Get like from @" + source.getScreenName());
			
				bot.gacha.trigger(source, favoritedStatus);
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
		public void onException(Exception ex) {
			neko.getLogger().warning(ex.getMessage());
		}

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
	
	public void switchBotStatus(){
		isBotEnable = !isBotEnable;
		String msg = null;
		
		if(isBotEnable){
			msg = "Successfully enabled the Twitter Bot.";
		}else{
			msg = "Successfully disabled the Twitter Bot.";
		}
		neko.getLogger().info(msg);
	}
	
	public void tweet(String str){
		if(isBotEnable){
			twitter.updateStatus(str + " #猫鯖");
		}
	}
	
    public void reply(String user, String message, long statusId) {
    	if(isBotEnable){
    		twitter.updateStatus(new StatusUpdate("@" + user + " " + message).inReplyToStatusId(statusId));
    	}
    }
    
    public void sendDirectMessgae(String user, String str){
    	twitter.sendDirectMessage(user, str);
    	neko.getLogger().info("Twitter DM:Successfully sent to " + user);
    }
	
	private boolean isReplay(Status status){
		if(status.getText().indexOf("@DekitateServer") != -1 && !status.getUser().getScreenName().equals("DekitateServer")){
			return true;
		}
		return false;
	}
	
	public boolean isOwner(Status status){
		if(status.getUser().getScreenName().equals("ken_kentan")){
			return true;
		}
		return false;
	}
	
	private boolean isFavToMe(User targer){
		if(targer.getScreenName().equals("DekitateServer")){
			return true;
		}
		return false;
	}
}
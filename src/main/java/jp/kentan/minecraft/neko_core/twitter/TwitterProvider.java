package jp.kentan.minecraft.neko_core.twitter;


import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.TimeHandler;
import jp.kentan.minecraft.neko_core.twitter.bot.TwitterBot;
import jp.kentan.minecraft.neko_core.utils.Log;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.util.concurrent.ConcurrentLinkedQueue;

public class TwitterProvider {

    private AsyncTwitter mTwitter;
    private TwitterStream mStream;

    private Server mServer = Bukkit.getServer();

    private ConcurrentLinkedQueue<StatusUpdate> mStatusQueue = new ConcurrentLinkedQueue<>();

    private TwitterBot mBot;
    private TimeHandler mBotHandler;

    public TwitterProvider(Config config, TwitterBot.Messages messages){
        mTwitter = new AsyncTwitterFactory().getInstance();
        mTwitter.setOAuthConsumer(config.mConsumerKey, config.mConsumerSecret);
        mTwitter.setOAuthAccessToken(new AccessToken(config.mAccessToken, config.mAccessTokenSecret));
        mTwitter.addListener(new TwitterAdapter() {
            @Override
            public void updatedStatus(Status status) {
                mStatusQueue.poll();
                Log.print("Async Tweet:" + status.getText());
            }

            @Override
            public void onException(TwitterException e, TwitterMethod method) {
                Log.warn("Async Tweet Failed " + e.getMessage());
            }
        });

        mBot = new TwitterBot(this, messages);
        Bukkit.getServer().getPluginManager().registerEvents(mBot, NekoCore.getPlugin());
        mBotHandler = mBot;

        startTimer(NekoCore.getPlugin());

        // Stream configuration
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.setOAuthConsumerKey(config.mConsumerKey);
        builder.setOAuthConsumerSecret(config.mConsumerSecret);
        builder.setOAuthAccessToken(config.mAccessToken);
        builder.setOAuthAccessTokenSecret(config.mAccessTokenSecret);

        UserStreamListener streamListener = new UserStreamListener() {
            @Override
            public void onStatus(Status status) {
                if (isReplay(status)) {
                    Log.print("Twitter:Get replay from @" + status.getUser().getScreenName());
                    mBot.action(status);
                }
            }

            @Override
            public void onFavorite(User source, User target, Status favoritedStatus) {
                if (isFavToMe(target)) {
                    Log.print("Twitter:Get like from @" + source.getScreenName());
                }
            }

            @Override
            public void onDeletionNotice(long directMessageId, long userId) {
            }

            @Override
            public void onFriendList(long[] friendIds) {
            }

            @Override
            public void onUnfavorite(User source, User target, Status unfavoritedStatus) {
            }

            @Override
            public void onFollow(User source, User followedUser) {
            }

            @Override
            public void onUnfollow(User source, User unfollowedUser) {
            }

            @Override
            public void onDirectMessage(DirectMessage directMessage) {
            }

            @Override
            public void onUserListMemberAddition(User addedMember, User listOwner, UserList list) {
            }

            @Override
            public void onUserListMemberDeletion(User deletedMember, User listOwner, UserList list) {
            }

            @Override
            public void onUserListSubscription(User subscriber, User listOwner, UserList list) {
            }

            @Override
            public void onUserListUnsubscription(User subscriber, User listOwner, UserList list) {
            }

            @Override
            public void onUserListCreation(User listOwner, UserList list) {
            }

            @Override
            public void onUserListUpdate(User listOwner, UserList list) {
            }

            @Override
            public void onUserListDeletion(User listOwner, UserList list) {
            }

            @Override
            public void onUserProfileUpdate(User updatedUser) {
            }

            @Override
            public void onUserSuspension(long suspendedUser) {
            }

            @Override
            public void onUserDeletion(long deletedUser) {
            }

            @Override
            public void onBlock(User source, User blockedUser) {
            }

            @Override
            public void onUnblock(User source, User unblockedUser) {
            }

            @Override
            public void onRetweetedRetweet(User source, User target, Status retweetedStatus) {
            }

            @Override
            public void onFavoritedRetweet(User source, User target, Status favoritedRetweeet) {
            }

            @Override
            public void onQuotedTweet(User source, User target, Status quotingTweet) {
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
            }

            @Override
            public void onStallWarning(StallWarning warning) {
            }

            @Override
            public void onException(Exception ex) {
            }
        };

        Configuration streamConfig = builder.build();
        mStream = new TwitterStreamFactory(streamConfig).getInstance();
        mStream.addListener(streamListener);

        // start user Stream
//        mStream.user();
    }

    private void startTimer(Plugin plugin){
        mServer.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if(mStatusQueue.size() > 0){
                mTwitter.updateStatus(mStatusQueue.peek());
            }

            mBotHandler.timeHandler();
        }, 20*30L, 20*30L); //20ticks = 1sec
    }

    public void disable() {
//        mStream.shutdown();
    }

    public void tweet(String str){
        mStatusQueue.add(new StatusUpdate(str + " #猫鯖"));
    }

    public void reply(String user, String message, long statusId) {
        mStatusQueue.add(new StatusUpdate("@" + user + " " + message).inReplyToStatusId(statusId));
    }

    public void sendDirectMessage(String user, String message) {
        mTwitter.sendDirectMessage(user, message);
    }

    private boolean isReplay(Status status) {
        return status.getInReplyToScreenName().equals("DekitateServer") && !status.getUser().getScreenName().equals("DekitateServer");
    }

    private boolean isFavToMe(User user) {
        return user.getScreenName().equals("DekitateServer");
    }


    public static class Config{

        int mTweetInterval;

        String mConsumerKey, mConsumerSecret;
        String mAccessToken, mAccessTokenSecret;

        public Config(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret, int tweetInterval){
            mConsumerKey = consumerKey;
            mConsumerSecret = consumerSecret;
            mAccessToken = accessToken;
            mAccessTokenSecret = accessTokenSecret;

            mTweetInterval = tweetInterval;
        }
    }
}

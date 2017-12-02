//package jp.kentan.minecraft.neko_core.twitter;
//
//import jp.kentan.minecraft.neko_core.config.ConfigManager;
//import jp.kentan.minecraft.neko_core.config.ConfigUpdateListener;
//import jp.kentan.minecraft.neko_core.util.Log;
//import twitter4j.*;
//import twitter4j.auth.AccessToken;
//
//
//public class TwitterManager implements ConfigUpdateListener<TwitterManager.Config> {
//
//    private static TwitterManager sInstance = new TwitterManager();
//    private static AsyncTwitter sTwitter;
//
//    public static void setup(){
//        ConfigManager.bindTwitterConfigListener(sInstance);
//    }
//
//    static void tweet(String string){
//        sTwitter.updateStatus(new StatusUpdate(string));
//    }
//
//    public static void shutdown(){
//        if(sTwitter != null){
//            sTwitter.shutdown();
//        }
//    }
//
//    @Override
//    public void onUpdate(Config data) {
//        if(data == null) return;
//
//        shutdown();
//
//        sTwitter = new AsyncTwitterFactory().getInstance();
//        sTwitter.setOAuthConsumer(data.CONSUMER_KEY, data.CONSUMER_SECRET);
//        sTwitter.setOAuthAccessToken(data.ACCESS_TOKEN);
//        sTwitter.addListener(new TwitterAdapter() {
//            @Override
//            public void updatedStatus(Status status) {
//                Log.info("tweet:" + status.getText());
//            }
//
//            @Override
//            public void onException(TwitterException e, TwitterMethod method) {
//                Log.warn("tweet failed: " + e.getMessage());
//            }
//        });
//
//        Log.info("Twitter config updated.");
//    }
//
//
//    public static class Config{
//        String CONSUMER_KEY, CONSUMER_SECRET;
//        AccessToken ACCESS_TOKEN;
//
//        public Config(String consumerKey, String consumerSecret, String accessToken, String accessTokenSecret){
//            CONSUMER_KEY = consumerKey;
//            CONSUMER_SECRET = consumerSecret;
//            ACCESS_TOKEN = new AccessToken(accessToken, accessTokenSecret);
//        }
//    }
//}

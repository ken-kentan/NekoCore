package jp.kentan.minecraft.neko_core.twitter.bot;

import jp.kentan.minecraft.neko_core.TimeHandler;
import jp.kentan.minecraft.neko_core.twitter.TwitterProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import twitter4j.Status;

import java.util.*;
import java.util.regex.Pattern;


public class TwitterBot implements Listener, TimeHandler {

    private final int IGNORE_RELOGIN_SEC = 60 * 5;

    private final static Pattern SHIRITORI_PATTERN = Pattern.compile("しりとり|「.+」");

    private enum Command{NONE, PLAYERS, STAFF, REBOOT, TRIGGER, CANCEL,
        LUCKY, THANKS, MORNING, WEATHER, NYAN, GACHA, BALANCE, ASK_IN_GAME_PLAYER, DETACH, SHIRITORI}

    private static TwitterBot sInstance;

    private TwitterProvider mTwitter;
    private Messages mMessages;

    private Map<String, Integer> mLogoutPlayerList = Collections.synchronizedMap(new HashMap<>());
    private Map<String, Shiritori> mUserShiritoriMap = Collections.synchronizedMap(new HashMap<>());

    public TwitterBot(TwitterProvider twitter, Messages messages){
        sInstance = this;

        mTwitter = twitter;
        mMessages = messages;
    }

    public static TwitterBot getInstance(){
        if(sInstance == null) throw new NullPointerException("TwitterBot instance not found.");
        return sInstance;
    }

    @Override
    public void timeHandler() { //will call @ 30sec
        mLogoutPlayerList.replaceAll((key, val) -> val -= 30);
        mLogoutPlayerList.values().removeAll(Collections.singleton(0));
    }

    @EventHandler
    public void loginHandler(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();

        if(!mLogoutPlayerList.containsKey(playerName)) {
            tweetActionMessage(playerName, "ログイン");
        }
    }

    @EventHandler
    public void logoutHandler(PlayerQuitEvent event) {
        String playerName = event.getPlayer().getName();

        if(!mLogoutPlayerList.containsKey(playerName)) {
            tweetActionMessage(playerName, "ログアウト");
        }
        mLogoutPlayerList.put(event.getPlayer().getName(), IGNORE_RELOGIN_SEC);
    }

    public void action(Status status){
        final String userName = status.getUser().getScreenName();
        final long id = status.getId();
        final String text = status.getText();
        final Command command = getCommand(text);

        if(isAquatan(userName) && command != Command.SHIRITORI) return;

        switch (command) {
            case SHIRITORI:
                final Shiritori shiritori;

                if (mUserShiritoriMap.containsKey(userName)) {
                    shiritori = mUserShiritoriMap.get(userName);
                    shiritori.analyze(text);
                } else {
                    if (isAquatan(userName)) {
                        shiritori = new Shiritori(Shiritori.Result.CONTINUE, userName);
                        shiritori.analyze(text);
                    } else {
                        shiritori = new Shiritori(Shiritori.Result.NEW, userName);
                    }

                    mUserShiritoriMap.put(userName, shiritori);
                }

                mTwitter.reply(userName, getShiritoriMessage(shiritori.getResultStatus(), shiritori.getResultWord()), id);

                if (!shiritori.isContinue()) {
                    mUserShiritoriMap.remove(userName);
                }
                break;
            default:
                break;
        }
    }

    private Command getCommand(String text){
        if(SHIRITORI_PATTERN.matcher(text).find()){
            return Command.SHIRITORI;
        }

        return Command.NONE;
    }

    private boolean isAquatan(String userName){
        return userName.equals("sel_aquarium");
    }

    public void tweetActionMessage(String player, String status){
        String tweet = mMessages.getActionMessage();

        tweet = tweet.replace("{player}", player);
        tweet = tweet.replace("{status}", status);
        tweet = tweet.replace("{face}", mMessages.getNeko());

        mTwitter.tweet(tweet);
    }

    private String getShiritoriMessage(Shiritori.Result result, String word){
        String message = mMessages.getShiritoriMessage(result);

        message = message.replace("{face}", mMessages.getNeko());
        message = message.replace("{word}", "「" + word +"」");

        return message;
    }

    public String getNeko(){
        return mMessages.getNeko();
    }


    public static class Messages{

        private Random mRandom = new Random();

        private List<String> mNekoList = new ArrayList<>();
        private List<String> mActionList = new ArrayList<>();

        private List<String> mShiritoriStartList         = new ArrayList<>();
        private List<String> mShiritoriContinueList      = new ArrayList<>();
        private List<String> mShiritoriWinNList          = new ArrayList<>();
        private List<String> mShiritoriWinNotMatchList   = new ArrayList<>();
        private List<String> mShiritoriWinUsedList       = new ArrayList<>();
        private List<String> mShiritoriLoseList          = new ArrayList<>();

        public Messages(List<String> nekoFace, List<String> actionMsg, List<String> shiritoriStartMsg, List<String> shiritoriContinueMsg,
                        List<String> shiritoriWinNMsg, List<String> shiritoriWinNotMatchMsg, List<String> shiritoriWinUsedMsg, List<String> shiritoriLoseMsg){
            mNekoList.addAll(nekoFace);
            mActionList.addAll(actionMsg);

            mShiritoriStartList.addAll(shiritoriStartMsg);
            mShiritoriContinueList.addAll(shiritoriContinueMsg);
            mShiritoriWinNList.addAll(shiritoriWinNMsg);
            mShiritoriWinNotMatchList.addAll(shiritoriWinNotMatchMsg);
            mShiritoriWinUsedList.addAll(shiritoriWinUsedMsg);
            mShiritoriLoseList.addAll(shiritoriLoseMsg);
        }

        String getNeko(){
            return mNekoList.get(mRandom.nextInt(mNekoList.size()));
        }
        String getActionMessage(){
            return mActionList.get(mRandom.nextInt(mActionList.size()));
        }

        String getShiritoriMessage(Shiritori.Result result){
            switch (result){
                case NEW:
                    return mShiritoriStartList.get(mRandom.nextInt(mShiritoriStartList.size()));
                case LOSE:
                    return mShiritoriLoseList.get(mRandom.nextInt(mShiritoriLoseList.size()));
                case WIN_N:
                    return mShiritoriWinNList.get(mRandom.nextInt(mShiritoriWinNList.size()));
                case WIN_USED:
                    return mShiritoriWinUsedList.get(mRandom.nextInt(mShiritoriWinUsedList.size()));
                case WIN_NOT_MATCH:
                    return mShiritoriWinNotMatchList.get(mRandom.nextInt(mShiritoriWinNotMatchList.size()));
                default:
                    return mShiritoriContinueList.get(mRandom.nextInt(mShiritoriContinueList.size()));
            }
        }
    }
}

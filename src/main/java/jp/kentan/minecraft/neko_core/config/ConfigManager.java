package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.vote.RewardManager;
import jp.kentan.minecraft.neko_core.sql.SqlProvider;
import jp.kentan.minecraft.neko_core.twitter.TwitterProvider;
import jp.kentan.minecraft.neko_core.twitter.bot.TwitterBot;
import jp.kentan.minecraft.neko_core.utils.Log;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConfigManager {

    private final Charset UTF_8 = StandardCharsets.UTF_8;

    private String mConfigFilePath;

    private PlayerConfig mPlayerConfig;
    private SpawnConfig mSpawnConfig;

    private TwitterProvider.Config mTwitterConfig;
    private TwitterBot.Messages mBotMessages;

    private RewardManager.Config mRewardConfig;

    private static SqlProvider.Config sSqlConfig;

    public ConfigManager(File folder){
        mPlayerConfig = new PlayerConfig(folder);
        mSpawnConfig = new SpawnConfig(folder);

        mConfigFilePath = folder + File.separator + "config.yml";
    }

    public void load(){
        try (Reader reader = new InputStreamReader(new FileInputStream(mConfigFilePath), UTF_8)) {
            final FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            loadTwitterConfig(config);
            loadBotMessages(config);
            loadRewardConfig(config);
            loadSqlConfig(config);

            reader.close();
        }catch (Exception e){
            Log.warn(e.getMessage());
        }
    }

    private void loadTwitterConfig(FileConfiguration config){
        final String consumerKey       = config.getString("Twitter.consumerKey");
        final String consumerSecret    = config.getString("Twitter.consumerSecret");
        final String accessToken       = config.getString("Twitter.accessToken");
        final String accessTokenSecret = config.getString("Twitter.accessTokenSecret");
        final int tweetInterval        = config.getInt("Twitter.tweetInterval");

        mTwitterConfig = new TwitterProvider.Config(consumerKey, consumerSecret, accessToken, accessTokenSecret, tweetInterval);
    }

    private void loadBotMessages(FileConfiguration config){
        final List<String> nekoFace          = config.getStringList("Bot.nekoFace");
        final List<String> msgPlayerAction   = config.getStringList("Bot.msgPlayerAction");
        final List<String> msgUnknownCommand = config.getStringList("Bot.msgUnknownCommand");
        final List<String> msgRejectCommand  = config.getStringList("Bot.msgRejectCommand");
        final List<String> msgThanks         = config.getStringList("Bot.msgThanks");
        final List<String> msgLucky          = config.getStringList("Bot.msgLucky");
        final List<String> msgGoodMorning    = config.getStringList("Bot.msgGoodMorning");
        final List<String> msgWeather        = config.getStringList("Bot.msgWeather");
        final List<String> msgNyan           = config.getStringList("Bot.msgNyan");
        final List<String> msgGachaMiss      = config.getStringList("Bot.msgGachaMiss");
        final List<String> msgAskYes         = config.getStringList("Bot.msgAskYes");
        final List<String> msgAskNo          = config.getStringList("Bot.msgAskNo");

        final List<String> msgShiritoriNewList         = config.getStringList("Bot.msgShiritoriNew");
        final List<String> msgShiritoriContinueList    = config.getStringList("Bot.msgShiritoriContinue");
        final List<String> msgShiritoriWinNList        = config.getStringList("Bot.msgShiritoriWinN");
        final List<String> msgShiritoriWinNotMatchList = config.getStringList("Bot.msgShiritoriWinNotMatch");
        final List<String> msgShiritoriWinUsedList     = config.getStringList("Bot.msgShiritoriWinUsed");
        final List<String> msgShiritoriLoseList        = config.getStringList("Bot.msgShiritoriLose");

        mBotMessages = new TwitterBot.Messages(nekoFace, msgPlayerAction, msgShiritoriNewList, msgShiritoriContinueList,
                msgShiritoriWinNList, msgShiritoriWinNotMatchList, msgShiritoriWinUsedList, msgShiritoriLoseList);
    }

    private void loadRewardConfig(FileConfiguration config){
        final int maxSuccession = config.getInt("Vote.maxSuccession");

        final List<List<String>> rewardList = new ArrayList<>();
        final List<String> rewardDetailList = config.getStringList("Vote.Reward.Detail");

        for (int i = 1; i <= maxSuccession; ++i) {
            rewardList.add(config.getStringList("Vote.Reward." + i + "day"));
        }

        mRewardConfig = new RewardManager.Config(mPlayerConfig, maxSuccession, rewardList, rewardDetailList);
    }

    private void loadSqlConfig(FileConfiguration config){
        final String host     = config.getString("SQL.host");
        final String id       = config.getString("SQL.id");
        final String password = config.getString("SQL.pass");

        sSqlConfig = new SqlProvider.Config(host, id, password);
    }

    public TwitterProvider.Config getTwitterConfig(){
        return mTwitterConfig;
    }

    public TwitterBot.Messages getBotMessages(){
        return mBotMessages;
    }

    public RewardManager.Config getRewardConfig(){
        return mRewardConfig;
    }

    public SpawnConfig getSpawnConfig()
    {
        return mSpawnConfig;
    }
    public static SqlProvider.Config getSqlConfig(){
        return sSqlConfig;
    }
}

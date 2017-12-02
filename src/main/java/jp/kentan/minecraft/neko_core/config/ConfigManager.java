package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.vote.reward.Reward;
import jp.kentan.minecraft.neko_core.vote.reward.RewardManager;
import jp.kentan.minecraft.neko_core.util.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class ConfigManager {

    private static String sConfigPath;


//    private static ConfigUpdateListener<TwitterManager.Config> sTwitterConfigListener;
//    private static ConfigUpdateListener<TwitterBot.Messages> sBotMessagesListener;
    private static ConfigUpdateListener<String> sTutorialKeywordListener;
    private static ConfigUpdateListener<RewardManager.Config> sRewardConfigListener;


    public static void setup(File dataFolder){
        sConfigPath = dataFolder + File.separator + "config.yml";

        PlayerConfigProvider.setup(dataFolder);
        SpawnConfigProvider.setup(dataFolder);
    }

//    public static void bindTwitterConfigListener(ConfigUpdateListener<TwitterManager.Config> listener){
//        sTwitterConfigListener = listener;
//    }
//
//    public static void bindBotMessagesListener(ConfigUpdateListener<TwitterBot.Messages> listener){
//        sBotMessagesListener = listener;
//    }

    public static void bindTutorialKeywordListener(ConfigUpdateListener<String> listener){
        sTutorialKeywordListener = listener;
    }

    public static void bindRewardConfigListener(ConfigUpdateListener<RewardManager.Config> listener){
        sRewardConfigListener = listener;
    }

    public static void load(){
        try (Reader reader = new InputStreamReader(new FileInputStream(sConfigPath), StandardCharsets.UTF_8)) {
            final FileConfiguration config = new YamlConfiguration();

            config.load(reader);

//            loadTwitterConfig(config);
//            loadBotMessages(config);
            loadRewardConfig(config);

            sTutorialKeywordListener.onUpdate(
                    config.getString("Tutorial.keyword")
            );

            reader.close();
        }catch (Exception e){
            Log.warn(e.getMessage());
        }
    }

//    private static void loadTwitterConfig(FileConfiguration config){
//        sTwitterConfigListener.onUpdate(new TwitterManager.Config(
//                config.getString("Twitter.consumerKey"),
//                config.getString("Twitter.consumerSecret"),
//                config.getString("Twitter.accessToken"),
//                config.getString("Twitter.accessTokenSecret")
//        ));
//    }
//
//    private static void loadBotMessages(FileConfiguration config){
//        sBotMessagesListener.onUpdate(new TwitterBot.Messages(
//                config.getStringList("Bot.nekoFace"),
//                config.getStringList("Bot.msgPlayerAction")
//        ));
//    }

    private static void loadRewardConfig(FileConfiguration config){
        final List<Reward> rewardList = new ArrayList<>();

        for(int i=1; i<10; ++i){
            final String path = "Vote.Reward." + i + "day";

            if(!config.isConfigurationSection(path)) break;

            rewardList.add(new Reward(
                    config.getString(path + ".name"),
                    config.getStringList(path + ".commands")
            ));
        }

        sRewardConfigListener.onUpdate(new RewardManager.Config(
                rewardList
        ));
    }
}

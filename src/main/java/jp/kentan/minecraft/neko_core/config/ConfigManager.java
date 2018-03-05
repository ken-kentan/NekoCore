package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.component.ServerVoteReward;
import jp.kentan.minecraft.neko_core.component.SpawnLocation;
import jp.kentan.minecraft.neko_core.event.ConfigUpdateEvent;
import jp.kentan.minecraft.neko_core.util.Log;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ConfigManager {

    private final JavaPlugin JAVA_PLUGIN;

    /*
    ConfigProvider
     */
    private PlayerConfigProvider mPlayerConfigProvider;
    private SpawnConfigProvider mSpawnConfigProvider;

    /*
    ConfigUpdateEvent
     */
    private ConfigUpdateEvent<List<ServerVoteReward>> mServerVoteRewardUpdateEvent = null;
    private ConfigUpdateEvent<String> mTutorialKeywordUpdateEvent;


    public ConfigManager(JavaPlugin javaPlugin) {
        JAVA_PLUGIN = javaPlugin;

        mPlayerConfigProvider = new PlayerConfigProvider(JAVA_PLUGIN.getDataFolder());
        mSpawnConfigProvider  = new SpawnConfigProvider(JAVA_PLUGIN.getDataFolder());
    }

    public void bindServerVoteRewardEvent(ConfigUpdateEvent<List<ServerVoteReward>> event) {
        mServerVoteRewardUpdateEvent = event;
    }

    public void bindTutorialKeywordEvent(ConfigUpdateEvent<String> event) {
        mTutorialKeywordUpdateEvent = event;
    }

    public void bindSpawnConfigEvent(ConfigUpdateEvent<List<SpawnLocation>> event) {
        mSpawnConfigProvider.bindEvent(event);
    }

    public PlayerConfigProvider getPlayerConfigProvider() {
        return mPlayerConfigProvider;
    }

    public SpawnConfigProvider getSpawnConfigProvider() {
        return mSpawnConfigProvider;
    }

    public boolean reload() {
        JAVA_PLUGIN.reloadConfig();
        return load();
    }

    public boolean load() {
        FileConfiguration config = JAVA_PLUGIN.getConfig();

        loadServerVoteConfig(config);
        loadTutorialKeyword(config);
        mSpawnConfigProvider.load();

        return true;
    }

    private void loadServerVoteConfig(FileConfiguration config) {
        if (mServerVoteRewardUpdateEvent == null) {
            Log.warn("ServerVoteRewardUpdateEvent was not bound.");
            return;
        }

        List<ServerVoteReward> rewardList = new ArrayList<>();

        for (int i = 1; i < 10; ++i) {
            final String path = "Vote.Reward." + i + "day";

            if (!config.isConfigurationSection(path)) break;

            rewardList.add(
                    new ServerVoteReward(
                            config.getString(path + ".name"),
                            config.getStringList(path + ".commands")
                    )
            );
        }

        mServerVoteRewardUpdateEvent.onConfigUpdate(rewardList);
    }

    private void loadTutorialKeyword(FileConfiguration config) {
        if (mTutorialKeywordUpdateEvent == null) {
            Log.warn("TutorialKeywordUpdateEvent was not bound.");
            return;
        }

        mTutorialKeywordUpdateEvent.onConfigUpdate(
                config.getString("Tutorial.keyword")
        );
    }
}

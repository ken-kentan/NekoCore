package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.component.ServerVoteReward;
import jp.kentan.minecraft.neko_core.component.SpawnLocation;
import jp.kentan.minecraft.neko_core.event.ConfigUpdateEvent;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.manager.ServerVoteManager;
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
    private ConfigUpdateEvent<ServerVoteManager.Config> mServerVoteConfigUpdateEvent = null;


    public ConfigManager(JavaPlugin javaPlugin) {
        JAVA_PLUGIN = javaPlugin;

        mPlayerConfigProvider = new PlayerConfigProvider(JAVA_PLUGIN.getDataFolder());
        mSpawnConfigProvider  = new SpawnConfigProvider(JAVA_PLUGIN.getDataFolder());
    }

    public void bindServerVoteEvent(ConfigUpdateEvent<ServerVoteManager.Config> event) {
        mServerVoteConfigUpdateEvent = event;
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
        mSpawnConfigProvider.load();

        return true;
    }

    private void loadServerVoteConfig(FileConfiguration config) {
        if (mServerVoteConfigUpdateEvent == null) {
            Log.warn("ServerVoteConfigListener was not bound.");
            return;
        }


        for (int i = 1; i < 10; ++i) {
            final String path = "Vote.Reward." + i + "day";

            if (!config.isConfigurationSection(path)) break;

            List<ServerVoteReward> rewardList = new ArrayList<>();

            rewardList.add(
                    new ServerVoteReward(
                            config.getString(path + ".name"),
                            config.getStringList(path + ".commands")
                    )
            );

            mServerVoteConfigUpdateEvent.onConfigUpdate(
                    new ServerVoteManager.Config(mPlayerConfigProvider, rewardList)
            );
        }
    }
}

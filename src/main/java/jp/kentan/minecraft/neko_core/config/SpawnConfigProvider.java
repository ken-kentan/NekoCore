package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.component.SpawnLocation;
import jp.kentan.minecraft.neko_core.event.ConfigUpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class SpawnConfigProvider extends BaseConfig {

    private ConfigUpdateEvent<List<SpawnLocation>> mSpawnConfigUpdateEvent;

    SpawnConfigProvider(File dataFolder) {
        super.mConfigFile = new File(dataFolder, "spawn.yml");
    }

    void bindEvent(ConfigUpdateEvent<List<SpawnLocation>> event) {
        mSpawnConfigUpdateEvent = event;
    }

    public void load() {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(super.mConfigFile);

            Set<String> spawnSet = config.getConfigurationSection("").getKeys(false);

            List<SpawnLocation> spawnList = new ArrayList<>();

            spawnSet.forEach(name -> {
                String world = config.getString(name + ".world");
                double x     = config.getDouble(name + ".x");
                double y     = config.getDouble(name + ".y");
                double z     = config.getDouble(name + ".z");
                float yaw    = (float)config.getDouble(name + ".yaw");
                float pitch  = (float)config.getDouble(name + ".pitch");

                spawnList.add(new SpawnLocation(name, new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch)));
            });

            mSpawnConfigUpdateEvent.onConfigUpdate(spawnList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean save(String name, Location location) {
        return super.save(new LinkedHashMap<String, Object>(){
            {
                put(name + ".world", location.getWorld().getName());
                put(name + ".x"    , location.getX());
                put(name + ".y"    , location.getY());
                put(name + ".z"    , location.getZ());
                put(name + ".yaw"  , location.getYaw());
                put(name + ".pitch", location.getPitch());
            }
        });
    }
}

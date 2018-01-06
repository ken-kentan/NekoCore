package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.spawn.SpawnLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SpawnConfigProvider {

    private static File sFile;
    private static ConfigUpdateListener<List<SpawnLocation>> sUpdateListener;

    static void setup(File dataFolder){
        sFile = new File(dataFolder, "spawn.yml");
    }

    public static void setListener(ConfigUpdateListener<List<SpawnLocation>> listener){
        sUpdateListener = listener;
    }

    public static void load() {
        try (Reader reader = new InputStreamReader(new FileInputStream(sFile), StandardCharsets.UTF_8)) {

            FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            reader.close();

            Set<String> spawnSet = config.getConfigurationSection("").getKeys(false);

            List<SpawnLocation> spawnList = new ArrayList<>();

            spawnSet.forEach(name -> {
                String worldName = config.getString(name + ".world");
                double x = config.getDouble(name + ".x");
                double y = config.getDouble(name + ".y");
                double z = config.getDouble(name + ".z");
                float yaw = (float)config.getDouble(name + ".yaw");
                float pitch = (float)config.getDouble(name + ".pitch");

                spawnList.add(new SpawnLocation(name, new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch)));
            });

            sUpdateListener.onUpdate(spawnList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean save(String spawnName, Location location) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(sFile);

            config.set(spawnName + ".world", location.getWorld().getName());
            config.set(spawnName + ".x", location.getX());
            config.set(spawnName + ".y", location.getY());
            config.set(spawnName + ".z", location.getZ());
            config.set(spawnName + ".yaw", location.getYaw());
            config.set(spawnName + ".pitch", location.getPitch());

            config.save(sFile);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}

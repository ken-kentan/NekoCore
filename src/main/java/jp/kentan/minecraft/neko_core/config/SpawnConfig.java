package jp.kentan.minecraft.neko_core.config;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class SpawnConfig {

    private final Charset UTF_8 = StandardCharsets.UTF_8;

    private File mFile;
    private String mFilePath;

    SpawnConfig(File folder){
        mFile = new File(folder, "spawn.yml");
        mFilePath = folder + File.separator + "spawn.yml";
    }

    public Location load(String spawnName) {
        try (Reader reader = new InputStreamReader(new FileInputStream(mFilePath), UTF_8)) {

            FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            reader.close();

            String worldName = config.getString(spawnName + ".world");
            double x = config.getDouble(spawnName + ".x");
            double y = config.getDouble(spawnName + ".y");
            double z = config.getDouble(spawnName + ".z");
            float yaw = (float)config.getDouble(spawnName + ".yaw");
            float pitch = (float)config.getDouble(spawnName + ".pitch");

            return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean save(String spawnName, Location location) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(mFile);

            config.set(spawnName + ".world", location.getWorld().getName());
            config.set(spawnName + ".x", location.getX());
            config.set(spawnName + ".y", location.getY());
            config.set(spawnName + ".z", location.getZ());
            config.set(spawnName + ".yaw", location.getYaw());
            config.set(spawnName + ".pitch", location.getPitch());

            config.save(mFile);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}

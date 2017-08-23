package jp.kentan.minecraft.neko_core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class PlayerConfig {

    private final Charset UTF_8 = StandardCharsets.UTF_8;

    private File mFile;
    private String mFilePath;

    PlayerConfig(File folder){
        mFile = new File(folder, "player.yml");
        mFilePath = folder + File.separator + "player.yml";
    }

    public int load(String path, int ifNull) {
        try (Reader reader = new InputStreamReader(new FileInputStream(mFilePath), UTF_8)) {

            FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            reader.close();

            return config.getInt(path);
        } catch (Exception e) {
            return ifNull;
        }
    }

    public String load(String path) {
        try (Reader reader = new InputStreamReader(new FileInputStream(mFilePath), UTF_8)) {

            FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            reader.close();

            return config.getString(path);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean save(String path, Object data) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(mFile);

            config.set(path, data);

            config.save(mFile);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}

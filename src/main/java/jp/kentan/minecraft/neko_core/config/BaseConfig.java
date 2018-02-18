package jp.kentan.minecraft.neko_core.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract class BaseConfig {

    File mConfigFile = null;

    Object get(String path, Object def) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(mConfigFile);

            return config.get(path, def);
        } catch (Exception e) {
            return def;
        }
    }

    List<String> getStringList(String path) {
        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(mConfigFile);

            return config.getStringList(path);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    boolean save(Map<String, Object> dataMap) {
        if (mConfigFile == null) {
           return false;
        }

        try {
            FileConfiguration config = new YamlConfiguration();
            config.load(mConfigFile);

            dataMap.forEach(config::set);

            config.save(mConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

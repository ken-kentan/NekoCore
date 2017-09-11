package jp.kentan.minecraft.neko_core.config;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_core.utils.Log;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;


public class ZoneConfigProvider {
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private static String sFolderPath;

    static void setup(File dataFolder){
        sFolderPath = dataFolder + File.separator + "zones" + File.separator;
    }

    public static boolean setRate(World world, float rate){
        return save(world, new HashMap<String, Object>(){
            {
                put("rate", rate);
            }
        });
    }

    public static boolean register(World world, ProtectedRegion area, String name, int size) {
        String id = area.getId();

        return save(world, new HashMap<String, Object>(){
            {
                put("Zone." + id + ".name", name);
                put("Zone." + id + ".size", size);
            }
        });
    }

    private static boolean save(World world, Map<String, Object> dataList) {
        final File file = new File(sFolderPath + world.getName() + ".yml");

        try {
            if(!file.exists()){
                file.createNewFile();
            }

            Log.print(dataList.toString());

            FileConfiguration config = new YamlConfiguration();
            config.load(file);

            dataList.forEach(config::set);

            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

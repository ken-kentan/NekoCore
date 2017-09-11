package jp.kentan.minecraft.neko_core.config;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.zone.component.Area;
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
import java.util.*;


public class ZoneConfigProvider {
    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private static String sFolderPath;

    static void setup(File dataFolder){
        sFolderPath = dataFolder + File.separator + "zones" + File.separator;
    }

    public static void setWorldConfig(String nameWorld, double rate, double rateGain, int ownerLimit){
        save(nameWorld, new HashMap<String, Object>(){
            {
                put("rate", rate);
                put("rateGain", rateGain);
                put("ownerLimit", ownerLimit);
            }
        });
    }
    
    public static void registerOwner(String nameWorld, Player player, String nameArea){
        PlayerConfigProvider.addToArray(player.getUniqueId(), "OwnerArea." + nameWorld, nameArea);

        save(nameWorld, new HashMap<String, Object>(){
            {
                put("Area." + nameArea + ".owner", player.getUniqueId().toString());
                put("Area." + nameArea + ".onSale", false);
            }
        });
    }

    public static boolean register(String nameWorld, String nameArea, ProtectedRegion area, int size) {
        return save(nameWorld, new HashMap<String, Object>(){
            {
                put("Area." + nameArea + ".id", area.getId());
                put("Area." + nameArea + ".size", size);
                put("Area." + nameArea + ".owner", null);
                put("Area." + nameArea + ".onSale", true);
            }
        });
    }

    private static boolean save(String nameWorld, String path, Object data) {
        final File file = new File(sFolderPath + nameWorld + ".yml");

        try {
            if(!file.exists()){
                file.createNewFile();
            }

            FileConfiguration config = new YamlConfiguration();
            config.load(file);

            config.set(path, data);

            config.save(file);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static boolean save(String nameWorld, Map<String, Object> dataList) {
        final File file = new File(sFolderPath + nameWorld + ".yml");

        try {
            if(!file.exists()){
                file.createNewFile();
            }

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

    public static Object get(String nameWorld, String path, Object def) {
        try (Reader reader = new InputStreamReader(new FileInputStream(sFolderPath + nameWorld + ".yml"), UTF_8)) {

            FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            reader.close();

            return config.get(path, def);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Map<String, Object> get(String nameWorld, List<String> pathList) {
        try (Reader reader = new InputStreamReader(new FileInputStream(sFolderPath + nameWorld + ".yml"), UTF_8)) {

            FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            reader.close();

            Map<String, Object> dataMap = new HashMap<>();

            pathList.forEach(p -> dataMap.put(p, config.get(p)));

            return dataMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Area getArea(String nameWorld, String nameArea) {
        try (Reader reader = new InputStreamReader(new FileInputStream(sFolderPath + nameWorld + ".yml"), UTF_8)) {

            FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            reader.close();

            if(config.contains("Area." + nameArea)) {
                String strUuid = config.getString("Area." + nameArea + ".owner");

                return new Area(
                        nameWorld,
                        nameArea,
                        config.getString("Area." + nameArea + ".id"),
                        (strUuid != null) ? UUID.fromString(strUuid) : null,
                        config.getInt("Area." + nameArea + ".size"),
                        config.getBoolean("Area." + nameArea + ".onSale")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

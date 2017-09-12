package jp.kentan.minecraft.neko_core.config;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_core.zone.component.Area;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    
    public static void registerOwner(String nameWorld, UUID owner, String nameArea){
        PlayerConfigProvider.addToArray(owner, "OwnerArea." + nameWorld, nameArea);

        final String PATH = "Area." + nameArea;

        save(nameWorld, new HashMap<String, Object>(){
            {
                put(PATH + ".owner", owner.toString());
                put(PATH + ".onSale", false);
            }
        });
    }

    public static void removeOwner(String nameWorld, UUID owner, String nameArea){
        PlayerConfigProvider.removeFromArray(owner, "OwnerArea." + nameWorld, nameArea);

        final String PATH = "Area." + nameArea;

        save(nameWorld, new HashMap<String, Object>(){
            {
                put(PATH + ".owner", null);
                put(PATH + ".onSale", false);
            }
        });
    }

    public static boolean register(String nameWorld, String nameArea, ProtectedRegion area, int size) {
        final String PATH = "Area." + nameArea;

        return save(nameWorld, new HashMap<String, Object>(){
            {
                put(PATH + ".id", area.getId());
                put(PATH + ".size", size);
                put(PATH + ".owner", null);
                put(PATH + ".onSale", true);
            }
        });
    }

    public static boolean setSign(String nameWorld, String nameArea, Location location) {
        final String PATH = "Area." + nameArea;

        return save(nameWorld, new HashMap<String, Object>(){
            {
                if(location == null){
                    put(PATH + ".sign", null);
                }else {
                    put(PATH + ".sign.x", location.getX());
                    put(PATH + ".sign.y", location.getY());
                    put(PATH + ".sign.z", location.getZ());
                }
            }
        });
    }

    public static void setOnSale(String nameWorld, String nameArea, boolean onSale){
        save(nameWorld, "Area." + nameArea + ".onSale", onSale);
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

            final String PATH = "Area." + nameArea;

            if(config.contains(PATH)) {
                String strUuid = config.getString(PATH + ".owner");

                Location signLocation = null;

                if(config.contains(PATH + ".sign")){
                    signLocation = new Location(Bukkit.getWorld(nameWorld),
                            config.getDouble(PATH + ".sign.x", 0D),
                            config.getDouble(PATH + ".sign.y", 0D),
                            config.getDouble(PATH + ".sign.z", 0D)
                    );
                }

                return new Area(
                        nameWorld,
                        nameArea,
                        config.getString(PATH + ".id"),
                        (strUuid != null) ? UUID.fromString(strUuid) : null,
                        config.getInt(PATH + ".size"),
                        config.getBoolean(PATH + ".onSale"),
                        signLocation
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static List<Area> getAreaList(String nameWorld) {
        final File file = new File(sFolderPath + nameWorld + ".yml");

        if(!file.exists()){
            return null;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), UTF_8)) {

            FileConfiguration config = new YamlConfiguration();

            config.load(reader);

            reader.close();

            List<Area> areaList = new ArrayList<>();

            Set<String> areaSet = config.getConfigurationSection("Area").getKeys(false);

            areaSet.forEach(nameArea -> {
                final String PATH = "Area." + nameArea;

                String strUuid = config.getString(PATH + ".owner");

                Location signLocation = null;

                if(config.contains(PATH + ".sign")){
                    signLocation = new Location(Bukkit.getWorld(nameWorld),
                            config.getDouble(PATH + ".sign.x", 0D),
                            config.getDouble(PATH + ".sign.y", 0D),
                            config.getDouble(PATH + ".sign.z", 0D)
                    );
                }

                areaList.add(new Area(
                        nameWorld,
                        nameArea,
                        config.getString(PATH + ".id"),
                        (strUuid != null) ? UUID.fromString(strUuid) : null,
                        config.getInt(PATH + ".size"),
                        config.getBoolean(PATH + ".onSale"),
                        signLocation
                ));
            });

            return areaList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

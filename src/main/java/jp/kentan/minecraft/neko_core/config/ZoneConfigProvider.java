package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.zone.component.Area;
import jp.kentan.minecraft.neko_core.zone.component.AreaUpdateListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class ZoneConfigProvider implements AreaUpdateListener {

    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private static String sFolderPath;

    private final static Map<World, Map<String, Area>> sWorldAreaCacheMap = Collections.synchronizedMap(new HashMap<>());


    public ZoneConfigProvider(File dataFolder){
        sFolderPath = dataFolder + File.separator + "zones" + File.separator;

        Bukkit.getScheduler().runTaskAsynchronously(NekoCore.getPlugin(), this::createCache);
    }

    private void createCache(){
        Bukkit.getWorlds().forEach(world -> {
            final File file = new File(sFolderPath + world.getName() + ".yml");

            if(file.exists()){
                try (Reader reader = new InputStreamReader(new FileInputStream(file), UTF_8)) {
                    FileConfiguration config = new YamlConfiguration();
                    config.load(reader);
                    reader.close();

                    Map<String, Area> areaMap = Collections.synchronizedMap(new HashMap<>());
                    Set<String> areaSet = config.getConfigurationSection("Area").getKeys(false);

                    areaSet.forEach(name -> {
                        final String PATH = "Area." + name;

                        String strUuid = config.getString(PATH + ".owner");

                        Location signLocation = null;

                        if(config.contains(PATH + ".sign")){
                            signLocation = new Location(world,
                                    config.getDouble(PATH + ".sign.x"),
                                    config.getDouble(PATH + ".sign.y"),
                                    config.getDouble(PATH + ".sign.z")
                            );
                        }

                        areaMap.put(name, new Area(
                                this,
                                world,
                                name,
                                config.getString(PATH + ".id"),
                                (strUuid != null) ? UUID.fromString(strUuid) : null,
                                config.getInt(PATH + ".size"),
                                config.getBoolean(PATH + ".onSale"),
                                signLocation
                        ));
                    });

                    sWorldAreaCacheMap.put(world, areaMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Log.print("World area cache created with async.");

        Bukkit.getScheduler().scheduleSyncDelayedTask(NekoCore.getPlugin(), this::refresh);

    }

    @Override
    public void onUpdate(Area area) {
        //非同期で保存
        Bukkit.getScheduler().runTaskAsynchronously(NekoCore.getPlugin(), () -> {
            final String PATH = "Area." + area.getName();
            final Location SIGN = area.getSignLocation();
            final UUID OWNER = area.getOwnerUuid();

            sWorldAreaCacheMap.get(area.getWorld()).put(area.getName(), area);

            save(area.getWorldName(), new HashMap<String, Object>(){
                {
                    put(PATH + ".id", area.getId());
                    put(PATH + ".size", area.getSize());
                    put(PATH + ".owner", (OWNER != null) ? OWNER.toString() : null);
                    put(PATH + ".onSale", area.onSale());

                    if(SIGN == null){
                        put(PATH + ".sign", null);
                    }else {
                        put(PATH + ".sign.x", SIGN.getX());
                        put(PATH + ".sign.y", SIGN.getY());
                        put(PATH + ".sign.z", SIGN.getZ());
                    }
                }
            });
        });
    }

    public Area getArea(World world, String name){
        //キャッシュから探す
        Area area = sWorldAreaCacheMap.get(world).get(name);

        if(area != null){
            return area;
        }

        //見つからなかった場合、ファイルから探す
        final File file = new File(sFolderPath + world.getName() + ".yml");

        if(!file.exists()){
            return null;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), UTF_8)) {
            FileConfiguration config = new YamlConfiguration();
            config.load(reader);
            reader.close();

            final String PATH = "Area." + name;

            String strUuid = config.getString(PATH + ".owner");

            Location signLocation = null;

            if(config.contains(PATH + ".sign")){
                signLocation = new Location(world,
                        config.getDouble(PATH + ".sign.x"),
                        config.getDouble(PATH + ".sign.y"),
                        config.getDouble(PATH + ".sign.z")
                );
            }

            area = new Area(
                    this,
                    world,
                    name,
                    config.getString(PATH + ".id"),
                    (strUuid != null) ? UUID.fromString(strUuid) : null,
                    config.getInt(PATH + ".size"),
                    config.getBoolean(PATH + ".onSale"),
                    signLocation
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        //キャッシュに追加
        sWorldAreaCacheMap.get(world).put(name, area);

        return area;
    }

    public void refresh(){
        sWorldAreaCacheMap.forEach((w, m) -> m.forEach((n, a) -> a.updateSign()));
    }

    public void setWorldConfig(String nameWorld, double rate, double rateGain, int ownerLimit){
        save(nameWorld, new HashMap<String, Object>(){
            {
                put("rate", rate);
                put("rateGain", rateGain);
                put("ownerLimit", ownerLimit);
            }
        });
    }

    public void register(World world, String name, String id, int size) {
        new Area(
                this,
                world,
                name,
                id,
                null,
                size,
                true,
                null
        ).save();
    }

    private boolean save(String nameWorld, Map<String, Object> dataList) {
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
}

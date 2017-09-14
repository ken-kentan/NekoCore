package jp.kentan.minecraft.neko_core.config;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.utils.comparator.WorldComparator;
import jp.kentan.minecraft.neko_core.zone.component.Area;
import jp.kentan.minecraft.neko_core.zone.component.AreaUpdateListener;
import jp.kentan.minecraft.neko_core.zone.component.WorldParam;
import jp.kentan.minecraft.neko_core.zone.component.WorldParamUpdateListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import java.util.stream.Collectors;


public class ZoneConfigProvider implements AreaUpdateListener, WorldParamUpdateListener {

    private final static Charset UTF_8 = StandardCharsets.UTF_8;

    private static String sFolderPath;

    private final static Map<World, WorldParam> sWorldParamCacheMap = Collections.synchronizedMap(new HashMap<>());
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

                    if(config.isConfigurationSection("Area")) {
                        Set<String> areaSet = config.getConfigurationSection("Area").getKeys(false);

                        areaSet.forEach(name -> {
                            final String PATH = "Area." + name;

                            String strUuid = config.getString(PATH + ".owner");

                            Location signLocation = null;

                            if (config.contains(PATH + ".sign")) {
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
                                    config.getDouble(PATH + ".purchasedPrice", -1D),
                                    config.getInt(PATH + ".size"),
                                    config.getBoolean(PATH + ".onSale"),
                                    signLocation
                            ));
                        });
                    }

                    boolean enablePurchaseRuleMessage = config.getBoolean("Purchase.ruleMessageEnabled", false);
                    boolean enableSellRuleMessage     = config.getBoolean("Sell.ruleMessageEnabled", false);

                    sWorldParamCacheMap.put(world, new WorldParam(
                            this,
                            world,
                            config.getInt("ownerLimit"),
                            config.getDouble("Purchase.rate"),
                            config.getDouble("Purchase.rateGain"),
                            enablePurchaseRuleMessage ? config.getStringList("Purchase.ruleMessage") : null,
                            config.getDouble("Sell.rate"),
                            enableSellRuleMessage ? config.getStringList("Sell.ruleMessage") : null
                    ));

                    sWorldAreaCacheMap.put(world, areaMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Log.print("WorldParamCacheMap has been created async.");
        Log.print("WorldAreaCacheMap has been created async.");

        Bukkit.getScheduler().scheduleSyncDelayedTask(NekoCore.getPlugin(), () -> sWorldAreaCacheMap.forEach((w, m) -> m.forEach((n, a) -> a.updateSign())));

    }

    public void refresh(){
        createCache();
    }

    @Override
    public void onUpdate(Area area) {
        //非同期で保存
        Bukkit.getScheduler().runTaskAsynchronously(NekoCore.getPlugin(), () -> {
            final String PATH = "Area." + area.getName();
            final Location SIGN = area.getSignLocation();
            final UUID OWNER = area.getOwnerUuid();

            final World WORLD = area.getWorld();

            if(sWorldAreaCacheMap.containsKey(WORLD)){
                if(area.getId() != null) {
                    sWorldAreaCacheMap.get(WORLD).put(area.getName(), area);
                }else{
                    sWorldAreaCacheMap.get(WORLD).remove(area.getName());
                }
            }else{
                sWorldAreaCacheMap.put(WORLD, new HashMap<String, Area>(){
                    {
                        put(area.getName(), area);
                    }
                });
            }


            save(area.getWorldName(), new HashMap<String, Object>(){
                {
                    if(area.getId() != null) {
                        put(PATH + ".id", area.getId());
                        put(PATH + ".size", area.getSize());
                        put(PATH + ".owner", (OWNER != null) ? OWNER.toString() : null);
                        put(PATH + ".purchasedPrice", area.getPurchasedPrice());
                        put(PATH + ".onSale", area.onSale());

                        if (SIGN == null) {
                            put(PATH + ".sign", null);
                        } else {
                            put(PATH + ".sign.x", SIGN.getX());
                            put(PATH + ".sign.y", SIGN.getY());
                            put(PATH + ".sign.z", SIGN.getZ());
                        }
                    }else{ //消去
                        put(PATH, null);
                    }
                }
            });
            Log.print("Area data has been saved async.");
        });
    }

    @Override
    public void onUpdate(WorldParam param) {
        //非同期で保存
        Bukkit.getScheduler().runTaskAsynchronously(NekoCore.getPlugin(), () -> {
            sWorldParamCacheMap.put(param.getWorld(), param);

            save(param.getWorldName(), new HashMap<String, Object>() {
                {
                    put("ownerLimit", param.getOwnerLimit());
                    put("Purchase.rate", param.getPurchaseRate());
                    put("Purchase.rateGain", param.getPurchaseRateGain());
                    put("Sell.rate", param.getSellRate());
                }
            });
            Log.print("WorldParam data has been saved async.");
        });
    }

    public Area getArea(World world, String name){
        Area area;

        //キャッシュから探す
        if(sWorldAreaCacheMap.containsKey(world)) {
            area = sWorldAreaCacheMap.get(world).get(name);

            if (area != null) {
                return area;
            }
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

            if(!config.contains(PATH)){
                return null;
            }

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
                    config.getDouble(PATH + ".purchasedPrice", -1D),
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

    public static Map<World, List<Area>> getOwnerAreaMap(UUID owner){
        Map<World, List<Area>> ownerAreaMap = new HashMap<>();

        Bukkit.getWorlds()
                .parallelStream()
                .sorted(WorldComparator.getInstance())
                .forEach(world -> {
            if(sWorldAreaCacheMap.containsKey(world)){
                List<Area> areaList = sWorldAreaCacheMap.get(world)
                        .values()
                        .parallelStream()
                        .sorted()
                        .filter(a -> a.isOwner(owner))
                        .collect(Collectors.toList());

                if(areaList != null && areaList.size() > 0){
                    ownerAreaMap.put(world, new ArrayList<>());
                    ownerAreaMap.get(world).addAll(areaList);
                }
            }
        });

        return ownerAreaMap;
    }

    public WorldParam getWorldParam(World world){
        //キャッシュから探す
        WorldParam param = sWorldParamCacheMap.get(world);

        if(param != null){
            return param;
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

            boolean enablePurchaseRuleMessage = config.getBoolean("Purchase.ruleMessageEnabled", false);
            boolean enableSellRuleMessage = config.getBoolean("Sell.ruleMessageEnabled", false);

            param = new WorldParam(
                    this,
                    world,
                    config.getInt("ownerLimit"),
                    config.getDouble("Purchase.rate"),
                    config.getDouble("Purchase.rateGain"),
                    enablePurchaseRuleMessage ? config.getStringList("Purchase.ruleMessage") : null,
                    config.getDouble("Sell.rate"),
                    enableSellRuleMessage ? config.getStringList("Sell.ruleMessage") : null
            );
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        //キャッシュに追加
        sWorldParamCacheMap.put(world, param);

        return param;
    }

    public static List<WorldParam> getWorldParamList(){
        if(sWorldParamCacheMap == null){
            return Collections.emptyList();
        }

        return new ArrayList<>(sWorldParamCacheMap.values());
    }

    public static int getTotalOwnerNumber(Player owner){
        return getTotalOwnerNumber(owner.getUniqueId(), owner.getWorld());
    }

    public static int getTotalOwnerNumber(UUID owner, World world){
        try {
            return  (int) sWorldAreaCacheMap.get(world).entrySet().parallelStream().filter(map -> map.getValue().isOwner(owner)).count();
        }catch (Exception e){
            return 0;
        }
    }

    public void register(World world, String name, String id, int size) {
        new Area(
                this,
                world,
                name,
                id,
                null,
                -1D,
                size,
                true,
                null
        ).save();
    }

    private void save(String nameWorld, Map<String, Object> dataList) {
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
        }
    }
}

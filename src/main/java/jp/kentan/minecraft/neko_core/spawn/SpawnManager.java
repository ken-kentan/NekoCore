package jp.kentan.minecraft.neko_core.spawn;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.ConfigUpdateListener;
import jp.kentan.minecraft.neko_core.config.SpawnConfigProvider;
import jp.kentan.minecraft.neko_core.spawn.listener.CancelListener;
import jp.kentan.minecraft.neko_core.spawn.listener.SpawnCancelListener;
import jp.kentan.minecraft.neko_core.util.Log;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpawnManager implements CancelListener, ConfigUpdateListener<List<SpawnLocation>> {

    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();
    private static Plugin sPlugin;

    private static Map<String, Location> sSpawnLocationMap = new HashMap<>();
    private static Map<Player, Integer> sPlayerSpawnTaskMap = new HashMap<>();

    public static void setup(JavaPlugin plugin){
        sPlugin = plugin;

        final SpawnManager instance = new SpawnManager();

        SpawnConfigProvider.setListener(instance);

        plugin.getServer().getPluginManager().registerEvents(new SpawnCancelListener(instance), plugin);

        SpawnCommandExecutor executor = new SpawnCommandExecutor();
        plugin.getCommand("spawn").setExecutor(executor);
        plugin.getCommand("setspawn").setExecutor(executor);
    }

    public static boolean spawn(Player player, String locationName){
        if(!sSpawnLocationMap.containsKey(locationName)){
            return false;
        }

        player.teleport(sSpawnLocationMap.get(locationName));
        return true;
    }

    static void addSpawnTask(Player player, String locationName){

        if(!sSpawnLocationMap.containsKey(locationName)){
            player.sendMessage(NekoCore.PREFIX + ChatColor.YELLOW + "そのようなスポーンは存在しません.");
            return;
        }

        final Location location = sSpawnLocationMap.get(locationName);
        Location playerLocation = player.getLocation();

        player.sendMessage(NekoCore.PREFIX + "3秒後にスポーンします.");
        player.getWorld().playEffect(playerLocation, Effect.MOBSPAWNER_FLAMES, 6);
        player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f);

        if(sPlayerSpawnTaskMap.containsKey(player)){
            SCHEDULER.cancelTask(sPlayerSpawnTaskMap.remove(player));
        }

        int taskId = SCHEDULER.scheduleSyncDelayedTask(sPlugin, () ->{
            sPlayerSpawnTaskMap.remove(player);
            player.teleport(location);
        }, 20*3L);

        sPlayerSpawnTaskMap.put(player, taskId);
    }

    static void saveSpawn(Player player, String spawnName){
        final Location location = player.getLocation();

        if(!SpawnConfigProvider.save(spawnName, location)){
            player.sendMessage(NekoCore.PREFIX + ChatColor.RED + "保存に失敗しました.");
            return;
        }

        sSpawnLocationMap.put(spawnName, location);
        player.sendMessage(NekoCore.PREFIX + "スポーン(" + spawnName + ")を、ここにセットしました.");
    }

    @Override
    public void onCancel(Player player) {
        if(sPlayerSpawnTaskMap.containsKey(player)) {
            SCHEDULER.cancelTask(sPlayerSpawnTaskMap.remove(player));
            player.sendMessage(NekoCore.PREFIX + "スポーンをキャンセルしました.");
        }
    }

    @Override
    public void onUpdate(List<SpawnLocation> data) {
        if(data == null){
            return;
        }

        sSpawnLocationMap.clear();

        data.forEach(spawn -> sSpawnLocationMap.put(spawn.getName(), spawn.getLocation()));

        Log.info("SpawnMap updated.");
    }
}

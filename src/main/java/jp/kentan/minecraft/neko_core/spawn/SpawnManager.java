package jp.kentan.minecraft.neko_core.spawn;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.SpawnConfigProvider;
import jp.kentan.minecraft.neko_core.spawn.listener.CancelListener;
import jp.kentan.minecraft.neko_core.spawn.listener.SpawnCancelListener;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;

public class SpawnManager implements CancelListener {

    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();
    private static Plugin sPlugin;

    private static Map<Player, Integer> sPlayerSpawnTaskMap = new HashMap<>();

    public static void setup(JavaPlugin plugin){
        sPlugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(new SpawnCancelListener(new SpawnManager()), plugin);

        SpawnCommandExecutor executor = new SpawnCommandExecutor();
        plugin.getCommand("spawn").setExecutor(executor);
        plugin.getCommand("setspawn").setExecutor(executor);
    }

    public static boolean spawn(Player player, String locationName){
        Location location = SpawnConfigProvider.load(locationName);

        if(location == null){
            return false;
        }

        player.teleport(location);
        return true;
    }

    static void addSpawnTask(Player player, String locationName){
        Location location = SpawnConfigProvider.load(locationName);

        if(location != null){
            Location playerLocation = player.getLocation();

            player.sendMessage(NekoCore.PREFIX + "3秒後にスポーンします.");
            player.getWorld().playEffect(playerLocation, Effect.MOBSPAWNER_FLAMES, 6);
            player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f);
        }else{
            player.sendMessage(NekoCore.PREFIX + "そのようなスポーンは存在しません.");
            return;
        }

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
        SpawnConfigProvider.save(spawnName, player.getLocation());

        player.sendMessage(NekoCore.PREFIX + "スポーン(" + spawnName + ")を、ここにセットしました.");
    }

    @Override
    public void onCancel(Player player) {
        if(sPlayerSpawnTaskMap.containsKey(player)) {
            SCHEDULER.cancelTask(sPlayerSpawnTaskMap.remove(player));
            player.sendMessage(NekoCore.PREFIX + "スポーンをキャンセルしました.");
        }
    }
}

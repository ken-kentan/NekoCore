package jp.kentan.minecraft.neko_core.spawn;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.SpawnConfig;
import jp.kentan.minecraft.neko_core.spawn.listener.CancelListener;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;

public class SpawnManager implements CancelListener {

    private Plugin mPlugin;
    private BukkitScheduler mScheduler;

    private SpawnConfig mConfig;
    private Map<Player, Integer> mPlayerSpawnTaskMap = new HashMap<>();

    public SpawnManager(Plugin plugin, SpawnConfig config){
        mPlugin = plugin;
        mScheduler = plugin.getServer().getScheduler();

        mConfig = config;
    }

    void addSpawnTask(Player player, String locationName){
        Location location = mConfig.load(locationName);
        if(location != null){
            Location playerLocation = player.getLocation();

            player.sendMessage(NekoCore.TAG + "3秒後にスポーンします.");
            player.getWorld().playEffect(playerLocation, Effect.MOBSPAWNER_FLAMES, 6);
            player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f);
        }else{
            player.sendMessage(NekoCore.TAG + "そのようなスポーンは存在しません.");
            return;
        }

        if(mPlayerSpawnTaskMap.containsKey(player)){
            mScheduler.cancelTask(mPlayerSpawnTaskMap.remove(player));
        }

        int taskId = mScheduler.scheduleSyncDelayedTask(mPlugin, () ->{
            mPlayerSpawnTaskMap.remove(player);
            player.teleport(location);
        }, 20*3L);

        mPlayerSpawnTaskMap.put(player, taskId);
    }

    void saveSpawn(Player player, String spawnName){
        mConfig.save(spawnName, player.getLocation());

        player.sendMessage(NekoCore.TAG + "スポーン(" + spawnName + ")を、ここにセットしました.");
    }

    @Override
    public void onCancel(Player player) {
        if(mPlayerSpawnTaskMap.containsKey(player)) {
            mScheduler.cancelTask(mPlayerSpawnTaskMap.remove(player));
            player.sendMessage(NekoCore.TAG + "スポーンをキャンセルしました.");
        }
    }
}

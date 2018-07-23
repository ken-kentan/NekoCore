package jp.kentan.minecraft.neko_core.manager;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.component.SpawnLocation;
import jp.kentan.minecraft.neko_core.event.ConfigUpdateEvent;
import jp.kentan.minecraft.neko_core.config.SpawnConfigProvider;
import jp.kentan.minecraft.neko_core.event.SpawnCancelEvent;
import jp.kentan.minecraft.neko_core.util.Log;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;

public class SpawnManager implements SpawnCancelEvent, ConfigUpdateEvent<List<SpawnLocation>> {

    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final Plugin PLUGIN;
    private final SpawnConfigProvider SPAWN_CONFIG;

    private Map<String, Location> SPAWN_MAP = new HashMap<>();
    private Map<Player, Integer> SPAWN_TASK_MAP = new HashMap<>();
    private List<String> SPAWN_NAME_LIST = new ArrayList<>();

    public SpawnManager(Plugin plugin, SpawnConfigProvider spawnConfigProvider) {
        PLUGIN = plugin;
        SPAWN_CONFIG = spawnConfigProvider;
    }

    public List<String> getSpawnNameList() {
        return SPAWN_NAME_LIST;
    }

    public boolean spawn(Player player, String spawnName) {
        if (!SPAWN_MAP.containsKey(spawnName)) {
            return false;
        }

        player.teleport(SPAWN_MAP.get(spawnName));
        return true;
    }

    public void addSpawnTask(Player player, String spawnName) {
        if (SPAWN_MAP.size() <= 0 || !SPAWN_MAP.containsKey(spawnName)) {
            player.sendMessage(NekoCore.PREFIX + ChatColor.YELLOW + "そのようなスポーンは存在しません.");
            return;
        }

        final Location location = SPAWN_MAP.get(spawnName);
        Location playerLocation = player.getLocation();

        player.sendMessage(NekoCore.PREFIX + "3秒後にスポーンします.");
        player.getWorld().playEffect(playerLocation, Effect.MOBSPAWNER_FLAMES, 6);
        player.playSound(playerLocation, Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f);

        if (SPAWN_TASK_MAP.containsKey(player)) {
            SCHEDULER.cancelTask(SPAWN_TASK_MAP.remove(player));
        }

        int taskId = SCHEDULER.scheduleSyncDelayedTask(PLUGIN, () -> {
            if (!player.isOnline()) {
                return;
            }

            SPAWN_TASK_MAP.remove(player);
            player.teleport(location);
        }, 60L);

        SPAWN_TASK_MAP.put(player, taskId);
    }

    public void saveSpawn(Player player, String spawnName) {
        final Location location = player.getLocation();

        if(!SPAWN_CONFIG.save(spawnName, location)){
            player.sendMessage(NekoCore.PREFIX + ChatColor.RED + "保存に失敗しました.");
            return;
        }

        SPAWN_MAP.put(spawnName, location);

        if (!spawnName.equals("default")) {
            SPAWN_NAME_LIST.add(spawnName);
            Collections.sort(SPAWN_NAME_LIST);
        }

        player.sendMessage(NekoCore.PREFIX + "スポーン(" + spawnName + ")を、ここにセットしました.");
    }

    @Override
    public void onSpawnCancel(Player player) {
        if(SPAWN_TASK_MAP.containsKey(player)) {
            SCHEDULER.cancelTask(SPAWN_TASK_MAP.remove(player));
            player.sendMessage(NekoCore.PREFIX + "スポーンをキャンセルしました.");
        }
    }

    @Override
    public void onConfigUpdate(List<SpawnLocation> data) {
        SPAWN_MAP.clear();
        SPAWN_NAME_LIST.clear();

        data.forEach(spawn -> {
            SPAWN_MAP.put(spawn.NAME, spawn.LOCATION);

            if(!spawn.NAME.equals("default")) {
                SPAWN_NAME_LIST.add(spawn.NAME);
            }
        });

        Collections.sort(SPAWN_NAME_LIST);

        Log.info("SpawnMap updated.");
    }
}

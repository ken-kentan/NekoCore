package jp.kentan.minecraft.neko_core.listener;

import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.event.AdvertisementEvent;
import jp.kentan.minecraft.neko_core.event.SpawnCancelEvent;
import jp.kentan.minecraft.neko_core.event.ZoneEvent;
import jp.kentan.minecraft.neko_core.manager.RankManager;
import jp.kentan.minecraft.neko_core.manager.TutorialManager;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.List;

public class BukkitEventListener implements Listener {

    private final static ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();
    private final static String ZONE_SIGN_INDEX = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + ChatColor.BOLD + "区画" + ChatColor.DARK_GRAY + ChatColor.BOLD + "]";

    private final Plugin PLUGIN;
    private final BukkitScheduler SCHEDULER;
    private final PlayerConfigProvider PLAYER_CONFIG;

    private final TutorialManager TUTORIAL_MANAGER;
    private final RankManager RANK_MANAGER;

    private final SpawnCancelEvent SPAWN_CANCEL_EVENT;
    private final ZoneEvent ZONE_EVENT;
    private final AdvertisementEvent AD_EVENT;

    public BukkitEventListener(
            Plugin plugin,
            PlayerConfigProvider playerConfigProvider,
            TutorialManager tutorialManager,
            RankManager rankManager,
            SpawnCancelEvent spawnCancelEvent,
            ZoneEvent zoneEvent,
            AdvertisementEvent advertisementEvent) {

        PLUGIN        = plugin;
        SCHEDULER     = plugin.getServer().getScheduler();
        PLAYER_CONFIG = playerConfigProvider;

        TUTORIAL_MANAGER = tutorialManager;
        RANK_MANAGER     = rankManager;

        SPAWN_CANCEL_EVENT = spawnCancelEvent;
        ZONE_EVENT = zoneEvent;
        AD_EVENT = advertisementEvent;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        TUTORIAL_MANAGER.teleportIfNeed(player);
        RANK_MANAGER.update(player);
        ZONE_EVENT.onPlayerJoin(player);
        AD_EVENT.onPlayerJoin(player);

        List<String> stackCommandList = PLAYER_CONFIG.getStackCommandList(player.getUniqueId());

        if(stackCommandList == null || stackCommandList.size() <= 0) {
            return;
        }

        SCHEDULER.scheduleSyncDelayedTask(PLUGIN, () -> {
            if(!player.isOnline()){
                return;
            }

            final String playerName = player.getName();

            stackCommandList.forEach(cmd -> Bukkit.getServer().dispatchCommand(CONSOLE, cmd.replace("{player}", playerName)));

            PLAYER_CONFIG.save(player.getUniqueId(), new HashMap<String, Object>(){
                {
                    put("stackCommands", null);
                }
            });
        }, 20L * 5);
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        AD_EVENT.onPlayerQuit(event.getPlayer());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        Location from = event.getFrom();

        if((to.getBlockX() != from.getBlockX()) || (to.getBlockY() != from.getBlockY()) || (to.getBlockZ() != from.getBlockZ())) {
            SPAWN_CANCEL_EVENT.onSpawnCancel(event.getPlayer());
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        SPAWN_CANCEL_EVENT.onSpawnCancel(event.getPlayer());
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (Util.isPlayer(entity)) {
            SPAWN_CANCEL_EVENT.onSpawnCancel((Player)entity);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChanged(SignChangeEvent event) {
        if(event.getPlayer().hasPermission("neko.zone.moderator") && event.getLine(0).equals("z")){
            ZONE_EVENT.onSignPlace(event);
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final BlockState blockState = event.getBlock().getState();

        if(blockState instanceof Sign && event.getPlayer().hasPermission("neko.zone.moderator")){
            Sign sign = (Sign)blockState;

            if(sign.getLine(0).contains(ZONE_SIGN_INDEX)) {
                ZONE_EVENT.onSignBreak(event.getPlayer(), sign);
            }
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final BlockState blockState = event.getClickedBlock().getState();

        if(blockState instanceof Sign){
            Sign sign = (Sign)blockState;

            if(sign.getLine(0).contains(ZONE_SIGN_INDEX)){
                if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    ZONE_EVENT.onSignClick(event.getPlayer(), sign);
                }else if(!event.getPlayer().hasPermission("neko.zone.moderator")){
                    event.setCancelled(true);
                }
            }
        }
    }
}

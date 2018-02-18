package jp.kentan.minecraft.neko_core.manager;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.bridge.EconomyProvider;
import jp.kentan.minecraft.neko_core.component.WeatherState;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WeatherVoteManager {

    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final Plugin PLUGIN;
    private final EconomyProvider ECONOMY;

    private List<Player> mVotedPlayerList = Collections.synchronizedList(new ArrayList<>());
    private VoteTask mVoteTask = null;

    public WeatherVoteManager(Plugin plugin, EconomyProvider economyProvider) {
        PLUGIN = plugin;
        ECONOMY = economyProvider;
    }

    public void vote(Player player) {
        if (mVoteTask == null) {
            player.sendMessage(NekoCore.PREFIX + ChatColor.YELLOW + "天候投票は開始されていません.");
            return;
        }

        if (mVotedPlayerList.contains(player)) {
            player.sendMessage(NekoCore.PREFIX + ChatColor.YELLOW + "すでに投票しています.");
            sendInfo(player);
            return;
        }

        mVotedPlayerList.add(player);

        int voteThreshold = Bukkit.getOnlinePlayers().size() / 3;

        if(mVotedPlayerList.size() >= voteThreshold){
            World world = mVoteTask.PLAYER.getWorld();

            switch (mVoteTask.WEATHER_STATE) {
                case SUN:
                    world.setStorm(false);
                    world.setThundering(false);
                    break;
                case RAIN:
                    world.setStorm(true);
                    break;
            }

            Bukkit.broadcastMessage(NekoCore.PREFIX + "投票の結果、天候を" + mVoteTask.WEATHER_STATE.getName() + "にしました.");

            stopVoteTask();
        }else{
            player.sendMessage(NekoCore.PREFIX + ChatColor.AQUA + "天候投票に成功しました！");
            sendInfo(player);
        }
    }

    public void sendInfo(Player player) {
        if (mVoteTask == null) {
            player.sendMessage(NekoCore.PREFIX + ChatColor.YELLOW + "天候投票は開始されていません.");
            return;
        }

        player.sendMessage(NekoCore.PREFIX + "投票主： " + mVoteTask.PLAYER.getDisplayName());
        player.sendMessage(NekoCore.PREFIX + "投票数：" + ChatColor.AQUA + " " + mVotedPlayerList.size() + "人");
        player.sendMessage(NekoCore.PREFIX + "残り時間：" + ChatColor.GREEN + " " + mVoteTask.mTimerSec.get() + "秒");
        player.sendMessage(NekoCore.PREFIX + ChatColor.GRAY + "ログインプレイヤーの約1/3が投票すると天候が" + mVoteTask.WEATHER_STATE.getName() + ChatColor.GRAY + "になります。");
    }

    public void startVoteTask(Player player, WeatherState weather) {
        if (mVoteTask != null) {
            player.sendMessage(NekoCore.PREFIX + ChatColor.YELLOW + "現在、天候投票中です.");
            return;
        }

        if (ECONOMY.withdraw(player, 100D)) {
            player.sendMessage(NekoCore.PREFIX + ChatColor.GREEN + " \u00A5100 を支払いました.");
        } else {
            player.sendMessage(NekoCore.PREFIX + ChatColor.YELLOW + "所持金が不足しています.");
            return;
        }

        mVoteTask = new VoteTask(player, weather);

        mVoteTask.mId = SCHEDULER.runTaskTimerAsynchronously(PLUGIN, () -> {
            if(mVoteTask.mTimerSec.decrementAndGet() < 0){
                stopVoteTask();
            }
        }, 20L, 20L).getTaskId(); //20ticks = 1sec

        vote(player);

        Util.broadcast(NekoCore.PREFIX + player.getDisplayName() + "が天候投票（" + weather.getName() + "）を開始しました.", player);
        Util.broadcast(NekoCore.PREFIX + ChatColor.GRAY + "/weathervote で投票に参加します.", player);
    }

    private void stopVoteTask() {
        SCHEDULER.cancelTask(mVoteTask.mId);

        mVoteTask = null;

        mVotedPlayerList.clear();

        Log.info("WeatherVoteTask stopped.");
    }

    private class VoteTask {
        private final Player PLAYER;
        private final WeatherState WEATHER_STATE;

        private int mId;
        private AtomicInteger mTimerSec = new AtomicInteger(300);

        private VoteTask(Player player, WeatherState weatherState) {
            PLAYER = player;
            WEATHER_STATE = weatherState;
        }
    }
}

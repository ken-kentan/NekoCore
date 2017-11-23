package jp.kentan.minecraft.neko_core.vote;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.NekoUtil;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WeatherVote {

    private static int sTaskId;
    private static int sCountSec = 300;

    private static Plugin sPlugin;
    private static Server sServer;
    private static List<Player> sVotedPlayerList =  Collections.synchronizedList(new ArrayList<>());

    public static void setup(Plugin plugin){
        sPlugin = plugin;
        sServer = plugin.getServer();
    }

    public static void vote(Player player){
        if(sVotedPlayerList.contains(player)){ //多重投票
            sendStatus(player);
            return;
        }


        final int currentOnlinePlayer = sServer.getOnlinePlayers().size();

        startTimerIfNeed(player);

        sVotedPlayerList.add(player);

        if(sVotedPlayerList.size() >= currentOnlinePlayer / 2){

            sVotedPlayerList.forEach(p -> {
                p.getWorld().setStorm(false);
                p.getWorld().setThundering(false);
            });

            sServer.broadcastMessage(NekoCore.TAG + ChatColor.AQUA + "投票の結果、天候を晴れにしました。");

            stopTimer();
        }else{
            player.sendMessage(NekoCore.TAG + ChatColor.AQUA + "天候投票に成功しました！");
            sendStatus(player);
        }
    }

    private static void startTimerIfNeed(Player player){
        if(sVotedPlayerList.size() > 0){
            return;
        }

        sCountSec = 300;

        sTaskId = sServer.getScheduler().runTaskTimer(sPlugin, () -> {
            if(--sCountSec < 0){
                stopTimer();
            }
        }, 20L, 20L).getTaskId(); //20ticks = 1sec

        NekoUtil.broadcast(NekoCore.TAG + player.getDisplayName() + "さんが天候投票を開始しました。", player);

        Log.info("WeatherVote start.");
    }

    private static void stopTimer(){
        sServer.getScheduler().cancelTask(sTaskId);

        sVotedPlayerList.clear();

        Log.info("WeatherVote stopped.");
    }

    private static void sendStatus(Player player){
        player.sendMessage(NekoCore.TAG + "現在の投票数：" + ChatColor.AQUA + " " + sVotedPlayerList.size() + "人");
        player.sendMessage(NekoCore.TAG + "残り投票時間：" + ChatColor.GREEN + " " + sCountSec + "秒");
        player.sendMessage(NekoCore.TAG + ChatColor.GRAY + "ログインプレイヤーの半数が投票すると天候が晴れになります。");
    }
}

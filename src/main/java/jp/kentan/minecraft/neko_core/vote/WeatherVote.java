package jp.kentan.minecraft.neko_core.vote;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.utils.Log;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WeatherVote {

    private int mTaskId;
    private int mCountSec = 300;

    private Plugin mPlugin;
    private Server mServer;
    private List<Player> mVotedPlayers =  Collections.synchronizedList(new ArrayList<>());

    public WeatherVote(Plugin plugin){
        mPlugin = plugin;
        mServer = plugin.getServer();
    }


    public void vote(Player player){
        if(mVotedPlayers.contains(player)){ //多重投票
            printStatus(player);
            return;
        }


        final int currentOnlinePlayer = mServer.getOnlinePlayers().size();

        startTimerIfNeed(player);

        mVotedPlayers.add(player);

        if(mVotedPlayers.size() >= currentOnlinePlayer / 2){

            mVotedPlayers.forEach(p -> {
                p.getWorld().setStorm(false);
                p.getWorld().setThundering(false);
            });

            final String broadcastMsg = NekoCore.TAG + ChatColor.AQUA + "投票の結果、天候を晴れにしました。";
            mServer.getOnlinePlayers().forEach(p -> p.sendMessage(broadcastMsg));

            cancel();
        }else{
            player.sendMessage(NekoCore.TAG + ChatColor.AQUA + "天候投票に成功しました！");
            printStatus(player);
        }
    }

    private void startTimerIfNeed(Player player){
        if(mVotedPlayers.size() > 0){
            return;
        }

        mCountSec = 300;

        mTaskId = mServer.getScheduler().runTaskTimer(mPlugin, () -> {
            if(--mCountSec < 0){
                cancel();
            }
        }, 20L, 20L).getTaskId(); //20ticks = 1sec

        final String playerName = player.getName();
        final String broadcastMsg = NekoCore.TAG + playerName + "さんが天候投票を開始しました。";

        final List<Player> onlinePlayers = new ArrayList<>(mServer.getOnlinePlayers());
        onlinePlayers.removeIf(p -> p.getName().contains(playerName));

        onlinePlayers.forEach(p -> p.sendMessage(broadcastMsg));

        Log.print("WeatherVote start.");
    }

    private void cancel(){
        mServer.getScheduler().cancelTask(mTaskId);

        mVotedPlayers.clear();

        Log.print("WeatherVote canceled.");
    }

    private void printStatus(Player player){
        player.sendMessage(NekoCore.TAG + "現在の投票数：" + ChatColor.AQUA + " " + mVotedPlayers.size() + "人");
        player.sendMessage(NekoCore.TAG + "残り投票時間：" + ChatColor.GREEN + " " + mCountSec + "秒");
        player.sendMessage(NekoCore.TAG + ChatColor.GRAY + "ログインプレイヤーの半数が投票すると天候が晴れになります。");
    }
}

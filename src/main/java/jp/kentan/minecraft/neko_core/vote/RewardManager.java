package jp.kentan.minecraft.neko_core.vote;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.twitter.bot.TwitterBot;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class RewardManager {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);

    private Config mConfig;

    private TwitterBot mBot = TwitterBot.getInstance();

    public RewardManager(Config config){
        mConfig = config;
    }

    void vote(String playerName){
        String[] broadcastMsgs = new String[]{
                NekoCore.TAG + playerName + ChatColor.YELLOW + " がサーバーに投票しました！",
                NekoCore.TAG + ChatColor.GRAY + "投票すると様々な" + ChatColor.GOLD + "特典" + ChatColor.GRAY + "を入手できます！",
                NekoCore.TAG + ChatColor.GRAY + "まだの方は、鯖主の為に投票してみませんか？",
                NekoCore.TAG + ChatColor.AQUA + "ｸﾘｯｸ！ https://minecraft.kentan.jp/vote"
        };

        mBot.tweetActionMessage(playerName, " http://minecraft.jp で投票");

        final Player player = NekoUtils.getOnlinePlayer(playerName);

        if (player == null) { // offline
            NekoUtils.broadcast(broadcastMsgs, null);

            Log.print(playerName + " voted as a OFFLINE.");

            Bukkit.getScheduler().runTaskAsynchronously(NekoCore.getPlugin(), () -> {
                UUID uuid = NekoUtils.getOfflinePlayerUuid(playerName);

                if(uuid != null){
                    mConfig.save(uuid, mConfig.getContinuous(uuid));
                }
            });
            return;
        }

        final UUID uuid = player.getUniqueId();

        int continuous = mConfig.getContinuous(uuid);

        runRewardCommand(playerName, continuous);

        mConfig.save(uuid, continuous);

        player.sendMessage(NekoCore.TAG + "投票ありがとにゃ" + mBot.getNeko() + ChatColor.AQUA + " " + continuous + "day" + ChatColor.GOLD + "ボーナス" + ChatColor.WHITE + "をゲット！");
        player.sendMessage(NekoCore.TAG + ChatColor.GOLD + "ボーナス" + ChatColor.WHITE + ": " + mConfig.mRewardDetailList.get(continuous - 1));
        player.sendMessage(NekoCore.TAG + "ステータス: " + generateContinuousMessage(continuous));
        player.sendMessage(NekoCore.TAG + ChatColor.GRAY + "1日1回、続けて投票するとボーナスがアップグレード！");

        NekoUtils.broadcast(broadcastMsgs, player);

        Log.print(playerName + " voted as a online.");
    }

    private void runRewardCommand(String playerName, int succession) {
        final Server server = Bukkit.getServer();
        final ConsoleCommandSender console = server.getConsoleSender();

        List<String> rewardList = mConfig.mRewardList.get(succession - 1);

        rewardList.forEach(cmd -> server.dispatchCommand(console, cmd.replace("{player}", playerName)));
    }

    private String generateContinuousMessage(int succession) {
        StringBuilder builder = new StringBuilder();

        final String star  = ChatColor.AQUA + "{day}day" + ChatColor.GRAY + "[" + ChatColor.YELLOW + "★" + ChatColor.GRAY + "] ";
        final String empty = ChatColor.DARK_GRAY + "{day}day[☆] ";

        for (int day = 1; day <= mConfig.mContinuousLimit; ++day) {
            String cell = (day <= succession) ? star : empty;

            builder.append(cell.replace("{day}", String.valueOf(day)));
        }

        return builder.toString();
    }


    public static class Config{
        private int mContinuousLimit;

        List<List<String>> mRewardList = new ArrayList<>();
        List<String> mRewardDetailList = new ArrayList<>();

        public Config(int maxSuccession, List<List<String>> rewardList, List<String> rewardDetailList){
            mContinuousLimit = maxSuccession;

            mRewardList.addAll(rewardList);
            mRewardDetailList.addAll(rewardDetailList);
        }

        private Date getDate(UUID uuid){
            Object date = PlayerConfigProvider.get(uuid, "Vote.date", null);

            if(date == null){
                return null;
            }

            try {
                return DATE_FORMAT.parse(date.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                return null;
            }
        }

        int getContinuous(UUID uuid){
            Date lastDate = getDate(uuid);
            int continuous = (int)PlayerConfigProvider.get(uuid, "Vote.continuous", 1);

            if(lastDate != null && diffDays(new Date(), lastDate) <= 1){
                return Math.min(continuous + 1, mContinuousLimit);
            }

            return 1;
        }

        void save(UUID uuid, int continuous){
            PlayerConfigProvider.save(uuid, new HashMap<String, Object>(){
                    {
                        put("Vote.date", DATE_FORMAT.format(new Date()));
                        put("Vote.continuous", continuous);
                    }
                }
            );
        }

        private static int diffDays(Date from, Date to) {
            return (int) TimeUnit.MILLISECONDS.toDays(from.getTime() - to.getTime());
        }
    }
}

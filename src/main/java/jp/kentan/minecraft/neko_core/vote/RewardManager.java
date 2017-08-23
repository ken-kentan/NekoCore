package jp.kentan.minecraft.neko_core.vote;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.PlayerConfig;
import jp.kentan.minecraft.neko_core.twitter.bot.TwitterBot;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
            mConfig.saveLastVoteDate(playerName);

            NekoUtils.broadcast(broadcastMsgs, null);

            Log.print(playerName + " voted as a OFFLINE.");
            return;
        }

        int succession = getSuccession(playerName);

        runRewardCommand(playerName, succession);

        mConfig.saveLastVoteDate(playerName);
        mConfig.saveVoteSuccession(playerName, succession);

        player.sendMessage(NekoCore.TAG + "投票ありがとにゃ" + mBot.getNeko() + ChatColor.AQUA + " " + succession + "day" + ChatColor.GOLD + "ボーナス" + ChatColor.WHITE + "をゲット！");
        player.sendMessage(NekoCore.TAG + ChatColor.GOLD + "ボーナス" + ChatColor.WHITE + ": " + mConfig.mRewardDetailList.get(succession - 1));
        player.sendMessage(NekoCore.TAG + "ステータス: " + generateSuccessionMessage(succession));
        player.sendMessage(NekoCore.TAG + ChatColor.GRAY + "1日1回、続けて投票するとボーナスがアップグレード！");

        NekoUtils.broadcast(broadcastMsgs, player);

        Log.print(playerName + " voted as a online.");
    }

    private void runRewardCommand(String playerName, int succession) {
        final Server server = NekoUtils.SERVER;
        final ConsoleCommandSender console = server.getConsoleSender();

        List<String> rewardList = mConfig.mRewardList.get(succession - 1);

        rewardList.forEach(cmd -> server.dispatchCommand(console, cmd.replace("{player}", playerName)));
    }

    private String generateSuccessionMessage(int succession) {
        StringBuilder builder = new StringBuilder();

        final String star  = ChatColor.AQUA + "{day}day" + ChatColor.GRAY + "[" + ChatColor.YELLOW + "★" + ChatColor.GRAY + "] ";
        final String empty = ChatColor.DARK_GRAY + "{day}day[☆] ";

        for (int day = 1; day <= mConfig.mSuccessionLimit; ++day) {
            String cell = (day <= succession) ? star : empty;

            builder.append(cell.replace("{day}", String.valueOf(day)));
        }

        return builder.toString();
    }

    private int getSuccession(String playerName){
        final Date lastVoteDate = mConfig.getLastVoteDate(playerName);

        if(lastVoteDate != null && differenceDays(new Date(), lastVoteDate) == 1){
            return Math.min(mConfig.getSuccession(playerName) + 1, mConfig.mSuccessionLimit);
        }

        return 1;
    }

    private int differenceDays(Date date1, Date date2) {
        long time1 = date1.getTime();
        long time2 = date2.getTime();
        long oneDateTime = 1000 * 60 * 60 * 24;
        long diffDays = (time1 - time2) / oneDateTime;
        return (int) diffDays;
    }


    public static class Config{

        private PlayerConfig mPlayerConfig;

        int mSuccessionLimit;

        List<List<String>> mRewardList = new ArrayList<>();
        List<String> mRewardDetailList = new ArrayList<>();

        public Config(PlayerConfig playerConfig, int maxSuccession, List<List<String>> rewardList, List<String> rewardDetailList){
            mPlayerConfig = playerConfig;

            mSuccessionLimit = maxSuccession;

            mRewardList.addAll(rewardList);
            mRewardDetailList.addAll(rewardDetailList);
        }

        Date getLastVoteDate(String playerName){
            String strDate = mPlayerConfig.load("Player." + playerName + ".Vote.LastDate");

            if(strDate == null){
                return null;
            }

            try {
                return DATE_FORMAT.parse(strDate);
            } catch (ParseException e) {
                Log.warn(e.getMessage());
                return null;
            }
        }

        int getSuccession(String playerName){
            return mPlayerConfig.load("Player." + playerName + ".Vote.Succession", 1);
        }

        void saveLastVoteDate(String playerName){
            mPlayerConfig.save("Player." + playerName + ".Vote.LastDate", DATE_FORMAT.format(new Date()));
        }

        void saveVoteSuccession(String playerName, int succession){
            mPlayerConfig.save("Player." + playerName + ".Vote.Succession", succession);
        }
    }
}

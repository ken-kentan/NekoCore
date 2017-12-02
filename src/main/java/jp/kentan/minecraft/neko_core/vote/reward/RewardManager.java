package jp.kentan.minecraft.neko_core.vote.reward;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.ConfigManager;
import jp.kentan.minecraft.neko_core.config.ConfigUpdateListener;
import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.NekoUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class RewardManager implements ConfigUpdateListener<RewardManager.Config> {

    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN);
    private final static ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();

    private static Config sConfig;


    public static void setup(){
        ConfigManager.bindRewardConfigListener(new RewardManager());
    }

    public static void vote(String playerName){
//        TwitterBot.pushActionMessage(playerName, " http://minecraft.jp で投票");

        final Player player = NekoUtil.getOnlinePlayer(playerName);
        final UUID uuid;

        boolean isaOnlineMode = (player != null);

        if(isaOnlineMode){ //Online
            uuid = player.getUniqueId();
        }else{
            uuid = NekoUtil.getOfflinePlayerUuid(playerName);
        }

        if(uuid == null){
            Log.error("Failed to vote about " + playerName);
            return;
        }


        final Reward reward = sConfig.getReward(uuid);
        final int continuous = sConfig.getContinuous(uuid);

        if(isaOnlineMode) {
            reward.getCommandList().forEach(cmd -> Bukkit.getServer().dispatchCommand(CONSOLE, cmd.replace("{player}", playerName)));

            final String[] playerMessages = PLAYER_MESSAGES.clone();
            playerMessages[1] = playerMessages[1].replace("{reward}", reward.NAME);
            playerMessages[2] = playerMessages[2].replace("{status}", buildStatusMessage(continuous));

            player.sendMessage(playerMessages);
        }else{
            sConfig.saveCommand(uuid, reward.getCommandList());
        }

        sConfig.saveContinuous(uuid, continuous);

        /*
        Broadcast
         */
        final String[] broadcastMessages = BROADCAST_MESSAGES.clone();
        broadcastMessages[0] = broadcastMessages[0].replace("{player}", playerName).replace("{reward}", reward.NAME);

        NekoUtil.broadcast(broadcastMessages, player);

        Log.info(playerName + " voted as " + (isaOnlineMode ? "ONLINE." : "offline."));
    }

    private static String buildStatusMessage(int succession) {
        StringBuilder sb = new StringBuilder();

        final String star  = ChatColor.AQUA + "{day}day" + ChatColor.GRAY + "[" + ChatColor.YELLOW + "★" + ChatColor.GRAY + "] ";
        final String empty = ChatColor.DARK_GRAY + "{day}day[☆] ";

        for (int day = 1; day <= sConfig.CONTINUOUS_LIMIT; ++day) {
            String cell = (day <= succession) ? star : empty;

            sb.append(cell.replace("{day}", String.valueOf(day)));
        }

        return sb.toString();
    }

    @Override
    public void onUpdate(Config data) {
        if(data == null) return;

        sConfig = data;

        Log.info("Reward config updated.");
    }

    private final static String[] BROADCAST_MESSAGES = new String[]{
            NekoCore.TAG + ChatColor.translateAlternateColorCodes('&', "{player}&7さんが投票で&r {reward} &7をゲットしました！"),
            NekoCore.TAG + ChatColor.translateAlternateColorCodes('&', "&3まだ投票をしていませんか？ ↓をクリックしてぜひ投票を！"),
            NekoCore.TAG + ChatColor.translateAlternateColorCodes('&', "&b&nhttps://minecraft.kentan.jp/vote")
    };

    private final static String[] PLAYER_MESSAGES = new String[]{
            NekoCore.TAG + ChatColor.translateAlternateColorCodes('&', "&6投票ありがとにゃ(｡･ω･｡)"),
            NekoCore.TAG + ChatColor.translateAlternateColorCodes('&', "&e特典&r {reward} &rを&dゲット！"),
            NekoCore.TAG + ChatColor.translateAlternateColorCodes('&', "&aステータス&7: {status}"),
            NekoCore.TAG + ChatColor.translateAlternateColorCodes('&', "&7毎日投票すると、特典がアップグレードします！")
    };

    public static class Config{
        final int CONTINUOUS_LIMIT;
        private List<Reward> mRewardList = new ArrayList<>();

        public Config(List<Reward> rewardList){
            CONTINUOUS_LIMIT = rewardList.size();
            mRewardList.addAll(rewardList);
        }

        Reward getReward(UUID uuid){
            final int continuous = getContinuous(uuid);
            return mRewardList.get(continuous - 1);
        }

        int getContinuous(UUID uuid){
            Date lastDate = getDate(uuid);
            int continuous = (int)PlayerConfigProvider.get(uuid, "Vote.continuous", 1);

            if(lastDate != null && diffDays(new Date(), lastDate) <= 1){
                return Math.min(continuous + 1, CONTINUOUS_LIMIT);
            }

            return 1;
        }

        void saveContinuous(UUID uuid, int continuous){
            PlayerConfigProvider.save(uuid, new LinkedHashMap<String, Object>(){
                    {
                        put("Vote.date", DATE_FORMAT.format(new Date()));
                        put("Vote.continuous", continuous);
                    }
                }
            );
        }

        void saveCommand(UUID uuid, List<String> commandList){
            PlayerConfigProvider.save(uuid, new HashMap<String, Object>(){
                        {
                            put("stackCommands", commandList);
                        }
                    }
            );
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

        private static int diffDays(Date from, Date to) {
            return (int) TimeUnit.MILLISECONDS.toDays(from.getTime() - to.getTime());
        }
    }
}

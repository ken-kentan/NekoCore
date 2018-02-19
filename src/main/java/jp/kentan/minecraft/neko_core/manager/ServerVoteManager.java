package jp.kentan.minecraft.neko_core.manager;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.component.ServerVoteReward;
import jp.kentan.minecraft.neko_core.event.ConfigUpdateEvent;
import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerVoteManager implements ConfigUpdateEvent<ServerVoteManager.Config> {

    private final static ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();

    private Config mConfig;

    public void vote(final String playerName) {
        final Player player = Util.getOnlinePlayer(playerName);
        final String playerDisplayName;
        final UUID uuid;

        boolean isOnline = (player != null) && player.isOnline();

        if (isOnline) {
            uuid = player.getUniqueId();
            playerDisplayName = player.getDisplayName();
        } else {
            uuid = Util.getOfflinePlayerUuid(playerName);
            playerDisplayName = playerName;
        }

        if (uuid == null) {
            Log.error("Could not resolve UUID of " + playerName + ".");
            return;
        }


        final int voteContinuous      = mConfig.getContinuous(uuid);
        final ServerVoteReward reward = mConfig.getReward(voteContinuous);

        if (isOnline) {
            reward.getCommandList().forEach(cmd -> Bukkit.getServer().dispatchCommand(CONSOLE, cmd.replace("{player}", playerName)));

            final String[] playerMessages = PLAYER_MESSAGES.clone();
            playerMessages[1] = playerMessages[1].replace("{reward}", reward.NAME);
            playerMessages[2] = playerMessages[2].replace("{status}", buildStatusMessage(voteContinuous));

            player.sendMessage(playerMessages);
        } else {
            if (!mConfig.PLAYER_CONFIG.addStackCommands(uuid, reward.getCommandList())) {
                Log.error("failed add the StackCommands of " + uuid + '.');
            }
        }

        mConfig.updateVoteData(uuid, voteContinuous);

        /*
        Broadcast
         */
        final String[] broadcastMessages = BROADCAST_MESSAGES.clone();
        broadcastMessages[0] = broadcastMessages[0].replace("{player}", playerDisplayName).replace("{reward}", reward.NAME);

        Util.broadcast(broadcastMessages, player);

        Log.info(playerName + " voted as " + (isOnline ? "ONLINE." : "offline."));
    }

    private String buildStatusMessage(int continuous) {
        StringBuilder sb = new StringBuilder();

        final String star  = ChatColor.AQUA + "{day}日" + ChatColor.GRAY + "[" + ChatColor.YELLOW + "★" + ChatColor.GRAY + "] ";
        final String empty = ChatColor.DARK_GRAY + "{day}日[☆] ";

        for (int day = 1; day <= mConfig.CONTINUOUS_LIMIT; ++day) {
            String cell = (day <= continuous) ? star : empty;

            sb.append(cell.replace("{day}", String.valueOf(day)));
        }

        return sb.toString();
    }

    @Override
    public void onConfigUpdate(Config data) {
        mConfig = data;
    }



    private final static String[] PLAYER_MESSAGES = new String[]{
            NekoCore.PREFIX + ChatColor.translateAlternateColorCodes('&', "&6投票ありがとにゃ(｡･ω･｡)"),
            NekoCore.PREFIX + ChatColor.translateAlternateColorCodes('&', "&e特典&r {reward} &rを&dゲット！"),
            NekoCore.PREFIX + ChatColor.translateAlternateColorCodes('&', "&aステータス&7: {status}"),
            NekoCore.PREFIX + ChatColor.translateAlternateColorCodes('&', "&7毎日投票すると、特典がアップグレードします！")
    };

    private final static String[] BROADCAST_MESSAGES = new String[]{
            NekoCore.PREFIX + ChatColor.translateAlternateColorCodes('&', "{player}&7さんが投票で&r {reward} &7をゲットしました！"),
            NekoCore.PREFIX + ChatColor.translateAlternateColorCodes('&', "&3まだ投票をしていませんか？ ↓をクリックしてぜひ投票を！"),
            NekoCore.PREFIX + ChatColor.translateAlternateColorCodes('&', "&b&nhttps://minecraft.kentan.jp/vote")
    };

    public static class Config {

        private final int CONTINUOUS_LIMIT;

        private final List<ServerVoteReward> REWARD_LIST = new ArrayList<>();
        private final PlayerConfigProvider PLAYER_CONFIG;


        public Config(PlayerConfigProvider configProvider, List<ServerVoteReward> rewardList) {
            PLAYER_CONFIG = configProvider;

            REWARD_LIST.addAll(rewardList);
            CONTINUOUS_LIMIT = REWARD_LIST.size();
        }

        private int getContinuous(UUID uuid){
            ZonedDateTime lastDate = PLAYER_CONFIG.getLastServerVoteDate(uuid);
            int continuous         = PLAYER_CONFIG.getServerVoteContinuous(uuid);

            if(lastDate != null && ChronoUnit.DAYS.between(lastDate, ZonedDateTime.now()) <= 1){
                return Math.min(continuous + 1, CONTINUOUS_LIMIT);
            }

            return 1;
        }

        private ServerVoteReward getReward(int index) {
            return REWARD_LIST.get(index - 1);
        }

        private void updateVoteData(UUID uuid, int continuous) {
            if (!PLAYER_CONFIG.saveServerVoteData(uuid, ZonedDateTime.now(), continuous)) {
                Log.error("failed save the ServerVoteDate of " + uuid + '.');
            }
        }
    }
}

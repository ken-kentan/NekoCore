package jp.kentan.minecraft.neko_core.manager;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.component.AsyncWebClient;
import jp.kentan.minecraft.neko_core.component.ServerVoteReward;
import jp.kentan.minecraft.neko_core.event.ConfigUpdateEvent;
import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.Util;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ServerVoteManager implements ConfigUpdateEvent<List<ServerVoteReward>> {

    private final static ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();
    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();
    private final List<ServerVoteReward> REWARD_LIST = new ArrayList<>();
    private final AsyncWebClient WEB_CLIENT = new AsyncWebClient();

    private final Plugin PLUGIN;
    private final PlayerConfigProvider PLAYER_CONFIG;

    private int mVoteContinuousLimit = 0;

    public ServerVoteManager(Plugin plugin, PlayerConfigProvider playerConfigProvider) {
        PLUGIN = plugin;
        PLAYER_CONFIG = playerConfigProvider;
    }

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


        final int voteContinuous      = calcVoteContinuous(uuid);
        final ServerVoteReward reward = getReward(voteContinuous);

        if (isOnline) {
            reward.getCommandList().forEach(cmd -> Bukkit.getServer().dispatchCommand(CONSOLE, cmd.replace("{player}", playerName)));

            final String[] playerMessages = PLAYER_MESSAGES.clone();
            playerMessages[1] = playerMessages[1].replace("{reward}", reward.NAME);
            playerMessages[2] = playerMessages[2].replace("{status}", buildStatusMessage(voteContinuous));

            player.sendMessage(playerMessages);
        } else {
            if (!PLAYER_CONFIG.addStackCommands(uuid, reward.getCommandList())) {
                Log.error("failed add the StackCommands of " + uuid + '.');
            }
        }

        if (!PLAYER_CONFIG.saveServerVoteData(uuid, ZonedDateTime.now(), voteContinuous)) {
            Log.error("failed save the ServerVoteDate of " + uuid + '.');
        }

        /*
        Broadcast
         */
        final String[] broadcastMessages = BROADCAST_MESSAGES.clone();
        broadcastMessages[0] = broadcastMessages[0].replace("{player}", playerDisplayName).replace("{reward}", reward.NAME);

        Util.broadcast(broadcastMessages, player);

        Log.info(playerName + " voted as " + (isOnline ? "ONLINE." : "offline."));
    }


    public void checkPlayerVoted(Player target) {
        final String WARN = NekoCore.PREFIX + ChatColor.YELLOW;
        final String ERROR = NekoCore.PREFIX + ChatColor.RED;

        String targetPlayerName = target.getName();

        WEB_CLIENT.fetch("https://minecraft.jp/servers/minecraft.kentan.jp", new Callback() {
            @Override
            public void onResponse(@Nonnull Call call, @Nonnull Response response) {
                ZonedDateTime targetLastVoteDate = PLAYER_CONFIG.getLastServerVoteDate(target.getUniqueId());

                try {
                    ResponseBody body = response.body();
                    if (body == null) {
                        target.sendMessage(WARN + "時間をおいて再度お試しください.");
                        return;
                    }
                    Document document = Jsoup.parse(body.string());
                    Elements elements = document.select("ul.nav.avatar-list.players-icon li a");


                    boolean isFindTarget = false;
                    for (Element element : elements) {
                        // 最近の投票者リスト順に確認
                        String playerName = element.attr("href").substring(9); // "/players/name"

                        if (!isFindTarget && playerName.equals(targetPlayerName)) {
                            if (targetLastVoteDate == null) {
                                SCHEDULER.scheduleSyncDelayedTask(PLUGIN, () -> vote(targetPlayerName));
                                return;
                            }

                            isFindTarget = true;
                            continue;
                        }

                        if (isFindTarget) {
                            @SuppressWarnings("deprecation")
                            UUID uuid = Bukkit.getOfflinePlayer(playerName).getUniqueId();

                            ZonedDateTime lastVoteDate = PLAYER_CONFIG.getLastServerVoteDate(uuid);

                            if (lastVoteDate == null) {
                                continue;
                            }

                            if (targetLastVoteDate.isAfter(lastVoteDate)) {
                                target.sendMessage(WARN + "すでに特典を受け取っています.");
                                return;
                            }

                            SCHEDULER.scheduleSyncDelayedTask(PLUGIN, () -> vote(targetPlayerName));
                            return;
                        }
                    }

                    target.sendMessage(WARN + "投票を確認できませんでした.");
                    target.sendMessage(NekoCore.PREFIX + ChatColor.GRAY + "投票時のユーザー名が正しくない可能性があります.");
                } catch (Exception e) {
                    e.printStackTrace();
                    target.sendMessage(WARN + "時間をおいて再度お試しください.");
                }
            }

            @Override
            public void onFailure(@Nonnull Call call, @Nonnull IOException e) {
                e.printStackTrace();
                target.sendMessage(ERROR + "minecraft.jpへのアクセスに失敗しました.");
            }
        });
    }

    private String buildStatusMessage(int continuous) {
        StringBuilder sb = new StringBuilder();

        for (int day = 1; day <= mVoteContinuousLimit; ++day) {
            String cell = (day <= continuous) ? STATUS_STAR : STATUS_NONE;

            sb.append(cell.replace("{day}", Integer.toString(day)));
        }

        return sb.toString();
    }

    private int calcVoteContinuous(UUID uuid){
        ZonedDateTime lastDate = PLAYER_CONFIG.getLastServerVoteDate(uuid);
        int continuous         = PLAYER_CONFIG.getServerVoteContinuous(uuid);

        if(lastDate != null && ChronoUnit.DAYS.between(lastDate, ZonedDateTime.now()) <= 1){
            return Math.min(continuous + 1, mVoteContinuousLimit);
        }

        return 1;
    }

    private ServerVoteReward getReward(int index) {
        return REWARD_LIST.get(index - 1);
    }

    @Override
    public void onConfigUpdate(List<ServerVoteReward> data) {
        REWARD_LIST.clear();
        REWARD_LIST.addAll(data);

        mVoteContinuousLimit = REWARD_LIST.size();
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

    private final static String STATUS_STAR = ChatColor.translateAlternateColorCodes('&', "&b{day}日&7[&e★&7] ");
    private final static String STATUS_NONE = ChatColor.translateAlternateColorCodes('&', "&8{day}日[☆] ");
}

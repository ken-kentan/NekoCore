package jp.kentan.minecraft.neko_core.rank;

import jp.kentan.minecraft.neko_core.bridge.LuckPermsProvider;
import jp.kentan.minecraft.neko_core.bridge.VaultProvider;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.event.EventBus;
import me.lucko.luckperms.api.event.EventHandler;
import me.lucko.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;


public class RankManager {

    private static final Scoreboard SCOREBOARD = Bukkit.getScoreboardManager().getMainScoreboard();

    private static Plugin sPlugin;
    private static Node sGoldRank, sDiamondRank, sEmeraldRank;

    public static void setup(Plugin plugin) {
        sPlugin = plugin;

        sGoldRank    = LuckPermsProvider.getNodeByGroupName("gold_rank");
        sDiamondRank = LuckPermsProvider.getNodeByGroupName("diamond_rank");
        sEmeraldRank = LuckPermsProvider.getNodeByGroupName("emerald_rank");

        final EventBus eventBus = LuckPermsProvider.getEventBus();
        eventBus.getHandlers(UserDataRecalculateEvent.class).forEach(EventHandler::unregister);
        eventBus.subscribe(UserDataRecalculateEvent.class, new RankManager()::onUserDataRecalculate);
    }

    public static void update(final Player player) {
        if (player == null) return;

        final User user = LuckPermsProvider.getUser(player.getUniqueId());

        if (user == null) return;

        ChatColor rankColor = ChatColor.RESET;

        if (user.hasPermission(sEmeraldRank).asBoolean()) {
            rankColor = ChatColor.GREEN;
        } else if (user.hasPermission(sDiamondRank).asBoolean()) {
            rankColor = ChatColor.AQUA;
        } else if (user.hasPermission(sGoldRank).asBoolean()) {
            rankColor = ChatColor.GOLD;
        }


        if(rankColor != ChatColor.RESET){
            player.setDisplayName(rankColor + player.getName() + ChatColor.RESET);
        }

        Team team = SCOREBOARD.getTeam(player.getName());

        if (player.hasPermission("group.staff")) {
            if (team == null) {
                team = SCOREBOARD.registerNewTeam(player.getName());
                team.addEntry(player.getName());
            }

            String prefix = ChatColor.translateAlternateColorCodes('&', VaultProvider.getPlayerPrefix(player));
            prefix = prefix.substring(0, prefix.length() - 2);

            team.setPrefix(prefix + rankColor);
        } else if (rankColor != ChatColor.RESET){
            if (team == null) {
                team = SCOREBOARD.registerNewTeam(player.getName());
                team.addEntry(player.getName());
            }

            team.setPrefix(rankColor.toString());
        } else if (team != null) {
            team.unregister();
        }
    }

    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(sPlugin, () -> update(Bukkit.getPlayer(event.getUser().getUuid())));
    }
}

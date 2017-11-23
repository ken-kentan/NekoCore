package jp.kentan.minecraft.neko_core.rank;

import jp.kentan.minecraft.neko_core.bridge.LuckPermsProvider;
import jp.kentan.minecraft.neko_core.bridge.VaultProvider;
import me.lucko.luckperms.api.Group;
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
    private static Group sGoldRank, sDiamondRank, sEmeraldRank;

    public static void setup(Plugin plugin) {
        sPlugin = plugin;

        sGoldRank = LuckPermsProvider.getGroup("gold_rank");
        sDiamondRank = LuckPermsProvider.getGroup("diamond_rank");
        sEmeraldRank = LuckPermsProvider.getGroup("emerald_rank");

        final EventBus eventBus = LuckPermsProvider.getEventBus();
        eventBus.getHandlers(UserDataRecalculateEvent.class).forEach(EventHandler::unregister);
        eventBus.subscribe(UserDataRecalculateEvent.class, new RankManager()::onUserDataRecalculate);
    }

    public static void update(final Player player) {
        if (player == null) return;

        final User user = LuckPermsProvider.getUser(player.getUniqueId());

        if (user == null) return;

        ChatColor rankColor = ChatColor.RESET;

        if (user.isInGroup(sEmeraldRank)) {
            rankColor = ChatColor.GREEN;
        } else if (user.isInGroup(sDiamondRank)) {
            rankColor = ChatColor.AQUA;
        } else if (user.isInGroup(sGoldRank)) {
            rankColor = ChatColor.GOLD;
        }

        player.setDisplayName(rankColor + player.getName());

        Team team = SCOREBOARD.getTeam(player.getName());

        if (player.hasPermission("group.staff")) {
            if (team == null) {
                team = SCOREBOARD.registerNewTeam(player.getName());
                team.addEntry(player.getName());
            }

            String prefix = ChatColor.translateAlternateColorCodes('&', VaultProvider.getPlayerPrefix(player));
            prefix = prefix.substring(0, prefix.length() - 2);

            team.setPrefix(prefix + rankColor);
        } else if (team != null) {
            team.unregister();
        }
    }

    private void onUserDataRecalculate(UserDataRecalculateEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(sPlugin, () -> update(Bukkit.getPlayer(event.getUser().getUuid())));
    }
}

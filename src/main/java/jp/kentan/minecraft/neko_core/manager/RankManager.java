package jp.kentan.minecraft.neko_core.manager;

import jp.kentan.minecraft.neko_core.bridge.ChatProvider;
import jp.kentan.minecraft.neko_core.bridge.PermissionProvider;
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

    private final Plugin PLUGIN;
    private final PermissionProvider PERMISSION;
    private final ChatProvider CHAT;

    private static Node GOLD_RANK, DIAMOND_RANK, EMERALD_RANK;

    public RankManager(Plugin plugin, PermissionProvider permissionProvider, ChatProvider chatProvider) {
        PLUGIN = plugin;
        PERMISSION = permissionProvider;
        CHAT = chatProvider;

        GOLD_RANK    = PERMISSION.getNodeByGroupName("gold_rank");
        DIAMOND_RANK = PERMISSION.getNodeByGroupName("diamond_rank");
        EMERALD_RANK = PERMISSION.getNodeByGroupName("emerald_rank");

        final EventBus eventBus = PERMISSION.getEventBus();
        eventBus.getHandlers(UserDataRecalculateEvent.class).forEach(EventHandler::unregister);
        eventBus.subscribe(UserDataRecalculateEvent.class, this::onUserDataRecalculate);
    }

    public void update(final Player player) {
        if (player == null) return;

        final User user = PERMISSION.getUser(player.getUniqueId());

        if (user == null) return;

        ChatColor rankColor = ChatColor.RESET;

        if (user.hasPermission(EMERALD_RANK).asBoolean()) {
            rankColor = ChatColor.GREEN;
        } else if (user.hasPermission(DIAMOND_RANK).asBoolean()) {
            rankColor = ChatColor.AQUA;
        } else if (user.hasPermission(GOLD_RANK).asBoolean()) {
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

            String prefix = ChatColor.translateAlternateColorCodes('&', CHAT.getPlayerPrefix(player));
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
        Bukkit.getScheduler().scheduleSyncDelayedTask(PLUGIN, () -> update(Bukkit.getPlayer(event.getUser().getUuid())));
    }
}

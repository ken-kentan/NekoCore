package jp.kentan.minecraft.neko_core.utils;

import jp.kentan.minecraft.neko_core.listener.LuckPermsEventListener;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Group;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.event.EventBus;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class RankUtils {

    private static final Scoreboard SCOREBOARD = Bukkit.getScoreboardManager().getMainScoreboard();
    private static Chat sChat;
    private static LuckPermsApi sLuckPermsApi = LuckPerms.getApi();

    private static Group sGoldRank, sDiamondRank, sEmeraldRank;

    public static void setup() {
        RegisteredServiceProvider<Chat> chatProvider = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);

        if (chatProvider != null) {
            sChat = chatProvider.getProvider();
        } else {
            Log.warn("failed to detect net.milkbowl.vault.chat.Chat");
            return;
        }

        if(sLuckPermsApi != null){
            sGoldRank    = sLuckPermsApi.getGroup("gold_rank");
            sDiamondRank = sLuckPermsApi.getGroup("diamond_rank");
            sEmeraldRank = sLuckPermsApi.getGroup("emerald_rank");

            LuckPermsEventListener listener = new LuckPermsEventListener();

            EventBus eventBus = sLuckPermsApi.getEventBus();
            eventBus.subscribe(LuckPermsEventListener.class, listener::onUserDataRecalculate);
        } else {
            Log.warn("failed to detect LuckPerms");
            return;
        }

        refreshAll();
    }

    public static void update(final Player player) {
        if(player == null) return;


        final User user = sLuckPermsApi.getUser(player.getUniqueId());

        if(user == null) return;


        String prefix = ChatColor.translateAlternateColorCodes('&', sChat.getPlayerPrefix(player));

        if(prefix.length() <= 0 || prefix.contains("Special") || prefix.contains("Guest")) return;


        Team team = SCOREBOARD.getTeam(player.getName());

        if(team == null){
            team = SCOREBOARD.registerNewTeam(player.getName());
            team.addEntry(player.getName());
        }


        prefix = prefix.substring(0, prefix.length()-2);

        ChatColor rankColor = ChatColor.RESET;

        if (user.isInGroup(sEmeraldRank)) {
            rankColor = ChatColor.GREEN;
        } else if (user.isInGroup(sDiamondRank)) {
            rankColor = ChatColor.AQUA;
        } else if (user.isInGroup(sGoldRank)) {
            rankColor = ChatColor.GOLD;
        }

        if(!rankColor.equals(ChatColor.RESET)){
            player.setDisplayName(rankColor + player.getName());
        }

        team.setPrefix(prefix + rankColor);
    }

    public static void reset(final Player player) {
        Team team = SCOREBOARD.getTeam(player.getName());

        if (team != null) {
            team.unregister();
        }
    }

    private static void refreshAll(){
        Bukkit.getServer().getOnlinePlayers().forEach(p -> {
            reset(p);
            update(p);
        });
    }
}

package jp.kentan.minecraft.neko_core.listener;

import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.rank.RankManager;
import jp.kentan.minecraft.neko_core.tutorial.TutorialManager;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;


public class PlayerEventListener implements Listener {

    private final static ConsoleCommandSender CONSOLE = Bukkit.getServer().getConsoleSender();
    private Plugin sPlugin;

    public PlayerEventListener(Plugin plugin){
        sPlugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        TutorialManager.joinTutorialIfNeed(player);
        RankManager.update(player);

        List<String> stackCommandList = PlayerConfigProvider.get(player.getUniqueId(), "stackCommands");

        if(stackCommandList == null || stackCommandList.size() <= 0) return;

        Bukkit.getScheduler().scheduleSyncDelayedTask(sPlugin, () -> {
            final String playerName = player.getName();
            stackCommandList.forEach(cmd -> Bukkit.getServer().dispatchCommand(CONSOLE, cmd.replace("{player}", playerName)));
        }, 20L * 5);
    }

}

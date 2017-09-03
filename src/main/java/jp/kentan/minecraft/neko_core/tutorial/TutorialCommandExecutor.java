package jp.kentan.minecraft.neko_core.tutorial;

import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class TutorialCommandExecutor implements CommandExecutor {
    private TutorialManager mManager;

    public TutorialCommandExecutor(JavaPlugin plugin, TutorialManager manager){
        mManager = manager;

        plugin.getServer().getPluginManager().registerEvents(mManager, plugin);

        plugin.getCommand("tutorial").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length;

        if(!NekoUtils.isPlayer(sender) || params < 1){
            printHelp(sender);
            return true;
        }

        Player player = (Player)sender;
        PermissionUser user = PermissionsEx.getUser(player);

        if(user.inGroup("Guest")){
            mManager.agree(player, args[0]);
        }

        return true;
    }

    private void printHelp(CommandSender sender){
        sender.sendMessage(ChatColor.GOLD + "******************************************");
        sender.sendMessage(ChatColor.GOLD + "サーバールールのキーワードを入力してください.");
        sender.sendMessage(ChatColor.GOLD + "例 キーワードが cat の場合は");
        sender.sendMessage(ChatColor.GOLD + "/tutorial cat");
        sender.sendMessage(ChatColor.GOLD + "とチャットに入力してください.");
        sender.sendMessage(ChatColor.GOLD + "******************************************");
    }
}

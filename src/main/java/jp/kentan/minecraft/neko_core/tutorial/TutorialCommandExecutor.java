package jp.kentan.minecraft.neko_core.tutorial;

import jp.kentan.minecraft.neko_core.spawn.SpawnManager;
import jp.kentan.minecraft.neko_core.spawn.listener.SpawnCancelListener;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


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

        switch (args[0]){
            case "agree":
                mManager.agree(player);
                break;
            case "disagree":
                mManager.disagree(player);
                break;
            default:
                printHelp(sender);
                break;
        }

        return true;
    }

    private void printHelp(CommandSender sender){
        sender.sendMessage(ChatColor.GOLD + "******************************************");
        sender.sendMessage(ChatColor.GOLD + "サーバーのルールに同意しますか？");
        sender.sendMessage(ChatColor.GOLD + "同意する場合は　 /tutorial agree");
        sender.sendMessage(ChatColor.GOLD + "同意しない場合は /tutorial disagree");
        sender.sendMessage(ChatColor.GOLD + "とチャットに入力してください.");
        sender.sendMessage(ChatColor.GOLD + "******************************************");
    }
}

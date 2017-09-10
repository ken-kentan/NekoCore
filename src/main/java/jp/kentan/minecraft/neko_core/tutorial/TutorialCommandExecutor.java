package jp.kentan.minecraft.neko_core.tutorial;

import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.Bukkit;
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

        if(!NekoUtils.isPlayer(sender)){
            return true;
        }

        Player player = (Player) sender;

        if (mManager.isGuest(player)) {

            if (params < 1) {
                printHelp(player);
                return true;
            }

            mManager.agree(player, args[0]);
        }

        return true;
    }

    private void printHelp(Player player){
        player.sendMessage(ChatColor.GOLD + "****************************************************");
        Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                "tellraw " + player.getName() + " [\"\",{\"text\":\"[サーバールール]\",\"bold\":true,\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://minecraft.kentan.jp/rule/\"}},{\"text\":\"を確認して、キーワードを入力してください.\",\"color\":\"gold\"}]");
        player.sendMessage(ChatColor.GOLD + "例 キーワードが cat の場合は");
        player.sendMessage(ChatColor.GOLD + "/tutorial cat");
        player.sendMessage(ChatColor.GOLD + "とチャットに入力してください.");
        player.sendMessage(ChatColor.GOLD + "****************************************************");
    }
}

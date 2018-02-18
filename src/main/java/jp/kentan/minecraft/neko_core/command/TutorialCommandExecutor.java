package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.manager.TutorialManager;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TutorialCommandExecutor implements CommandExecutor {

    private final TutorialManager MANAGER;

    public TutorialCommandExecutor(TutorialManager tutorialManager) {
        MANAGER = tutorialManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!Util.isPlayer(sender)){
            return true;
        }

        Player player = (Player) sender;

        if (MANAGER.isGuest(player)) {
            if (args.length >= 1) {
                MANAGER.finish(player, args[0]);
            } else {
                sendHelp(player);
            }
        } else {
            MANAGER.spawn(player);
        }

        return true;
    }

    private void sendHelp(Player player){
        player.sendMessage(ChatColor.GOLD + "****************************************************");
        Bukkit.getServer().dispatchCommand(
                Bukkit.getConsoleSender(),
                "tellraw " + player.getName() +
                        " [\"\",{\"text\":\"サーバールール\",\"bold\":true,\"color\":\"aqua\"},{\"text\":\"[\",\"color\":\"gray\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://minecraft.kentan.jp/rule/\"}},{\"text\":\"ｸﾘｯｸ!\",\"color\":\"aqua\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://minecraft.kentan.jp/rule/\"}},{\"text\":\"]\",\"color\":\"gray\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://minecraft.kentan.jp/rule/\"}},{\"text\":\"を\",\"color\":\"gold\"},{\"text\":\"確認\",\"color\":\"red\"},{\"text\":\"して,キーワードを入力してください.\",\"color\":\"gold\"}]");
        player.sendMessage(ChatColor.GOLD + "例 キーワードが cat の場合は");
        player.sendMessage(ChatColor.GOLD + "/tutorial cat");
        player.sendMessage(ChatColor.GOLD + "とチャットに入力してください.");
        player.sendMessage(ChatColor.GOLD + "****************************************************");
    }
}

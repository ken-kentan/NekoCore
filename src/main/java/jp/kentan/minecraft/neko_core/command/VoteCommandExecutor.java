package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.manager.ServerVoteManager;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class VoteCommandExecutor implements CommandExecutor, TabCompleter {

    private final ServerVoteManager MANAGER;

    public VoteCommandExecutor(ServerVoteManager serverVoteManager) {
        MANAGER = serverVoteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length <= 0) {
            sender.sendMessage(NekoCore.PREFIX + ChatColor.AQUA + ChatColor.UNDERLINE + "https://minecraft.kentan.jp/vote");
            sender.sendMessage(NekoCore.PREFIX + ChatColor.GRAY + "↑のアドレスをクリックして下さい.");
            return true;
        }

        switch (args[0]) {
            case "check":
                if (Util.isPlayer(sender)) {
                    MANAGER.checkPlayerVoted((Player) sender);
                }
                break;
            case "force":
                if (sender.hasPermission("neko.vote.admin") && args.length > 1) {
                    MANAGER.vote(args[1]);
                }
                break;
            default:
                Util.sendUnknownCommand(sender);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return (args.length == 1) ? Collections.singletonList("check") : Collections.emptyList();
    }
}

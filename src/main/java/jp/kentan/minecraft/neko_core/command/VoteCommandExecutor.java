package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.manager.ServerVoteManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class VoteCommandExecutor implements CommandExecutor {

    private final ServerVoteManager MANAGER;

    public VoteCommandExecutor(ServerVoteManager serverVoteManager) {
        MANAGER = serverVoteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender.hasPermission("neko.vote.admin") && args.length > 0) {
            MANAGER.vote(args[0]);
            return true;
        }

        sender.sendMessage(NekoCore.PREFIX + ChatColor.AQUA + ChatColor.UNDERLINE + "https://minecraft.kentan.jp/vote");
        sender.sendMessage(NekoCore.PREFIX + ChatColor.GRAY + "↑のアドレスをクリックして下さい.");

        return true;
    }
}

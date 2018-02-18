package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.component.WeatherState;
import jp.kentan.minecraft.neko_core.manager.WeatherVoteManager;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WeatherVoteCommandExecutor implements CommandExecutor, TabCompleter {

    private final WeatherVoteManager MANAGER;

    public WeatherVoteCommandExecutor(WeatherVoteManager manager) {
        Collections.sort(COMMAND_ARGUMENT_LIST);

        MANAGER = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!Util.isPlayer(sender)) {
            return true;
        }

        Player player = (Player) sender;

        if (args.length <= 0) {
            MANAGER.vote(player);
            return true;
        }

        switch (args[0]) {
            case "sun":
                MANAGER.startVoteTask(player, WeatherState.SUN);
                break;
            case "rain":
                MANAGER.startVoteTask(player, WeatherState.RAIN);
                break;
            case "info":
                MANAGER.sendInfo(player);
                break;
            case "help":
                sendHelp(sender);
                break;
            default:
                Util.sendUnknownCommand(sender);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if(args.length <= 0) {
            return COMMAND_ARGUMENT_LIST;
        }

        List<String> completeList = new ArrayList<>();

        if (args.length == 1) {
            args[0] = args[0].toLowerCase();

            COMMAND_ARGUMENT_LIST.forEach(cmdArg -> {
                if (cmdArg.startsWith(args[0])) {
                    completeList.add(cmdArg);
                }
            });
        }

        return completeList;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("---------- 天候投票コマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.GOLD + "/weathervote" + ChatColor.RESET + " 投票する.");
        sender.sendMessage("| " + ChatColor.GOLD + "/weathervote <sun/rain>" + ChatColor.RESET + " 天候投票を開始する.");
        sender.sendMessage("| " + ChatColor.GOLD + "/weathervote info" + ChatColor.RESET + " 投票状況を表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/weathervote help" + ChatColor.RESET + " ヘルプを表示します.");
        sender.sendMessage("| " + ChatColor.GRAY + "/weathervote は /wvote と省略することも可能です.");
        sender.sendMessage("---------------------------------------");
    }

    private final List<String> COMMAND_ARGUMENT_LIST = Arrays.asList("sun", "rain", "info", "help");
}

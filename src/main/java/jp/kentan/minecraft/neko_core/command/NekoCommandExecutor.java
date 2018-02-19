package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.ConfigManager;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NekoCommandExecutor implements CommandExecutor, TabCompleter {

    private final ConfigManager CONFIG;

    public NekoCommandExecutor(ConfigManager configManager) {
        ADMIN_COMMAND_ARGUMENT_LIST.addAll(COMMAND_ARGUMENT_LIST);

        Collections.sort(COMMAND_ARGUMENT_LIST);
        Collections.sort(ADMIN_COMMAND_ARGUMENT_LIST);

        CONFIG = configManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 0 || args[0].equals("help")) {
            sendHelp(sender);
            return true;
        }

        if (sender.hasPermission("neko.admin") && args[0].equals("reload")) {
            if (CONFIG.reload()) {
                sender.sendMessage(NekoCore.PREFIX + ChatColor.GREEN + "設定ファイルを更新しました.");
            } else {
                sender.sendMessage(NekoCore.PREFIX + ChatColor.RED + "設定ファイルの更新に失敗しました.");
            }

            return true;
        }

        switch (args[0]) {
            case "nyan":
                if (Util.isPlayer(sender)) {
                    playNyan((Player) sender);
                }
                break;
            case "hp":
                sendUrl(sender, "https://minecraft.kentan.jp");
                break;
            case "map":
                sendUrl(sender, "http://minecraft.kentan.jp:8123");
                break;
            case "rule":
                sendUrl(sender, "https://minecraft.kentan.jp/rule/");
                break;
            case "discord":
                sendUrl(sender, "https://discord.gg/j3a6trZ");
                break;
            case "twitter":
                sendUrl(sender, "https://twitter.com/DekitateServer");
                break;
            default:
                Util.sendUnknownCommand(sender);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean isAdmin = sender.hasPermission("neko.admin");

        if (args.length <= 0) {
            return isAdmin ? ADMIN_COMMAND_ARGUMENT_LIST : COMMAND_ARGUMENT_LIST;
        }

        List<String> completeList = new ArrayList<>();

        if (args.length == 1) {
            args[0] = args[0].toLowerCase();
            (isAdmin ? ADMIN_COMMAND_ARGUMENT_LIST : COMMAND_ARGUMENT_LIST).forEach(arg -> {
                if (arg.startsWith(args[0])) {
                    completeList.add(arg);
                }
            });
        }

        return completeList;
    }

    private void playNyan(Player player) {
        player.sendMessage(" にゃーんฅ(●´ω｀●)ฅ");
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1, 1);
    }

    private void sendUrl(CommandSender sender, String url) {
        sender.sendMessage(NekoCore.PREFIX + ChatColor.AQUA + ChatColor.UNDERLINE + url);
        sender.sendMessage(NekoCore.PREFIX + ChatColor.GRAY + "↑のアドレスをクリックして下さい.");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("---------- NekoCoreコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko nyan" + ChatColor.RESET + " にゃーん.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko hp" + ChatColor.RESET + " ホームページのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko map" + ChatColor.RESET + " WebMapのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko rule" + ChatColor.RESET + " サーバールールのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko discord" + ChatColor.RESET + " DiscordのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko twitter" + ChatColor.RESET + " TwitterのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko help" + ChatColor.RESET + " ヘルプを表示します.");
        sender.sendMessage("| " + ChatColor.BLUE + "/zone help" + ChatColor.RESET + " 区画管理のヘルプを表示.");
        sender.sendMessage("| " + ChatColor.LIGHT_PURPLE + "/ad help" + ChatColor.RESET + " 広告のヘルプを表示.");
        sender.sendMessage("| " + ChatColor.DARK_AQUA + "/wvote help" + ChatColor.RESET + " 天候投票のヘルプを表示.");
        sender.sendMessage("| " + ChatColor.GRAY + "/neko は /nk と省略することも可能です.");
        sender.sendMessage("---------------------------------------");
    }

    private final List<String> COMMAND_ARGUMENT_LIST = Arrays.asList("nyan", "hp", "map", "rule", "discord", "twitter", "help");
    private final List<String> ADMIN_COMMAND_ARGUMENT_LIST = new ArrayList<>(Collections.singletonList("reload"));
}

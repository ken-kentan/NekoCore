package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.manager.AdvertisementManager;
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

public class AdvertisementCommandExecutor implements CommandExecutor, TabCompleter {

    private final AdvertisementManager MANAGER;

    public AdvertisementCommandExecutor(AdvertisementManager advertisementManager) {
        ADMIN_COMMAND_ARGUMENT_LIST.addAll(COMMAND_ARGUMENT_LIST);

        Collections.sort(COMMAND_ARGUMENT_LIST);
        Collections.sort(ADMIN_COMMAND_ARGUMENT_LIST);

        MANAGER = advertisementManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length <= 0 || args[0].equals("help")) {
            sendHelp(sender);
            return true;
        }

        switch (args[0]) {
            case "set":
                if (Util.isPlayer(sender) && args.length > 2) {
                    MANAGER.addSetAdConfirmTask((Player) sender, args[1], Util.jointStringArray(args, 2));
                } else {
                    Util.sendMissingParameters(sender);
                }
                break;
            case "unset":
                if (Util.isPlayer(sender)) {
                    MANAGER.addUnsetAdConfirmTask((Player) sender);
                }
                break;
            case "preview":
                if (args.length > 1) {
                    MANAGER.sendPreview(sender, args[1]);
                } else {
                    Util.sendMissingParameters(sender);
                }
                break;
            case "info":
                if (Util.isPlayer(sender)) {
                    MANAGER.sendPlayerAdInfo((Player) sender);
                }
                break;
            case "list":
                MANAGER.sendAdList(sender);
                break;
            case "freq":
                if (Util.isPlayer(sender) && args.length > 1) {
                    MANAGER.setAdvertiseFrequency((Player) sender, args[1]);
                } else {
                    Util.sendMissingParameters(sender);
                }
                break;
            case "confirm":
                if (Util.isPlayer(sender)) {
                    MANAGER.confirmTask((Player) sender);
                }
                break;
            case "sync":
                if (sender.hasPermission("neko.admin")) {
                    MANAGER.sync();
                    sender.sendMessage(NekoCore.PREFIX + ChatColor.GREEN + "最新のデータベースと同期しました.");
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
        } else if (args.length == 2 && args[0].equals("freq")) {
            args[1] = args[1].toLowerCase();

            FREQ_COMMAND_PARAM_LIST.forEach(param -> {
                if (param.startsWith(args[1])) {
                    completeList.add(param);
                }
            });
        }

        return completeList;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(COMMAND_HELP);
    }

    final private String[] COMMAND_HELP = new String[]{
            "---------- 広告コマンドヘルプ ----------",
            ChatColor.translateAlternateColorCodes('&', "| &d/ad set <日数> <内容> &r広告を登録"),
            ChatColor.translateAlternateColorCodes('&', "| &d/ad unset &r広告を消去"),
            ChatColor.translateAlternateColorCodes('&', "| &d/ad preview <内容> &r広告をプレビュー"),
            ChatColor.translateAlternateColorCodes('&', "| &d/ad info &r広告を確認"),
            ChatColor.translateAlternateColorCodes('&', "| &d/ad list &r全プレイヤーの広告を確認"),
            ChatColor.translateAlternateColorCodes('&', "| &d/ad freq <off|low|middle|high> &r広告の受信頻度を変更"),
            ChatColor.translateAlternateColorCodes('&', "| &d/ad help &rヘルプを表示"),
            ChatColor.translateAlternateColorCodes('&', "| &7'&'を使用して装飾コードを利用できます."),
            "---------------------------------------"
    };

    private final List<String> COMMAND_ARGUMENT_LIST = Arrays.asList("set", "unset", "preview", "info", "list", "freq", "confirm", "help");
    private final List<String> ADMIN_COMMAND_ARGUMENT_LIST = new ArrayList<>(Collections.singletonList("sync"));

    private final List<String> FREQ_COMMAND_PARAM_LIST = Arrays.asList("high", "middle", "low", "off");
}

package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.manager.ZoneManager;
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

public class ZoneCommandExecutor implements CommandExecutor, TabCompleter {

    private final ZoneManager MANAGER;

    public ZoneCommandExecutor(ZoneManager manager) {
        ADMIN_COMMAND_ARGUMENT_LIST.addAll(COMMAND_ARGUMENT_LIST);

        Collections.sort(COMMAND_ARGUMENT_LIST);
        Collections.sort(ADMIN_COMMAND_ARGUMENT_LIST);

        MANAGER = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int params = args.length;

        if (params <= 0 || args[0].equals("help")) {
            sendHelp(sender);
            return true;
        }

        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
        }

        // 管理者コマンド
        if (sender.hasPermission("neko.zone.admin")) {
            switch (args[0]) {
                case "register": // rg <areaName> <zoneId> <regionId> <size>
                case "rg":
                    if (player != null && params >= 5) {
                        if (params == 5) {
                            MANAGER.registerArea(player, args[1], args[2], args[3], args[4]);
                        } else if (params >= 7) {
                            MANAGER.registerAreas(player, args[1], args[2], args[3], args[4], args[5], args[6]);
                        } else {
                            Util.sendMissingParameters(sender);
                        }
                    } else {
                        Util.sendMissingParameters(sender);
                    }
                    return true;
                case "remove":   // rm <areaName>
                case "rm":
                    if (player != null && params >= 2) {
                        MANAGER.removeArea(player, args[1]);
                    } else {
                        sender.sendMessage(WARN_MISSING_AREA_NAME);
                    }
                    return true;
                case "lock":     // lock <areaName>
                    if (player != null && params >= 2) {
                        MANAGER.setAreaLock(player, args[1], true);
                    } else {
                        sender.sendMessage(WARN_MISSING_AREA_NAME);
                    }
                    return true;
                case "unlock":   // unlock <areaName>
                    if (player != null && params >= 2) {
                        MANAGER.setAreaLock(player, args[1], false);
                    } else {
                        sender.sendMessage(WARN_MISSING_AREA_NAME);
                    }
                    return true;
                case "take":     // take <areaName>
                    if (player != null && params >= 2) {
                        MANAGER.takeAreaFromOwner(player, args[1]);
                    } else {
                        sender.sendMessage(WARN_MISSING_AREA_NAME);
                    }
                    return true;
                case "reload":
                    MANAGER.reload();
                    return true;
                default:
                    break;
            }
        }

        if (!Util.isPlayer(sender) || player == null) {
            return true;
        }

        switch (args[0]) {
            case "info":
                if (params >= 2) {
                    MANAGER.sendAreaInfo(player, args[1]);
                } else {
                    player.sendMessage(WARN_MISSING_AREA_NAME);
                }
                break;
            case "buy":
                if (!sender.hasPermission("neko.zone.buy")) {
                    Util.sendPermissionError(sender);
                    return true;
                }

                if (params >= 2) {
                    MANAGER.registerBuyTask(player, args[1]);
                } else {
                    player.sendMessage(WARN_MISSING_AREA_NAME);
                }
                break;
            case "rental":
                if (!sender.hasPermission("neko.zone.rental")) {
                    Util.sendPermissionError(sender);
                    return true;
                }

                if (params >= 2) {
                    MANAGER.registerRentalTask(player, args[1]);
                } else {
                    player.sendMessage(WARN_MISSING_AREA_NAME);
                }
                break;
            case "sell":
                if (!sender.hasPermission("neko.zone.sell")) {
                    Util.sendPermissionError(sender);
                    return true;
                }

                if (params >= 2) {
                    MANAGER.registerSellTask(player, args[1]);
                } else {
                    player.sendMessage(WARN_MISSING_AREA_NAME);
                }
                break;
            case "limits":
                MANAGER.sendOwnerLimits(player);
                break;
            case "list":
                MANAGER.sendOwnerAreaList(player);
                break;
            case "rule":
                if (params >= 2) {
                    MANAGER.sendZoneRule(player, args[1]);
                } else {
                    player.sendMessage(WARN_MISSING_AREA_NAME);
                }
                break;
            case "confirm":
                MANAGER.confirmTask(player);
                break;
            default:
                Util.sendUnknownCommand(sender);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        boolean isAdmin = sender.hasPermission("neko.zone.admin");

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
        } else if (args.length == 2 && isRequireAreaName(args[0])) {
            if (sender instanceof Player) {
                String worldName = ((Player) sender).getWorld().getName();

                MANAGER.getAreaNameList(worldName).forEach(areaName -> {
                    if (areaName.startsWith(args[1])) {
                        completeList.add(areaName);
                    }
                });
            }
        } else if (args.length == 3 && isRequireZoneId(args[0])) {
            if (sender instanceof Player) {
                String worldName = ((Player) sender).getWorld().getName();

                MANAGER.getZoneIdList(worldName).forEach(areaName -> {
                    if (areaName.startsWith(args[2])) {
                        completeList.add(areaName);
                    }
                });
            }
        }

        return completeList;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(HELP_MESSAGES);
    }

    private boolean isRequireAreaName(String label) {
        return
                label.equals("info") ||
                        label.equals("buy") ||
                        label.equals("rental") ||
                        label.equals("sell") ||
                        label.equals("rule") ||
                        label.equals("register") ||
                        label.equals("rg") ||
                        label.equals("remove") ||
                        label.equals("rm") ||
                        label.equals("lock") ||
                        label.equals("unlock") ||
                        label.equals("take");
    }

    private boolean isRequireZoneId(String label) {
        return label.equals("register") || label.equals("rg");
    }

    private final List<String> COMMAND_ARGUMENT_LIST = Arrays.asList("info", "buy", "rental", "sell", "limits", "list", "rule", "confirm", "help");
    private final List<String> ADMIN_COMMAND_ARGUMENT_LIST = new ArrayList<>(Arrays.asList("register", "remove", "lock", "unlock", "take", "reload"));

    private final static String[] HELP_MESSAGES = new String[]{
            "---------- 区画コマンドヘルプ ----------",
            "| " + ChatColor.BLUE + "/zone info <区画名>    " + ChatColor.WHITE + " 区画の情報を表示.",
            "| " + ChatColor.BLUE + "/zone buy <区画名>     " + ChatColor.WHITE + " 区画を購入.",
            "| " + ChatColor.BLUE + "/zone rental <区画名>  " + ChatColor.WHITE + " 区画を借りる.",
            "| " + ChatColor.BLUE + "/zone sell <区画名>    " + ChatColor.WHITE + " 区画を売却.",
            "| " + ChatColor.BLUE + "/zone limits          " + ChatColor.WHITE + " 所有上限を表示.",
            "| " + ChatColor.BLUE + "/zone list            " + ChatColor.WHITE + " 所有区画一覧を表示.",
            "| " + ChatColor.BLUE + "/zone rule            " + ChatColor.WHITE + " ワールドの区画規約を表示.",
            "| " + ChatColor.BLUE + "/zone help            " + ChatColor.WHITE + " ヘルプを表示.",
            "---------------------------------------"
    };

    private final static String WARN_MISSING_AREA_NAME = ZoneManager.PREFIX + ChatColor.YELLOW + "区画名を入力してください.";
}

package jp.kentan.minecraft.neko_core.zone;

import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

class ZoneCommandExecutor implements CommandExecutor {

    private ZoneManager mManager;

    ZoneCommandExecutor(ZoneManager manager){
        mManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length;

        if(params <= 0 || args[0].equals("help")){
            sendHelp(sender);
            return true;
        }

        final Player player = NekoUtils.toPlayer(sender);

        if(sender.hasPermission("neko.zone.admin") || sender instanceof ConsoleCommandSender){
            switch (args[0]){
                case "param":
                    if(params < 5 || player == null) {
                        return true;
                    }

                    try {
                        mManager.setWorldParam(player, Integer.parseInt(args[1]),
                                Double.parseDouble(args[2]), Double.parseDouble(args[3]),
                                Double.parseDouble(args[4]));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case "register":
                case "rg":
                    if(params < 4 || player == null) {
                        return true;
                    }

                    try {
                        mManager.register(player, args[1], args[2], Integer.parseInt(args[3]));
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case "remove":
                case "rm":
                    if(params >= 2) {
                        mManager.remove(player, args[1]);
                    }
                    break;
                case "lock":
                    if(params >= 2 || player == null) {
                        mManager.setSaleStatus(false, player, args[1]);
                    }
                    break;
                case "unlock":
                    if(params >= 2 || player == null) {
                        mManager.setSaleStatus(true, player, args[1]);
                    }
                    break;
                case "refresh":
                    mManager.refresh();
                    return true;
            }
        }

        if(player == null){
            Log.warn("This command is not support in console.");
            return true;
        }

        switch (args[0]){
            case "info":
                if(params >= 2){
                    mManager.sendInfo(player, args[1]);
                }
                break;
            case "purchase":
                if(params >= 2){
                    mManager.prePurchase(player, args[1]);
                }
                break;
            case "sell":
                if(params >= 2){
                    mManager.preSell(player, args[1]);
                }
                break;
            case "limits":
                mManager.sendLimits(player);
                break;
            case "list":
                mManager.sendList(player);
                break;
            case "rule":
                mManager.sendRules(player);
                break;
            case "confirm":
                mManager.confirm(player);
                break;
            default:
                break;
        }
        return true;
    }

    private void sendHelp(CommandSender sender){
        sender.sendMessage(HELP_MESSAGES);
    }

    private final static String[] HELP_MESSAGES = new String[]{
            "---------- 区画コマンドヘルプ ----------",
            "| " + ChatColor.BLUE + "/zone info <区画名>    " + ChatColor.WHITE + " 区画の情報を表示.",
            "| " + ChatColor.BLUE + "/zone purchase <区画名>" + ChatColor.WHITE + " 区画を購入.",
            "| " + ChatColor.BLUE + "/zone sell <区画名>    " + ChatColor.WHITE + " 区画を売却.",
            "| " + ChatColor.BLUE + "/zone limits          " + ChatColor.WHITE + " 所有上限を表示.",
            "| " + ChatColor.BLUE + "/zone list            " + ChatColor.WHITE + " 所有区画一覧を表示.",
            "| " + ChatColor.BLUE + "/zone rule            " + ChatColor.WHITE + " ワールドの区画規約を表示.",
            "| " + ChatColor.BLUE + "/zone help            " + ChatColor.WHITE + " ヘルプを表示.",
            "---------------------------------------"
    };
}

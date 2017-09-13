package jp.kentan.minecraft.neko_core.zone;

import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class ZoneCommandExecutor implements CommandExecutor {

    private ZoneManager mManager;

    ZoneCommandExecutor(ZoneManager manager){
        mManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length;

        if(params < 1 || !NekoUtils.isPlayer(sender)){
            printHelp(sender);
            return true;
        }

        final Player player = NekoUtils.toPlayer(sender);

        if(sender.hasPermission("neko.zone.admin")){
            switch (args[0]){
                case "param":
                    if(params < 6) {
                        return true;
                    }

                    try {
                        mManager.setWorldParam(player, Integer.parseInt(args[1]),
                                Double.parseDouble(args[2]), Double.parseDouble(args[3]),
                                Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case "register":
                case "rg":
                    if(params < 4) {
                        return true;
                    }

                    try {
                        mManager.register(player, args[1], args[2], Integer.parseInt(args[3]));
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    break;
                case "lock":
                    if(params >= 2) {
                        mManager.setSaleStatus(false, player, args[1]);
                    }
                    break;
                case "unlock":
                    if(params >= 2) {
                        mManager.setSaleStatus(true, player, args[1]);
                    }
                    break;
                case "refresh":
                    mManager.refresh();
                    break;
            }
        }

        switch (args[0]){
            case "info":
                if(params >= 2){
                    mManager.sendInfo(player, args[1]);
                }
                break;
            case "limits":
                mManager.sendLimits(player);
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
            case "confirm":
                mManager.confirm(player);
                break;
            default:
                break;
        }
        return true;
    }

    private void printHelp(CommandSender sender){
        sender.sendMessage("help");
    }
}

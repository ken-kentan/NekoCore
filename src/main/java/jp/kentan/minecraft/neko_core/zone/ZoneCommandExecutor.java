package jp.kentan.minecraft.neko_core.zone;

import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

class ZoneCommandExecutor implements CommandExecutor {

    private ZoneManager mManager;

    ZoneCommandExecutor(ZoneManager manager){
        mManager = manager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length;

        if(params < 1 || !NekoUtils.isPlayer(sender)){
            return true;
        }

        if(sender.hasPermission("neko.zone.admin")){
            final Player player = NekoUtils.toPlayer(sender);

            switch (args[0]){
                case "rate":
                    if(params < 2) {
                        return true;
                    }

                    try {
                        mManager.setRate(player, Float.parseFloat(args[1]));
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
            }
        }

        switch (args[0]){
            case "price":
                break;
            case "info":
                break;
            case "buy":
                break;
            default:
                break;
        }
        return true;
    }

    private void printHelp(Player player){
        player.sendMessage("help");
    }
}

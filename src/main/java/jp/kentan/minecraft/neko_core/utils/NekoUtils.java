package jp.kentan.minecraft.neko_core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class NekoUtils {

    public final static Server SERVER = Bukkit.getServer();
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM/dd HH:mm:ss", Locale.JAPAN);

    public static String getTime(){
        return DATE_FORMAT.format(new Date());
    }

    public static boolean isPlayer(CommandSender sender){
        return sender instanceof Player;
    }

    public static Player toPlayer(CommandSender sender){
        if(sender instanceof Player) {
            return (Player) sender;
        }

        return null;
    }

    public static Player getOnlinePlayer(String playerName) {
        for(Player player : SERVER.getOnlinePlayers())
        {
            if(player.getName().equals(playerName)){
                return player;
            }
        }

        return null;
    }

    public static void broadcast(String[] messages, Player without){
        SERVER.getOnlinePlayers().forEach(player -> {
            if(player != without){
                player.sendMessage(messages);
            }
        });
    }
}

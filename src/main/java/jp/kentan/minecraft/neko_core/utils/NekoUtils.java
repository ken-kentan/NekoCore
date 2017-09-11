package jp.kentan.minecraft.neko_core.utils;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


public class NekoUtils {

    private final static Server SERVER = Bukkit.getServer();
    private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy MM/dd HH:mm:ss", Locale.JAPAN);

    private final static String MOJANG_PROFILES_API = "https://api.mojang.com/users/profiles/minecraft/";


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

    public static void broadcast(String message, Player without){
        NekoUtils.broadcast(new String[]{message}, without);
    }

    public static void broadcast(String[] messages, Player without){
        SERVER.getOnlinePlayers().forEach(player -> {
            if(player != without){
                player.sendMessage(messages);
            }
        });
    }

    public static UUID getPlayerUuid(String playerName){
        try{
            JSONObject json = new JSONObject(IOUtils.toString(new URL(MOJANG_PROFILES_API + playerName), StandardCharsets.UTF_8));

            return UUID.fromString(json.getString("id").replaceFirst( "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}

package jp.kentan.minecraft.neko_core.util;

import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONObject;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {

    private final static Server SERVER = Bukkit.getServer();

    private final static String MOJANG_PROFILES_API = "https://api.mojang.com/users/profiles/minecraft/";

    private final static DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");


    public static boolean isPlayer(CommandSender sender){
        if (sender instanceof Player){
            return true;
        }

        sender.sendMessage(ChatColor.YELLOW + "ゲーム内専用コマンドです.");
        return false;
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

    public static UUID getOfflinePlayerUuid(String playerName){
        try{
            JSONObject json = new JSONObject(IOUtils.toString(new URL(MOJANG_PROFILES_API + playerName), StandardCharsets.UTF_8));

            return UUID.fromString(json.getString("id").replaceFirst( "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"));
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static void broadcast(String message, Player without){
        SERVER.getOnlinePlayers().forEach(player -> {
            if(player != without){
                player.sendMessage(message);
            }
        });
    }

    public static void broadcast(String[] messages, Player without){
        SERVER.getOnlinePlayers().forEach(player -> {
            if(player != without){
                player.sendMessage(messages);
            }
        });
    }

    public static void sendUnknownCommand(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "このコマンドは存在しません.");
    }

    public static void sendMissingParameters(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "コマンドのパラメータが不足しています.");
    }

    public static void sendPermissionError(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "権限がありません.");
    }

    public static List<String> translateAlternateColorCodes(List<String> stringList){
        if (stringList == null) {
            return null;
        }

        List<String> translatedList = new ArrayList<>();
        stringList.forEach(str -> translatedList.add(ChatColor.translateAlternateColorCodes('&', str)));

        return translatedList;
    }

    public static String formatDate(ZonedDateTime date) {
        return DATE_FORMATTER.format(date);
    }

    public static String jointStringArray(String[] strings, int offset) {
        StringBuilder sb = new StringBuilder();

        for(int i = offset, size = strings.length; i < size; i++) {
            sb.append(' ');
            sb.append(strings[i]);
        }

        sb.deleteCharAt(0);

        return sb.toString();
    }
}

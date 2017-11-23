package jp.kentan.minecraft.neko_core.twitter;

import jp.kentan.minecraft.neko_core.config.ConfigManager;
import jp.kentan.minecraft.neko_core.config.ConfigUpdateListener;
import jp.kentan.minecraft.neko_core.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TwitterBot implements Listener, ConfigUpdateListener<TwitterBot.Messages> {

    private static Messages sMessages;
    private static ConcurrentLinkedQueue<QueueTweet> sTweetQueue = new ConcurrentLinkedQueue<>();

    private TwitterBot(Plugin plugin){
        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public static void setup(Plugin plugin){
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if(sTweetQueue.size() > 0){
                TwitterManager.tweet(sTweetQueue.poll().TWEET);
            }
        }, 20*30L, 20*30L);

        ConfigManager.bindBotMessagesListener(new TwitterBot(plugin));
    }

    public static void pushActionMessage(String playerName, String status){
        String tweet = sMessages.getActionMessage();

        tweet = tweet.replace("{player}", playerName);
        tweet = tweet.replace("{status}", status);
        tweet = tweet.replace("{face}", sMessages.getCatFace());

        sTweetQueue.remove(playerName);
        sTweetQueue.add(new QueueTweet(playerName, tweet));
    }

    public static String getCatFace(){return sMessages.getCatFace();}

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        pushActionMessage(event.getPlayer().getName(), "ログイン");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        pushActionMessage(event.getPlayer().getName(), "ログアウト");
    }

    @Override
    public void onUpdate(Messages data) {
        if(data == null) return;

        sMessages = data;

        Log.info("Twitter bot messages updated.");
    }


    public static class Messages{
        private final Random RANDOM = new Random();

        private List<String> CAT_FACE_LIST = new ArrayList<>();
        private List<String> ACTION_MESSAGE_LIST = new ArrayList<>();

        public Messages(List<String> nekoFace, List<String> actionMsg){
            CAT_FACE_LIST.addAll(nekoFace);
            ACTION_MESSAGE_LIST.addAll(actionMsg);
        }

        String getCatFace(){
            return CAT_FACE_LIST.get(RANDOM.nextInt(CAT_FACE_LIST.size()));
        }
        String getActionMessage(){
            return ACTION_MESSAGE_LIST.get(RANDOM.nextInt(ACTION_MESSAGE_LIST.size()));
        }
    }
}

package jp.kentan.minecraft.neko_core.tutorial;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.bridge.LuckPermsProvider;
import jp.kentan.minecraft.neko_core.config.ConfigManager;
import jp.kentan.minecraft.neko_core.config.ConfigUpdateListener;
import jp.kentan.minecraft.neko_core.spawn.SpawnManager;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.NekoUtil;
import me.lucko.luckperms.api.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;


public class TutorialManager implements ConfigUpdateListener<String> {

    private final static String GUEST_LOGIN_MSG = ChatColor.translateAlternateColorCodes('&', "が&6ゲスト&rとしてログインしました！");
    private final static String TUTORIAL_COMPLETE_MSG = ChatColor.translateAlternateColorCodes('&', "が&9チュートリアル&rを&6完了&rしました！");
    private final static String INVALID_KEYWORD_MSG = NekoCore.PREFIX + ChatColor.YELLOW + "キーワードが間違っています. ホームページのルールを確認してください.";
    private final static String ERROR_MSG = NekoCore.PREFIX + ChatColor.YELLOW + "チュートリアル処理に失敗しました. 運営に報告して下さい.";

    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private static Plugin sPlugin;
    private static Node sGuestNode, sCitizenNode;

    private static String sKeyword = "NULL";


    public static void setup(JavaPlugin plugin){
        sPlugin = plugin;

        sGuestNode = LuckPermsProvider.getNodeByGroupName("default");
        sCitizenNode = LuckPermsProvider.getNodeByGroupName("citizen");

        if(sGuestNode == null){
            Log.error("guest group does not exist.");
        }

        if(sCitizenNode == null){
            Log.error("citizen group does not exist.");
        }

        ConfigManager.bindTutorialKeywordListener(new TutorialManager());

        plugin.getCommand("tutorial").setExecutor(new TutorialCommandExecutor());
    }

    public static void joinTutorialIfNeed(Player player){
        if(isGuest(player)){
            NekoUtil.broadcast(NekoCore.PREFIX + player.getName() + GUEST_LOGIN_MSG, player);

            SCHEDULER.scheduleSyncDelayedTask(sPlugin, () -> SpawnManager.spawn(player, "tutorial"), 10L);
        }
    }

    static boolean isGuest(Player player){
        final User user = LuckPermsProvider.getUser(player.getUniqueId());
        return (user == null) || user.getPrimaryGroup().equals("default");
    }

    static void agree(Player player, String keyword){
        if(keyword != null && keyword.equals(sKeyword)){
            User user = LuckPermsProvider.getUser(player.getUniqueId());

            if(user == null){
                failed(player, "failed to find " + player.getName() + ".");
                return;
            }

            try {
                if(player.hasPermission("group.default")){
                    user.unsetPermission(sGuestNode);
                }

                user.setPermission(sCitizenNode);
                user.setPrimaryGroup("citizen");
            } catch (Exception e){
                e.printStackTrace();
                failed(player, e.toString());
                return;
            }

            LuckPermsProvider.getStorage().saveUser(user)
                    .thenAcceptAsync(wasSuccessful -> {
                        if (!wasSuccessful) {
                            return;
                        }

                        user.refreshCachedData();

                        SCHEDULER.scheduleSyncDelayedTask(sPlugin, () -> {
                            SpawnManager.spawn(player, "default");

                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f);

                            player.sendMessage(NekoCore.PREFIX + "できたてサーバー(猫)へようこそ！");
                            NekoUtil.broadcast(NekoCore.PREFIX + player.getName() + TUTORIAL_COMPLETE_MSG, player);

                            giveWelcomeItems(player);
                        });

                    }, LuckPermsProvider.getStorage().getAsyncExecutor());
        }else{
            player.sendMessage(INVALID_KEYWORD_MSG);
        }
    }

    private static void failed(Player player, String detail){
        player.sendMessage(ERROR_MSG);
        Log.warn(detail);
    }

    private static void giveWelcomeItems(Player player){
        player.getInventory().addItem(
                new ItemStack(Material.IRON_HELMET),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.IRON_AXE),
                new ItemStack(Material.IRON_SWORD),
                new ItemStack(Material.BAKED_POTATO, 64)
        );
    }

    @Override
    public void onUpdate(String data) {
        if(data == null) return;

        sKeyword = data;

        Log.info("Tutorial keyword updated.");
    }
}

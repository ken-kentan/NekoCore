package jp.kentan.minecraft.neko_core.manager;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.bridge.PermissionProvider;
import jp.kentan.minecraft.neko_core.event.ConfigUpdateEvent;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.Util;
import me.lucko.luckperms.api.Node;
import me.lucko.luckperms.api.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

public class TutorialManager implements ConfigUpdateEvent<String> {

    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final Plugin PLUGIN;
    private final PermissionProvider PERMISSION;
    private final Node GUEST_NODE, CITIZEN_NODE;
    private final SpawnManager SPAWN_MANAGER;

    private String mKeyword;

    public TutorialManager(Plugin plugin, PermissionProvider permissionProvider, SpawnManager spawnManager) {
        PLUGIN = plugin;
        PERMISSION = permissionProvider;
        SPAWN_MANAGER = spawnManager;

        GUEST_NODE = PERMISSION.getNodeByGroupName("default");
        CITIZEN_NODE = PERMISSION.getNodeByGroupName("citizen");

        if(GUEST_NODE == null){
            Log.error("guest group does not exist.");
        }

        if(CITIZEN_NODE == null){
            Log.error("citizen group does not exist.");
        }
    }

    public void teleportIfNeed(Player player) {
        if(isGuest(player)){
            Util.broadcast(NekoCore.PREFIX + player.getName() + GUEST_LOGIN_MSG, player);

            SCHEDULER.scheduleSyncDelayedTask(PLUGIN, () -> SPAWN_MANAGER.spawn(player, "tutorial"), 10L);
        }
    }

    public void finish(Player player, String keyword) {
        if (!mKeyword.equals(keyword)) {
            player.sendMessage(INVALID_KEYWORD_MSG);
            return;
        }

        User user = PERMISSION.getUser(player.getUniqueId());

        if(user == null){
            Log.error("failed to find LuckPermsUser(" + player.getName() + ").");
            player.sendMessage(ERROR_MSG);
            return;
        }

        try {
            if(player.hasPermission("group.default")){
                user.unsetPermission(GUEST_NODE);
            }

            user.setPermission(CITIZEN_NODE);
            user.setPrimaryGroup("citizen");
        } catch (Exception e){
            e.printStackTrace();
            player.sendMessage(ERROR_MSG);
            return;
        }

        PERMISSION.getUserManager().saveUser(user);

        user.refreshCachedData();

        SPAWN_MANAGER.spawn(player, "default");

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f);

        player.sendMessage(NekoCore.PREFIX + "できたてサーバー（猫）へようこそ！");
        Util.broadcast(NekoCore.PREFIX + player.getName() + TUTORIAL_COMPLETE_MSG, player);

        giveWelcomeItems(player);
    }

    public void spawn(Player player) {
        SPAWN_MANAGER.addSpawnTask(player, "tutorial");
    }

    public boolean isGuest(Player player){
        final User user = PERMISSION.getUser(player.getUniqueId());
        return (user == null) || user.getPrimaryGroup().equals("default");
    }

    private void giveWelcomeItems(Player player){
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
    public void onConfigUpdate(String data) {
        mKeyword = data;

        Log.info("Tutorial keyword updated to '" + mKeyword + "'.");
    }

    private final static String GUEST_LOGIN_MSG = ChatColor.translateAlternateColorCodes('&', "が&6ゲスト&rとしてログインしたにゃ(ﾉ*･ω･)ﾉ*");
    private final static String TUTORIAL_COMPLETE_MSG = ChatColor.translateAlternateColorCodes('&', "が&9チュートリアル&rを&6完了&rしました！");

    private final static String INVALID_KEYWORD_MSG = NekoCore.PREFIX + ChatColor.YELLOW + "キーワードが間違っています. ホームページのルールを確認してください.";
    private final static String ERROR_MSG = NekoCore.PREFIX + ChatColor.YELLOW + "チュートリアル処理に失敗しました. 運営に報告して下さい.";
}

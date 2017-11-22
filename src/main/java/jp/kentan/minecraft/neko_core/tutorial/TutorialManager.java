package jp.kentan.minecraft.neko_core.tutorial;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.spawn.SpawnManager;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;


public class TutorialManager implements Listener {

    private final String GUEST_LOGIN_MSG = ChatColor.translateAlternateColorCodes('&', "が&6ゲスト&rとしてログインしました！");
    private final String TUTORIAL_COMPLETE_MSG = ChatColor.translateAlternateColorCodes('&', "が&9チュートリアル&rを&6完了&rしました！");
    private final String INVALID_KEYWORD_MSG = NekoCore.TAG + ChatColor.YELLOW + "キーワードが間違っています. ルールを確認してください.";
    private final String ERROR_MSG = NekoCore.TAG + ChatColor.YELLOW + "チュートリアル処理に失敗しました. 運営に報告して下さい.";

    private Plugin mPlugin;
    private Server mServer;

    private LuckPermsApi mPermsApi;
    private Node mGuestNode, mCitizenNode;

    private SpawnManager mSpawn;
    private String mKeyword;

    public TutorialManager(SpawnManager spawn, String keyword){
        mPlugin = NekoCore.getPlugin();
        mServer = mPlugin.getServer();

        mSpawn = spawn;
        mKeyword = keyword;

        setupLuckPerms();
    }

    @EventHandler (priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        joinTutorialIfNeed(event.getPlayer());
    }

    private void joinTutorialIfNeed(Player player){
        if(isGuest(player)){
            NekoUtils.broadcast(NekoCore.TAG + player.getName() + GUEST_LOGIN_MSG, player);

            mServer.getScheduler().scheduleSyncDelayedTask(mPlugin, () -> mSpawn.spawn(player, "tutorial"), 10L);
        }
    }

    private void setupLuckPerms(){
        try {
            mPermsApi = LuckPerms.getApi();
            Log.print("LuckPerms detected.");
        } catch (Exception e){
            Log.warn("failed to detect LuckPerms.");
        }

        Group guestGroup = mPermsApi.getGroup("default");
        Group citizenGroup = mPermsApi.getGroup("citizen");

        if(guestGroup != null){
            mGuestNode = mPermsApi.getNodeFactory().makeGroupNode(guestGroup).build();
        }else{
            Log.warn("guest group does not exist.");
        }

        if(citizenGroup != null){
            mCitizenNode = mPermsApi.getNodeFactory().makeGroupNode(citizenGroup).build();
        }else{
            Log.warn("citizen group does not exist.");
        }
    }

    boolean isGuest(Player player){
        User user = mPermsApi.getUser(player.getUniqueId());

        return (user == null) || user.getPrimaryGroup().equals("default");
    }

    void agree(Player player, String keyword){
        if(keyword != null && keyword.equals(mKeyword)){
            User user = mPermsApi.getUser(player.getUniqueId());

            if(user == null){
                failed(player, "failed to find " + player.getName() + ".");
                return;
            }

            try {
                if(player.hasPermission("group.default")){
                    user.unsetPermission(mGuestNode);
                }

                user.setPermission(mCitizenNode);
                user.setPrimaryGroup("citizen");
            } catch (Exception e){
                e.printStackTrace();
                failed(player, e.toString());
                return;
            }

            mPermsApi.getStorage().saveUser(user)
                    .thenAcceptAsync(wasSuccessful -> {
                        if (!wasSuccessful) {
                            return;
                        }

                        user.refreshPermissions();

                        mServer.getScheduler().scheduleSyncDelayedTask(mPlugin, () -> {
                            mSpawn.spawn(player, "default");

                            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.0f);

                            player.sendMessage(NekoCore.TAG + "できたてサーバー(猫)へようこそ！");
                            NekoUtils.broadcast(NekoCore.TAG + player.getName() + TUTORIAL_COMPLETE_MSG, player);

                            giveWelcomeItems(player);
                        });

                    }, mPermsApi.getStorage().getAsyncExecutor());
        }else{
            player.sendMessage(INVALID_KEYWORD_MSG);
        }
    }

    private void failed(Player player, String detail){
        player.sendMessage(ERROR_MSG);
        Log.warn(detail);
    }

    private void giveWelcomeItems(Player player){
        player.getInventory().addItem(
                new ItemStack(Material.IRON_HELMET),
                new ItemStack(Material.IRON_CHESTPLATE),
                new ItemStack(Material.IRON_LEGGINGS),
                new ItemStack(Material.IRON_BOOTS),
                new ItemStack(Material.BAKED_POTATO, 64)
        );
    }
}

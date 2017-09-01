package jp.kentan.minecraft.neko_core.tutorial;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.spawn.SpawnManager;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;


public class TutorialManager implements Listener {

    private SpawnManager mSpawn;
    private String mKeyword;

    public TutorialManager(SpawnManager spawn, String keyword){
        mSpawn = spawn;
        mKeyword = keyword;
    }

    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        joinTutorialIfNeed(event.getPlayer());
    }

    private void joinTutorialIfNeed(Player player){
        PermissionUser user = PermissionsEx.getUser(player);

        if(user.inGroup("Guest")){
            mSpawn.spawn(player, "tutorial");
        }
    }

    void agree(Player player, String keyword){
        if(keyword != null && keyword.equals(mKeyword)){
            PermissionUser user = PermissionsEx.getUser(player);

            user.removeGroup("Guest");
            user.addGroup("Citizen");

            mSpawn.spawn(player, "default");

            player.sendMessage(NekoCore.TAG + "できたてサーバー(猫)へようこそ！");
            NekoUtils.broadcast(NekoCore.TAG + player + "がチュートリアルを完了しました！", player);
        }else{
            player.sendMessage(NekoCore.TAG + ChatColor.YELLOW + "キーワードが間違っています. ルールを確認してください.");
        }
    }
}

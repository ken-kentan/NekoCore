package jp.kentan.minecraft.neko_core.tutorial;

import jp.kentan.minecraft.neko_core.spawn.SpawnManager;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class TutorialManager implements Listener {

    private SpawnManager mSpawn;

    public TutorialManager(SpawnManager spawn){
        mSpawn = spawn;
    }

    @EventHandler (priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event){
        joinTutorialIfNeed(event.getPlayer());
    }

    private void joinTutorialIfNeed(Player player){
        //ToDo: 権限グループがtutorialかどうかで判断
        mSpawn.spawn(player, "Tutorial");
    }

    void agree(Player player){
        Log.print(player.getName() + " selected agree with server rule.");

        mSpawn.spawn(player, "default");
    }

    void disagree(Player player){
        Log.print(player.getName() + " selected DISAGREE with server rule.");

        Bukkit.getBanList(BanList.Type.NAME).addBan(player.getName(),
                StringUtils.repeat("\n", 35) +" サーバールールに同意しなかったため参加できません." + StringUtils.repeat("\n", 35),
                null,null);
    }
}

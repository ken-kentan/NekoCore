package jp.kentan.minecraft.neko_core.spawn.listener;

import jp.kentan.minecraft.neko_core.util.NekoUtil;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SpawnCancelListener implements Listener {
    private CancelListener mListener;

    public SpawnCancelListener(CancelListener listener){
        mListener = listener;
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location to = event.getTo();
        Location from = event.getFrom();

        if((to.getBlockX() != from.getBlockX()) || (to.getBlockY() != from.getBlockY()) || (to.getBlockZ() != from.getBlockZ())) {
            mListener.onCancel(event.getPlayer());
        }
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (NekoUtil.isPlayer(entity)) {
            mListener.onCancel((Player)entity);
        }
    }
}

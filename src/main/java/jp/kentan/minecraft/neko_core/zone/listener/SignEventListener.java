package jp.kentan.minecraft.neko_core.zone.listener;

import jp.kentan.minecraft.neko_core.utils.Log;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignEventListener implements Listener {

    private final static String SIGN_INDEX_TEXT = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + ChatColor.BOLD + "区画" + ChatColor.DARK_GRAY + ChatColor.BOLD + "]";


    private ZoneSignEventListener mListener;

    public SignEventListener(ZoneSignEventListener listener){
        mListener = listener;
    }

    @EventHandler (ignoreCancelled = true)
    public void onSignChanged(SignChangeEvent event) {
        if(event.getPlayer().hasPermission("neko.zone.moderator") && event.getLine(0).equals("z")){
            mListener.onSignPlace(event);
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent event) {
        final BlockState blockState = event.getBlock().getState();

        if(blockState instanceof Sign && event.getPlayer().hasPermission("neko.zone.moderator")){
            Sign sign = (Sign)blockState;

            if(sign.getLine(0).contains(SIGN_INDEX_TEXT)) {
                mListener.onSignBreak(event.getPlayer(), sign);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final BlockState blockState = event.getClickedBlock().getState();

        if(blockState instanceof Sign){
            Sign sign = (Sign)blockState;

            if(sign.getLine(0).contains(SIGN_INDEX_TEXT)){
                if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    mListener.onSignClick(event.getPlayer(), sign);
                }else if(!event.getPlayer().hasPermission("neko.zone.moderator")){
                    event.setCancelled(true);
                }
            }
        }
    }
}

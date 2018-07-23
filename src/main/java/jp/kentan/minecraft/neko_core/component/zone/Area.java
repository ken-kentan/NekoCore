package jp.kentan.minecraft.neko_core.component.zone;

import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Area {

    private final static String SIGN_INDEX_TEXT = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + ChatColor.BOLD + "区画" + ChatColor.DARK_GRAY + ChatColor.BOLD + "]";

    public final String NAME, WORLD, ZONE_ID, REGION_ID;
    public final int REGION_SIZE;
    public final AreaState STATE;
    public final Location SIGN_LOCATION;
    public final double BUY_RENTAL_PRICE;
    public final ZonedDateTime EXPIRE_DATE;

    public final OfflinePlayer OWNER;

    public Area(
            String name,
            String world,
            String zoneId,
            String regionId,
            int regionSize,
            AreaState state,
            Location signLocation,
            UUID ownerUuid,
            double buyRentalPrice,
            ZonedDateTime expireDate) {
        NAME = name;
        WORLD = world;
        ZONE_ID = zoneId;
        REGION_ID = regionId;
        REGION_SIZE = regionSize;
        STATE = state;
        SIGN_LOCATION = signLocation;
        BUY_RENTAL_PRICE = buyRentalPrice;
        EXPIRE_DATE = expireDate;

        OWNER = (ownerUuid != null) ? Bukkit.getOfflinePlayer(ownerUuid) : null;
    }

    public boolean isOwner(UUID uuid) {
        return (OWNER != null) && OWNER.getUniqueId().equals(uuid);
    }

    public final boolean isLock() {
        return STATE == AreaState.LOCK;
    }

    public final boolean onSale() {
        return STATE == AreaState.ON_SALE;
    }

    public boolean updateSign(){
        if(SIGN_LOCATION == null){
            return true;
        }

        BlockState blockState = SIGN_LOCATION.getWorld().getBlockAt(SIGN_LOCATION).getState();

        if(blockState instanceof Sign){
            Sign sign = (Sign)blockState;

            sign.setLine(0, SIGN_INDEX_TEXT);

            if(OWNER != null){
                sign.setLine(2, ChatColor.DARK_GRAY + OWNER.getName());
                sign.setLine(3, (EXPIRE_DATE == null) ? STATE.getName() : (ChatColor.DARK_PURPLE + Util.formatDate(EXPIRE_DATE)) + ChatColor.RESET + " まで");
            }else {
                sign.setLine(2, "");
                sign.setLine(3, STATE.getName());
            }

            if(!sign.update()){
                Log.warn("failed to update Area sing at " +
                        SIGN_LOCATION.getX() + ", " +
                        SIGN_LOCATION.getY() + ", " +
                        SIGN_LOCATION.getZ() + " in " + SIGN_LOCATION.getWorld().getName());
            }
        }else{ //看板が見つからなかった場合
            Log.warn("Sign was not found at " +
                    SIGN_LOCATION.getX() + ", " +
                    SIGN_LOCATION.getY() + ", " +
                    SIGN_LOCATION.getZ() + " in " + SIGN_LOCATION.getWorld().getName());

            return false;
        }

        return true;
    }
}

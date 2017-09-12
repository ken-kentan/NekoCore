package jp.kentan.minecraft.neko_core.zone.component;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.config.ZoneConfigProvider;
import jp.kentan.minecraft.neko_core.utils.Log;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class Area {

    private final static String SIGN_INDEX_TEXT = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + ChatColor.BOLD + "区画" + ChatColor.DARK_GRAY + ChatColor.BOLD + "]";

    public final static String ON_SALE_TEXT = ChatColor.RED + "販売中";
    public final static String SOLD_TEXT = ChatColor.GOLD + "契約済み";
    public final static String PROCESSING_TEXT = ChatColor.DARK_GRAY + "手続き中...";

    private final static double INF_PRICE = 99999999999D;

    private String mWorld, mName, mId;
    private UUID mOwnerUuid;
    private int mSize;
    private boolean mOnSale;
    private Location mSignLocation;

    public Area(String world, String name, String id, UUID owner, int size, boolean onSale, Location signLocation){
        mWorld = world;
        mName = name;
        mId = id;
        mOwnerUuid = owner;

        mSize = size;
        mOnSale = onSale;

        mSignLocation = signLocation;
    }

    public void updateSign(){
        if(mSignLocation == null){
            return;
        }

        BlockState blockState = Bukkit.getWorld(mWorld).getBlockAt(mSignLocation).getState();

        if(blockState instanceof Sign){
            Sign sign = (Sign)blockState;

            sign.setLine(0, SIGN_INDEX_TEXT);

            if(mOwnerUuid != null){
                sign.setLine(2, ChatColor.DARK_GRAY + getOwner().getName());
                sign.setLine(3, ChatColor.GREEN + "情報");
            }else {
                sign.setLine(2, "");
                sign.setLine(3, mOnSale ? ChatColor.RED + "販売中" : ChatColor.DARK_GRAY + "手続き中...");
            }

            if(!sign.update()){
                Log.warn("failed to set Area sing at " + mSignLocation.toString());
            }else{
                ZoneConfigProvider.setSign(mWorld, mName, mSignLocation);
            }
        }else{
            mSignLocation = null;
            Log.warn("failed to found Area sing.");
            ZoneConfigProvider.setSign(mWorld, mName, null);
        }
    }

    public void createSign(SignChangeEvent event){
        event.setLine(0, SIGN_INDEX_TEXT);

        if(mOwnerUuid != null){
            event.setLine(2, ChatColor.DARK_GRAY + getOwner().getName());
            event.setLine(3, ChatColor.GREEN + "情報");
        }else {
            event.setLine(3, mOnSale ? ON_SALE_TEXT : PROCESSING_TEXT);
        }

        ZoneConfigProvider.setSign(mWorld, mName, mSignLocation = event.getBlock().getLocation());
    }

    public void buy(UUID ownerUuid, ProtectedRegion region){
        ZoneConfigProvider.registerOwner(mWorld, ownerUuid, mName);

        DefaultDomain members = new DefaultDomain();
        members.addPlayer(ownerUuid);

        region.setMembers(members);

        mOwnerUuid = ownerUuid;
        mOnSale = false;

        updateSign();
    }

    public void sell(ProtectedRegion region){
        region.setMembers(new DefaultDomain());

        ZoneConfigProvider.removeOwner(mWorld, mOwnerUuid, mName);

        mOwnerUuid = null;
        mOnSale = false;

        updateSign();
    }

    public boolean setLock(boolean hasLock){
        if(mOwnerUuid != null){
            return false;
        }

        mOnSale = !hasLock;

        updateSign();

        ZoneConfigProvider.setOnSale(mWorld, mName, mOnSale);

        return true;
    }

    public String getName(){ return mName; }
    public String getId(){ return mId; }
    public OfflinePlayer getOwner(){
        if(mOwnerUuid == null){
            return null;
        }

        return Bukkit.getOfflinePlayer(mOwnerUuid);
    }

    public UUID getOwnerUuid(){ return mOwnerUuid; }

    public double getPrice(UUID uuid){
        int totalNum = PlayerConfigProvider.getOwnerAreaTotalNumber(uuid, mWorld);

        Map<String, Object> dataMap = ZoneConfigProvider.get(mWorld, Arrays.asList("rate", "rateGain", "ownerLimit"));

        int ownerLimit;
        double rate, rateGain;

        try {
            if(dataMap == null){
                return INF_PRICE;
            }

            ownerLimit = (int)dataMap.get("ownerLimit");

            rate = (double)dataMap.get("rate");
            rateGain = (double)dataMap.get("rateGain");
        } catch (Exception e){
            e.printStackTrace();
            return INF_PRICE;
        }

        totalNum = Math.min(totalNum, --ownerLimit);

        if(totalNum > 0){
            rate *= rateGain * totalNum;
        }

        return rate * mSize;
    }

    public boolean onSale(){ return mOnSale; }

    public boolean isOwner(UUID uuid){
        return uuid.equals(mOwnerUuid);
    }

    public Location getSignLocation() {
        if(mSignLocation == null){
            return null;
        }

        Block block = mSignLocation.getBlock();

        if(block != null && block.getState() instanceof Sign){
            return mSignLocation;
        }

        ZoneConfigProvider.setSign(mWorld, mName, null);

        return mSignLocation = null;
    }
}

package jp.kentan.minecraft.neko_core.zone.component;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
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

    private AreaUpdateListener mListener;

    private World mWorld;
    private String mName, mId;
    private UUID mOwnerUuid;
    private int mSize;
    private boolean mOnSale;
    private Location mSignLocation;

    public Area(AreaUpdateListener listener, World world, String name, String id, UUID owner, int size, boolean onSale, Location signLocation){
        mListener = listener;

        mWorld = world;
        mName = name;
        mId = id;
        mOwnerUuid = owner;

        mSize = size;
        mOnSale = onSale;

        mSignLocation = signLocation;
    }

    public void save(){
        mListener.onUpdate(this);
    }

    public void updateSign(){
        if(mSignLocation == null){
            return;
        }

        BlockState blockState = mWorld.getBlockAt(mSignLocation).getState();

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
            }
        }else{ //看板が見つからなかった場合
            Log.warn("failed to find Area sing at " + mSignLocation.toString());

            mSignLocation = null;

            mListener.onUpdate(this);
        }
    }

    public void breakSign(){
        mSignLocation = null;

        mListener.onUpdate(this);
    }

    public void createSign(SignChangeEvent event){
        event.setLine(0, SIGN_INDEX_TEXT);

        mSignLocation = event.getBlock().getLocation();

        if(mOwnerUuid != null){
            event.setLine(2, ChatColor.DARK_GRAY + getOwner().getName());
            event.setLine(3, ChatColor.GREEN + "情報");
        }else {
            event.setLine(3, mOnSale ? ON_SALE_TEXT : PROCESSING_TEXT);
        }

        mListener.onUpdate(this);
    }

    public void buy(UUID ownerUuid, ProtectedRegion region){
        DefaultDomain members = new DefaultDomain();
        members.addPlayer(ownerUuid);

        region.setMembers(members);

        mOwnerUuid = ownerUuid;
        mOnSale = false;

        updateSign();

        mListener.onUpdate(this);
    }

    public void sell(ProtectedRegion region){
        region.setMembers(new DefaultDomain());

        mOwnerUuid = null;
        mOnSale = false;

        updateSign();

        mListener.onUpdate(this);
    }

    public boolean setSaleStatus(boolean onSale){
        if(mOwnerUuid != null){
            return false;
        }

        mOnSale = onSale;

        updateSign();

        mListener.onUpdate(this);

        return true;
    }

    public World getWorld() {
        return mWorld;
    }

    public String getWorldName(){
        return mWorld.getName();
    }

    public String getName(){
        return mName;
    }

    public String getId(){
        return mId;
    }

    public int getSize(){
        return mSize;
    }

    public OfflinePlayer getOwner(){
        if(mOwnerUuid == null){
            return null;
        }

        return Bukkit.getOfflinePlayer(mOwnerUuid);
    }

    public UUID getOwnerUuid(){ return mOwnerUuid; }

    public double getBuyPrice(int total, WorldParam param){
        double rate = param.getBuyRate();

        total = Math.min(total, param.getOwnerLimit() - 1);

        if(total > 0){
            rate *= param.getBuyRateGain() * total;
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

        mSignLocation = null;

        mListener.onUpdate(this);

        return null;
    }
}

package jp.kentan.minecraft.neko_core.zone.component;

import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_core.utils.Log;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.block.SignChangeEvent;

import java.util.UUID;

public class Area implements Comparable<Area> {

    private final static String SIGN_INDEX_TEXT = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + ChatColor.BOLD + "区画" + ChatColor.DARK_GRAY + ChatColor.BOLD + "]";

    public final static String ON_SALE_TEXT = ChatColor.RED + "販売中";
    public final static String SOLD_TEXT = ChatColor.GOLD + "契約済み";
    public final static String PROCESSING_TEXT = ChatColor.DARK_GRAY + "手続き中...";

    private AreaUpdateListener mListener;

    private World mWorld;
    private String mName, mId;
    private UUID mOwnerUuid;
    private int mSize;
    private boolean mOnSale;
    private double mPurchasedPrice;
    private Location mSignLocation;

    public Area(AreaUpdateListener listener, World world, String name, String id, UUID owner, double purchasedPrice, int size, boolean onSale, Location signLocation){
        mListener = listener;

        mWorld = world;
        mName = name;
        mId = id;
        mOwnerUuid = owner;
        mPurchasedPrice = purchasedPrice;
        mSize = size;
        mOnSale = onSale;

        mSignLocation = signLocation;
    }

    public void save(){
        mListener.onUpdate(this);
    }

    public void remove(){
        mId = null;
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
                Log.warn("failed to update Area sing at " +
                        mSignLocation.getX() + ", " +
                        mSignLocation.getY() + ", " +
                        mSignLocation.getZ() + " in " + mSignLocation.getWorld().getName());
            }
        }else{ //看板が見つからなかった場合
            Log.warn("Sign was not found at " +
                    mSignLocation.getX() + ", " +
                    mSignLocation.getY() + ", " +
                    mSignLocation.getZ() + " in " + mSignLocation.getWorld().getName());

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

    public void purchase(UUID ownerUuid, ProtectedRegion region, double price){
        DefaultDomain members = new DefaultDomain();
        members.addPlayer(ownerUuid);

        region.setMembers(members);

        mOwnerUuid = ownerUuid;
        mOnSale = false;
        mPurchasedPrice = price;

        updateSign();

        mListener.onUpdate(this);
    }

    public void sell(ProtectedRegion region){
        region.setMembers(new DefaultDomain());

        mOwnerUuid = null;
        mOnSale = false;
        mPurchasedPrice = -1D;

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

    public World getWorld(){
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

    public double getPurchasePrice(int total, WorldParam param){
        double rate = param.getPurchaseRate();

        total = Math.min(total, param.getOwnerLimit() - 1);

        if(total > 0){
            rate *= param.getPurchaseRateGain() * total;
        }

        return rate * mSize;
    }

    public double getPurchasedPrice(){
        return mPurchasedPrice;
    }

    public double getSellPrice(WorldParam param){
        if(mPurchasedPrice < 0D){
            return 0D;
        }

        return mPurchasedPrice * param.getSellRate();
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

    @Override
    public int compareTo(Area a) {
        return mName.compareTo(a.getName());
    }
}

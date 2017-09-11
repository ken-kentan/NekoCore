package jp.kentan.minecraft.neko_core.zone.component;

import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.config.ZoneConfigProvider;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class Area {

    private final static double INF_PRICE = 99999999999D;

    private String mWorld, mName, mId;
    private UUID mOwnerUuid;
    private int mSize;
    private boolean mOnSale;

    public Area(String world, String name, String id, UUID owner, int size, boolean onSale){
        mWorld = world;
        mName = name;
        mId = id;
        mOwnerUuid = owner;

        mSize = size;
        mOnSale = onSale;
    }

    public String getName(){ return mName; }
    public String getId(){ return mId; }
    public String getOwner(){
        if(mOwnerUuid == null){
            return "";
        }

        return Bukkit.getOfflinePlayer(mOwnerUuid).getName();
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

        if(totalNum > 0){
            //所有上限の場合はレートゲインを上限に上書き
            if(totalNum >= ownerLimit){
                totalNum = --ownerLimit;
            }

            rate *= rateGain * totalNum;
        }

        return rate * mSize;
    }

    public boolean onSale(){ return mOnSale; }
}

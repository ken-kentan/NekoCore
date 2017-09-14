package jp.kentan.minecraft.neko_core.zone.component;

import org.bukkit.ChatColor;
import org.bukkit.World;

import java.util.List;

public class WorldParam implements Comparable<WorldParam> {

    private WorldParamUpdateListener mListener;

    private World mWorld;

    private int mOwnerLimit;
    private double mPurchaseRate, mPurchaseRateGain;
    private double mSellRate;

    private List<String> mPurchaseRuleMessage, mSellRuleMessage;

    public WorldParam(WorldParamUpdateListener listener, World world, int ownerLimit,
                      double purchaseRate, double purchaseRateGain, List<String> PurchaseRuleMessage,
                      double sellRate, List<String> sellRuleMessage){
        mListener = listener;

        mWorld = world;

        mOwnerLimit = ownerLimit;

        mPurchaseRate = purchaseRate;
        mPurchaseRateGain = purchaseRateGain;
        mPurchaseRuleMessage = PurchaseRuleMessage;

        mSellRate = sellRate;
        mSellRuleMessage = sellRuleMessage;

        translateAlternateColorCodes(mPurchaseRuleMessage);
        translateAlternateColorCodes(mSellRuleMessage);
    }

    public void save(){
        mListener.onUpdate(this);
    }

    public void update(int ownerLimit, double PurchaseRate, double PurchaseRateGain, double sellRate){
        mOwnerLimit = ownerLimit;

        mPurchaseRate = PurchaseRate;
        mPurchaseRateGain = PurchaseRateGain;

        mSellRate = sellRate;

        mListener.onUpdate(this);
    }

    public World getWorld(){
        return mWorld;
    }

    public String getWorldName(){
        return mWorld.getName();
    }

    public int getOwnerLimit(){
        return  mOwnerLimit;
    }

    public double getPurchaseRate(){
        return mPurchaseRate;
    }

    public double getPurchaseRateGain(){
        return mPurchaseRateGain;
    }

    public List<String> getPurchaseRuleMessage(){
        return mPurchaseRuleMessage;
    }

    public double getSellRate(){
        return mSellRate;
    }

    public List<String> getSellRuleMessage(){
        return mSellRuleMessage;
    }

    private static void translateAlternateColorCodes(List<String> textList){
        if(textList != null) {
            for (int i = 0; i < textList.size(); ++i) {
                textList.set(i, ChatColor.translateAlternateColorCodes('&', textList.get(i)));
            }
        }
    }

    @Override
    public int compareTo(WorldParam p) {
        return mWorld.getName().compareTo(p.getWorldName());
    }
}

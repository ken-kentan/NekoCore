package jp.kentan.minecraft.neko_core.zone.component;

import org.bukkit.ChatColor;
import org.bukkit.World;

import java.util.List;

public class WorldParam {

    private WorldParamUpdateListener mListener;

    private World mWorld;

    private int mOwnerLimit;
    private double mBuyRate, mBuyRateGain;
    private double mSellRate, mSellRateGain;

    private List<String> mBuyRuleMessage, mSellRuleMessage;

    public WorldParam(WorldParamUpdateListener listener, World world, int ownerLimit,
                      double buyRate, double buyRateGain, List<String> buyRuleMessage,
                      double sellRate, double sellRateGain, List<String> sellRuleMessage){
        mListener = listener;

        mWorld = world;

        mOwnerLimit = ownerLimit;

        mBuyRate = buyRate;
        mBuyRateGain = buyRateGain;
        mBuyRuleMessage = buyRuleMessage;

        mSellRate = sellRate;
        mSellRateGain = sellRateGain;
        mSellRuleMessage = sellRuleMessage;

        translateAlternateColorCodes(mBuyRuleMessage);
        translateAlternateColorCodes(mSellRuleMessage);
    }

    public void save(){
        mListener.onUpdate(this);
    }

    public void update(int ownerLimit, double buyRate, double buyRateGain, double sellRate, double sellRateGain){
        mOwnerLimit = ownerLimit;

        mBuyRate = buyRate;
        mBuyRateGain = buyRateGain;

        mSellRate = sellRate;
        mSellRateGain = sellRateGain;

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

    public double getBuyRate(){
        return mBuyRate;
    }

    public double getBuyRateGain(){
        return mBuyRateGain;
    }

    public List<String> getBuyRuleMessage(){
        return mBuyRuleMessage;
    }

    public double getSellRate(){
        return mSellRate;
    }

    public double getSellRateGain(){
        return mSellRateGain;
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
}

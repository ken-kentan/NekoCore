package jp.kentan.minecraft.neko_core.zone;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.config.ZoneConfigProvider;
import jp.kentan.minecraft.neko_core.economy.EconomyProvider;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.zone.component.Area;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ZoneManager {

    private RegionContainer mRegionContainer;

    private Map<Player, AreaProcessInfo> mWaitingAreaProcessMap = Collections.synchronizedMap(new HashMap<>());


    public ZoneManager(){
        WorldGuardPlugin worldGuard = detectWorldGuard();

        if(worldGuard != null) {
            mRegionContainer = worldGuard.getRegionContainer();
        }

        NekoCore.getPlugin().getCommand("zone").setExecutor(new ZoneCommandExecutor(this));
    }

    private WorldGuardPlugin detectWorldGuard(){
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");

        // WorldGuard may not be loaded
        if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
            Log.warn("failed to detect WorldGuard");
            return null; // Maybe you want throw an exception instead
        }

        Log.print("WorldGuard detected.");

        return (WorldGuardPlugin) plugin;
    }

    void setWorldConfig(Player player, double rate, double rateGain, int ownerLimit){
        if(rate < 0D){
            player.sendMessage("レートは0以上である必要があります.");
            return;
        }

        if(rateGain < 1D){
            player.sendMessage("レートゲインは1以上である必要があります.");
            return;
        }

        if(ownerLimit < 1){
            player.sendMessage("所有者上限は1以上である必要があります.");
            return;
        }

        ZoneConfigProvider.setWorldConfig(player.getWorld().getName(), rate, rateGain, ownerLimit);
    }

    void register(Player player, String areaId, String areaName, int size){
        RegionManager regions = mRegionContainer.get(player.getWorld());

        if(size < 1){
            player.sendMessage("面積は0以上である必要があります.");
        }

        if (regions != null && regions.hasRegion(areaId)) {

            boolean wasSuccessful = ZoneConfigProvider.register(player.getWorld().getName(), areaName, regions.getRegion(areaId), size);

            if(wasSuccessful){
                player.sendMessage("{id}を{name}で登録しました.".replace("{id}", areaId).replace("{name}", areaName));
            }else{
                player.sendMessage("登録に失敗しました.");
            }

        } else {
            player.sendMessage("{id}は存在しません.".replace("{id}", areaId));
        }
    }

    void info(Player player, String areaName){
        Area area = ZoneConfigProvider.getArea(player.getWorld().getName(), areaName);

        if(area != null){
            player.sendMessage(new String[]{
                    "名前:" + areaName,
                    "ID: " + area.getId(),
                    "価格: \u00A5" + area.getPrice(player.getUniqueId()),
                    "所有者:" + area.getOwner(),
                    "販売中:" + (area.onSale() ? "はい" : "いいえ")
            });
        }else{
            player.sendMessage("{name}は存在しません.".replace("{name}", areaName));
        }
    }

    void preBuy(Player player, String areaName){
        Area area = ZoneConfigProvider.getArea(player.getWorld().getName(), areaName);

        if(area == null){
            player.sendMessage("{name}は存在しません.".replace("{name}", areaName));
            return;
        }

        int ownerAreaNum = PlayerConfigProvider.getOwnerAreaTotalNumber(player.getUniqueId(), player.getWorld().getName());
        int ownerAreaLimit = (int)ZoneConfigProvider.get(player.getWorld().getName(), "ownerLimit", 1);

        if(ownerAreaNum >= ownerAreaLimit){
            player.sendMessage("所有数が上限に達しています.");
            return;
        }


        if(area.onSale()){
            player.sendMessage("区画" + areaName + "を\u00A5" + area.getPrice(player.getUniqueId()) + "で購入しますか？");
            player.sendMessage("購入を確定するには /zone confirm と入力して下さい.");

            mWaitingAreaProcessMap.put(player, new AreaProcessInfo(AreaProcessInfo.Type.BUY, area));

            Bukkit.getScheduler().runTaskLaterAsynchronously(NekoCore.getPlugin(),
                    () -> mWaitingAreaProcessMap.remove(player), 20L * 15);
        }else{
            player.sendMessage("現在、この区画は購入できません.");
        }
    }

    void confirm(Player player){
        AreaProcessInfo processInfo = mWaitingAreaProcessMap.get(player);

        if(processInfo == null){
            player.sendMessage("現在、あなたに認証が必要な処理はありません.");
            return;
        }

        mWaitingAreaProcessMap.remove(player);

        switch (processInfo.getType()){
            case BUY:
                buy(player, processInfo.getArea());
                break;
            case SALE:
                break;
        }

        player.sendMessage("" + processInfo.getType());
    }

    private void buy(Player player, Area area){
        double price = area.getPrice(player.getUniqueId());
        double balance = EconomyProvider.getBalance(player);

        if(balance < price){
            player.sendMessage("所持金が\u00A5" + (price - balance) + "不足しています.");
            return;
        }


        final ProtectedRegion region = getProtectedRegion(player.getWorld(), area.getId());
        if(region == null){
            return;
        }

        if(!EconomyProvider.withdraw(player, price)){
            player.sendMessage("購入処理に失敗しました.");
            return;
        }

        DefaultDomain members = new DefaultDomain();
        members.addPlayer(player.getUniqueId());

        region.setMembers(members);


        //保存
        ZoneConfigProvider.registerOwner(player.getWorld().getName(), player, area.getName());

        player.sendMessage("区画" + area.getName() + "を\u00A5" + price + "で購入しました！");
    }

    private ProtectedRegion getProtectedRegion(World world, String id){
        RegionManager regions = mRegionContainer.get(world);

        if(regions == null){
            return null;
        }

        return regions.getRegion(id);
    }


    private static class AreaProcessInfo{
        enum Type {BUY, SALE}

        private Type mType;
        private Area mArea;

        AreaProcessInfo(Type type, Area area){
            mType = type;
            mArea = area;
        }

        Type getType(){ return mType; }

        Area getArea(){ return mArea; }
    }
}

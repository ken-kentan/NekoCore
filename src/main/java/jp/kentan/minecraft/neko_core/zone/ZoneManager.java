package jp.kentan.minecraft.neko_core.zone;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.config.ZoneConfigProvider;
import jp.kentan.minecraft.neko_core.economy.EconomyProvider;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.zone.component.Area;
import jp.kentan.minecraft.neko_core.zone.listener.SignEventListener;
import jp.kentan.minecraft.neko_core.zone.listener.ZoneSignEventListener;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ZoneManager implements ZoneSignEventListener {

    private final static String TAG = ChatColor.GRAY + "[" + ChatColor.BLUE + "区画" + ChatColor.GRAY  + "] " + ChatColor.RESET;

    private ZoneConfigProvider mConfigProvider;

    private RegionContainer mRegionContainer;

    private Map<Player, AreaProcessInfo> mWaitingAreaProcessMap = Collections.synchronizedMap(new HashMap<>());


    public ZoneManager(){
        WorldGuardPlugin worldGuard = detectWorldGuard();

        if(worldGuard != null) {
            mRegionContainer = worldGuard.getRegionContainer();
        }

        JavaPlugin plugin = NekoCore.getPlugin();

        mConfigProvider = new ZoneConfigProvider(plugin.getDataFolder());

        plugin.getCommand("zone").setExecutor(new ZoneCommandExecutor(this));
        plugin.getServer().getPluginManager().registerEvents(new SignEventListener(this), plugin);
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


    void refresh(){
        mConfigProvider.refresh();
    }

    @Override
    public void onSignPlace(SignChangeEvent event) {
        Player player = event.getPlayer();

        if(event.getLine(1).length() < 1){
            player.sendMessage(TAG + ChatColor.YELLOW + "区画名を入力して下さい.");
            return;
        }

        final Area area = mConfigProvider.getArea(player.getWorld(), event.getLine(1));

        if(area == null){
            player.sendMessage(TAG + ChatColor.YELLOW + event.getLine(1) + "は存在しません.");
            return;
        }

        Location signLocation = area.getSignLocation();
        if(signLocation != null){
            player.sendMessage(TAG + ChatColor.YELLOW + "この区画の看板はすでに" + signLocation.toString() + "に設置されています!");
            return;
        }

        area.createSign(event);
    }

    @Override
    public void onSignBreak(Player player, Sign sign) {
        Area area = mConfigProvider.getArea(player.getWorld(), sign.getLine(1));

        if(area == null){
            return;
        }

        area.breakSign();
        player.sendMessage(TAG + ChatColor.YELLOW + area.getName() + "の看板を消去しました!");
    }

    @Override
    public void onSignClick(Player player, Sign sign) {
        String nameArea = sign.getLine(1);
        String statusText = sign.getLine(3);

        Area area = sendInfo(player, nameArea);

        if(area != null && area.isOwner(player.getUniqueId())){
            player.sendMessage(ChatColor.GRAY + "この区画を売却するには " + ChatColor.RESET + "/zone sell " + nameArea + ChatColor.GRAY + " と入力して下さい.");
            return;
        }

        if(statusText.contains("販売中")){
            player.sendMessage(ChatColor.GRAY + "この区画を購入するには " + ChatColor.RESET + "/zone buy " + nameArea + ChatColor.GRAY + " と入力して下さい.");
        }
    }


    void setWorldConfig(Player player, double rate, double rateGain, int ownerLimit){
        if(rate < 0D){
            sendWarn(player, "レートは0以上に設定して下さい.");
            return;
        }

        if(rateGain < 1D){
            sendWarn(player, "レートゲインは1以上に設定して下さい.");
            return;
        }

        if(ownerLimit < 0){
            sendWarn(player, "所有者上限は0以上に設定して下さい.");
            return;
        }

        mConfigProvider.setWorldConfig(player.getWorld().getName(), rate, rateGain, ownerLimit);
    }


    void register(Player player, String areaId, String areaName, int size){
        if(size < 1){
            sendWarn(player, "面積は1以上に設定して下さい.");
            return;
        }

        RegionManager regions = mRegionContainer.get(player.getWorld());

        if (regions != null && regions.hasRegion(areaId)) {
            mConfigProvider.register(player.getWorld(), areaName, areaId, size);

            player.sendMessage(TAG + areaId + "を" + areaName + "で登録しました.");
        } else {
            sendWarn(player, areaId + "は存在しません.");
        }
    }


    Area sendInfo(Player player, String nameArea){
        final Area area = mConfigProvider.getArea(player.getWorld(), nameArea);

        if(area != null){
            OfflinePlayer owner = area.getOwner();
            player.sendMessage(new String[]{
                    ChatColor.GRAY + "***************" + ChatColor.BLUE + " 区画情報 " + ChatColor.GRAY + "***************",
                    " 名前: "      + nameArea,
                    " ID: "       + area.getId(),
                    " 価格: "      + ChatColor.YELLOW + "\u00A5" + area.getPrice(0),//ToDo 一時的に0渡し
                    " 所有者: "    + ((owner != null) ? ChatColor.DARK_GRAY + owner.getName() : ""),
                    " ステータス: " + (area.onSale() ? Area.ON_SALE_TEXT : ((owner != null) ? Area.SOLD_TEXT : Area.PROCESSING_TEXT))
            });
        }else{
            sendWarn(player, nameArea + "は存在しません.");
        }

        return area;
    }


    void sendLimits(Player player){

    }


    void setSaleStatus(boolean onSale, Player player, String nameArea){
        Area area = mConfigProvider.getArea(player.getWorld(), nameArea);

        if(area == null){
            sendWarn(player, nameArea + "は存在しません.");
            return;
        }


        if(area.setSaleStatus(onSale)){
            player.sendMessage(TAG + nameArea + "のステータスを " + (onSale ? Area.ON_SALE_TEXT : Area.PROCESSING_TEXT) + ChatColor.RESET + " にしました.");
        }else{
            sendWarn(player, nameArea + "には所有者がいます.");
        }
    }


    void preBuy(Player player, String nameArea){
        Area area = mConfigProvider.getArea(player.getWorld(), nameArea);

        if(area == null){
            sendWarn(player, nameArea + "は存在しません.");
            return;
        }


        int ownerAreaNum = PlayerConfigProvider.getOwnerAreaTotalNumber(player.getUniqueId(), player.getWorld().getName());
        int ownerAreaLimit = (int)ZoneConfigProvider.get(player.getWorld().getName(), "ownerLimit", 1);

        if(ownerAreaNum >= ownerAreaLimit){
            sendWarn(player, "このワールドにおける区画の所有数が上限に達しています.");
            return;
        }


        if(area.onSale()){
            player.sendMessage(TAG + nameArea + "を" + ChatColor.YELLOW + "\u00A5" + area.getPrice(0) + ChatColor.RESET + "で購入しますか？");//ToDo 一時的に0渡し
            player.sendMessage(TAG + ChatColor.GRAY + "購入を確定するには " + ChatColor.RED + "/zone confirm" + ChatColor.GRAY + " と入力して下さい.");

            mWaitingAreaProcessMap.put(player, new AreaProcessInfo(AreaProcessInfo.Type.BUY, area));

            Bukkit.getScheduler().runTaskLaterAsynchronously(NekoCore.getPlugin(),
                    () -> mWaitingAreaProcessMap.remove(player), 20L * 15);
        }else{
            sendWarn(player, "現在、この区画は購入できません.");
        }
    }

    void preSell(Player player, String nameArea){
        Area area = mConfigProvider.getArea(player.getWorld(), nameArea);

        if(area == null){
            sendWarn(player, nameArea + "は存在しません.");
            return;
        }

        if(!area.isOwner(player.getUniqueId())){
            sendWarn(player, "この区画の所有者ではありません.");
            return;
        }


        player.sendMessage(TAG + nameArea + "を" + ChatColor.YELLOW + "\u00A5" + 0 + ChatColor.RESET + "で売却しますか？");
        player.sendMessage(TAG + ChatColor.GRAY + "売却を確定するには " + ChatColor.RED + "/zone confirm" + ChatColor.GRAY + " と入力して下さい.");

        mWaitingAreaProcessMap.put(player, new AreaProcessInfo(AreaProcessInfo.Type.SALE, area));

        Bukkit.getScheduler().runTaskLaterAsynchronously(NekoCore.getPlugin(),
                () -> mWaitingAreaProcessMap.remove(player), 20L * 15);
    }

    void confirm(Player player){
        AreaProcessInfo processInfo = mWaitingAreaProcessMap.get(player);

        if(processInfo == null){
            sendWarn(player, "現在、あなたに認証が必要な処理はありません.");
            return;
        }


        mWaitingAreaProcessMap.remove(player);

        switch (processInfo.getType()){
            case BUY:
                buy(player, processInfo.getArea());
                break;
            case SALE:
                sell(player, processInfo.getArea());
                break;
            default:
                break;
        }
    }

    private void buy(Player player, Area area){
        double price = area.getPrice(0);//ToDo 一時的に0渡し
        double balance = EconomyProvider.getBalance(player);

        if(balance < price){
            sendWarn(player, "所持金が \u00A5" + (price - balance) + " 不足しています.");
            return;
        }


        final ProtectedRegion region = getProtectedRegion(player.getWorld(), area.getId());
        if(region == null){
            sendError(player, "IDエラーです. 運営に連絡して下さい.");
            return;
        }

        if(!EconomyProvider.withdraw(player, price)){
            sendError(player, "購入処理に失敗しました. 運営に連絡して下さい.");
            return;
        }

        area.buy(player.getUniqueId(), region);

        player.sendMessage(TAG + area.getName() + "を " + ChatColor.YELLOW + "\u00A5" + price + ChatColor.RESET + " で購入しました！");
    }

    private void sell(Player player, Area area){
        final ProtectedRegion region = getProtectedRegion(player.getWorld(), area.getId());
        if(region == null){
            sendError(player, "IDエラーです. 運営に連絡して下さい.");
            return;
        }

        area.sell(region);

        player.sendMessage(TAG + area.getName() + "を " + ChatColor.YELLOW + "\u00A5" + 0 + ChatColor.RESET + " で売却しました！");
    }

    private ProtectedRegion getProtectedRegion(World world, String id){
        RegionManager regions = mRegionContainer.get(world);

        if(regions == null){
            return null;
        }

        return regions.getRegion(id);
    }


    private static void sendWarn(Player player, String text){
        player.sendMessage(TAG + ChatColor.YELLOW + text);
    }

    private static void sendError(Player player, String text){
        player.sendMessage(TAG + ChatColor.RED + text);
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

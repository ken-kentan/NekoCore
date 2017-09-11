package jp.kentan.minecraft.neko_core.zone;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.config.ZoneConfigProvider;
import jp.kentan.minecraft.neko_core.utils.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class ZoneManager {

    private WorldGuardPlugin mWorldGuard;

    public ZoneManager(){
        mWorldGuard = detectWorldGuard();

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

    void setRate(Player sender, float rate){
        if(rate < 0f){
            sender.sendMessage("レートは0以上である必要があります.");
        }

        ZoneConfigProvider.setRate(sender.getWorld(), rate);
    }

    void register(Player sender, String areaId, String name, int size){
        RegionContainer container = mWorldGuard.getRegionContainer();
        RegionManager regions = container.get(sender.getWorld());

        if(size < 1){
            sender.sendMessage("面積は0以上である必要があります.");
        }

        if (regions != null && regions.hasRegion(areaId)) {

            boolean wasSuccessful = ZoneConfigProvider.register(sender.getWorld(), regions.getRegion(areaId), name, size);

            if(wasSuccessful){
                sender.sendMessage("登録しました.");
            }else{
                sender.sendMessage("登録に失敗しました.");
            }

        } else {
            sender.sendMessage("{id}は存在しません.".replace("{id}", areaId));
        }
    }
}

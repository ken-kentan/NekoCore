package jp.kentan.minecraft.neko_core.bridge;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import jp.kentan.minecraft.neko_core.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class WorldGuardProvider {

    private static WorldGuardPlugin sWorldGuardPlugin;

    public static void setup(){
        detectWorldGuard();
    }

    private static void detectWorldGuard(){
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");


        if (plugin != null && plugin instanceof  WorldGuardPlugin) {
            sWorldGuardPlugin = (WorldGuardPlugin) plugin;
            Log.info("WorldGuard detected.");
        }

        Log.error("failed to detect WorldGuard");
    }

    public static RegionContainer getRegionContainer(){
        return sWorldGuardPlugin.getRegionContainer();
    }
}

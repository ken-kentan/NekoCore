package jp.kentan.minecraft.neko_core.bridge;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;

public class WorldGuardProvider {

    private final RegionContainer REGION_CONTAINER;

    public WorldGuardProvider(WorldGuardPlugin worldGuardPlugin) {
        REGION_CONTAINER = worldGuardPlugin.getRegionContainer();
    }

    public ProtectedRegion getProtectedRegion(World world, String id){
        RegionManager regions = REGION_CONTAINER.get(world);

        if(regions == null){
            return null;
        }

        return regions.getRegion(id);
    }
}

package jp.kentan.minecraft.neko_core.bridge;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;

public class WorldGuardProvider {

    private final RegionContainer CONTAINER;

    public WorldGuardProvider() {
        CONTAINER = WorldGuard.getInstance().getPlatform().getRegionContainer();

        if (CONTAINER == null) {
            throw new RuntimeException("RegionContainer is null");
        }
    }

    public ProtectedRegion getProtectedRegion(World world, String id){
        RegionManager regions = CONTAINER.get(BukkitAdapter.adapt(world));

        if(regions == null){
            return null;
        }

        return regions.getRegion(id);
    }
}

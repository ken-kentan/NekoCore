package jp.kentan.minecraft.neko_core.spawn;

import org.bukkit.Location;

public class SpawnLocation {
    private final String NAME;
    private final Location LOCATION;

    public SpawnLocation(String name, Location location){
        NAME = name;
        LOCATION = location;
    }

    String getName() {
        return NAME;
    }

    Location getLocation(){
        return LOCATION;
    }
}

package jp.kentan.minecraft.neko_core.component;

import org.bukkit.Location;

public class SpawnLocation {
    public final String NAME;
    public final Location LOCATION;

    public SpawnLocation(String name, Location location){
        NAME = name;
        LOCATION = location;
    }
}

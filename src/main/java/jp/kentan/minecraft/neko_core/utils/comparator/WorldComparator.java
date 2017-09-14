package jp.kentan.minecraft.neko_core.utils.comparator;

import org.bukkit.World;

import java.util.Comparator;

public class WorldComparator implements Comparator<World> {
    private static WorldComparator sInstance;

    public static WorldComparator getInstance(){
        if(sInstance == null){
            sInstance = new WorldComparator();
        }

        return sInstance;
    }

    @Override
    public int compare(World w1, World w2) {
        return w1.getName().compareTo(w2.getName());
    }
}

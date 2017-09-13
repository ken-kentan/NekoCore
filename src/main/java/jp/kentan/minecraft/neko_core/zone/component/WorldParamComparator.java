package jp.kentan.minecraft.neko_core.zone.component;

import java.util.Comparator;

public class WorldParamComparator implements Comparator<WorldParam> {
    private static WorldParamComparator sInstance;

    public static WorldParamComparator getInstance(){
        if(sInstance == null){
            sInstance = new WorldParamComparator();
        }

        return sInstance;
    }

    @Override
    public int compare(WorldParam w1, WorldParam w2) {
        return w1.getWorldName().compareTo(w2.getWorldName());
    }
}

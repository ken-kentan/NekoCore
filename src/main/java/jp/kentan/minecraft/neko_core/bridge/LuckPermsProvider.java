package jp.kentan.minecraft.neko_core.bridge;

import jp.kentan.minecraft.neko_core.util.Log;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.*;
import me.lucko.luckperms.api.event.EventBus;

import java.util.UUID;

public class LuckPermsProvider {

    private static LuckPermsApi sLuckPermsApi;

    public static void setup(){
        detectLickPerms();
    }

    private static void detectLickPerms(){
        try {
            sLuckPermsApi = LuckPerms.getApi();
            Log.info("LuckPerms detected.");
        } catch (Exception e){
            Log.error("failed to detect LuckPerms.");
        }
    }

    public static User getUser(UUID uuid){
        return sLuckPermsApi.getUser(uuid);
    }

    public static Group getGroup(String name){
        return sLuckPermsApi.getGroup(name);
    }

    public static Storage getStorage(){
        return sLuckPermsApi.getStorage();
    }

    public static EventBus getEventBus(){
        return sLuckPermsApi.getEventBus();
    }

    public static Node getNodeByGroupName(String name){
        final Group group = sLuckPermsApi.getGroup(name);

        if(group != null){
            return sLuckPermsApi.getNodeFactory().makeGroupNode(group).build();
        }else{
            return null;
        }
    }
}

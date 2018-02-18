package jp.kentan.minecraft.neko_core.event;

import org.bukkit.entity.Player;

public interface AdvertisementEvent {
    void onPlayerJoin(Player player);
    void onPlayerQuit(Player player);
}

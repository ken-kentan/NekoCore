package jp.kentan.minecraft.neko_core.event;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public interface ZoneEvent {
    void onPlayerJoin(Player player);

    void onSignPlace(SignChangeEvent event);

    void onSignBreak(Player player, Sign sign);

    void onSignClick(Player player, Sign sign);
}

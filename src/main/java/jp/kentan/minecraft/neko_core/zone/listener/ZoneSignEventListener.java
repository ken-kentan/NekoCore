package jp.kentan.minecraft.neko_core.zone.listener;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;

public interface ZoneSignEventListener {
    void onSignPlace(SignChangeEvent event);

    void onSignBreak(Player player, Sign sign);

    void onSignClick(Player player, Sign sign);
}

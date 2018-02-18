package jp.kentan.minecraft.neko_core.component.zone;

import org.bukkit.ChatColor;

public enum AreaState {
    ON_SALE(ChatColor.RED + "販売中"),
    SOLD(ChatColor.GOLD + "契約済み"),
    LOCK(ChatColor.DARK_GRAY + "ロック中");

    private final String NAME;

    AreaState(String name) {
        NAME = name;
    }

    public String getName() {
        return NAME;
    }
}

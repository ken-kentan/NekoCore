package jp.kentan.minecraft.neko_core.component;

import org.bukkit.ChatColor;

public enum WeatherState {
    SUN(ChatColor.GOLD + "晴れ"),
    RAIN(ChatColor.DARK_AQUA + "雨");

    private final String NAME;

    WeatherState(String name) {
        NAME = name + ChatColor.RESET;
    }

    public String getName() {
        return NAME;
    }
}

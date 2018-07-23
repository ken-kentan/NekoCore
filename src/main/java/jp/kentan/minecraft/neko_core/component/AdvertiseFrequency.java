package jp.kentan.minecraft.neko_core.component;

import org.bukkit.ChatColor;

public enum AdvertiseFrequency {
    OFF(0, "&7停止"),
    LOW(6, "&6低"),
    MIDDLE(3, "&b中"),
    HIGH(1, "&9高");

    private final int INTERVAL_GAIN;
    private final String NAME;

    AdvertiseFrequency(int gain, String name) {
        INTERVAL_GAIN = gain;
        NAME = ChatColor.translateAlternateColorCodes('&', name);
    }

    public int getIntervalGain() {
        return INTERVAL_GAIN;
    }

    public String getName() {
        return NAME;
    }
}

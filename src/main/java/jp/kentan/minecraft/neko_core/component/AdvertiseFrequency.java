package jp.kentan.minecraft.neko_core.component;

import org.bukkit.ChatColor;

public enum AdvertiseFrequency {
    OFF(0, "&7停止"),
    LOUNGE(6, "&6たまに"),
    NORMAL(3, "&bふつう"),
    BUSY(1, "&9たくさん");

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

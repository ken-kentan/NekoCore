package jp.kentan.minecraft.neko_core.component.zone;

import org.bukkit.ChatColor;
import org.bukkit.World;

public class Zone {

    public String ID, NAME;
    public final ZoneType TYPE;
    public final World WORLD;
    public final int OWNER_LIMIT;
    public final int RENTAL_DAYS;
    public final double PRICE_RATE, PRICE_RATE_GAIN, SELL_GAIN;

    public String BUY_RENTAL_RULE, SELL_RULE;

    public Zone(String id,
                String name,
                ZoneType type,
                World world,
                int ownerLimit,
                int rentalDays,
                double priceRate,
                double priceRateGain,
                double sellGain,
                String buyRentalRule,
                String sellRule) {
        ID = id;
        NAME = ChatColor.translateAlternateColorCodes('&', name);
        TYPE = type;
        WORLD = world;
        OWNER_LIMIT = ownerLimit;
        RENTAL_DAYS = rentalDays;
        PRICE_RATE = priceRate;
        PRICE_RATE_GAIN = priceRateGain;
        SELL_GAIN = sellGain;

        BUY_RENTAL_RULE = (buyRentalRule != null) ? ChatColor.translateAlternateColorCodes('&', buyRentalRule) : null;
        SELL_RULE       = (sellRule      != null) ? ChatColor.translateAlternateColorCodes('&', sellRule     ) : null;
    }
}

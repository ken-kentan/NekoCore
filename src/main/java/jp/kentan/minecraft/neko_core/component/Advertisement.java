package jp.kentan.minecraft.neko_core.component;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.time.ZonedDateTime;
import java.util.UUID;

public class Advertisement {

    public final OfflinePlayer OWNER;
    public final String CONTENT;
    public final ZonedDateTime CREATED_DATE, EXPIRE_DATE;
    public final boolean IS_DELETE;

    public Advertisement(
            UUID ownerUuid,
            String content,
            ZonedDateTime createdDate,
            ZonedDateTime expireDate,
            boolean isDelete
    ) {
        OWNER = Bukkit.getOfflinePlayer(ownerUuid);
        CONTENT = ChatColor.translateAlternateColorCodes('&', content);
        CREATED_DATE = createdDate;
        EXPIRE_DATE = expireDate;
        IS_DELETE = isDelete;
    }
}

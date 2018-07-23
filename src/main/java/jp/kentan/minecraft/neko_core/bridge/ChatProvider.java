package jp.kentan.minecraft.neko_core.bridge;

import net.milkbowl.vault.chat.Chat;
import org.bukkit.entity.Player;

public class ChatProvider {

    private final Chat CHAT;

    public ChatProvider(Chat chat){
        CHAT = chat;
    }

    public String getPlayerPrefix(Player player){
        return CHAT.getPlayerPrefix(player);
    }
}

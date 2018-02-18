package jp.kentan.minecraft.neko_core.component;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class ServerVoteReward {

    public final String NAME;
    private final List<String> COMMAND_LIST = new ArrayList<>();

    public ServerVoteReward(String name, List<String> commandList) {
        NAME = ChatColor.translateAlternateColorCodes('&', name);
        COMMAND_LIST.addAll(commandList);
    }

    public List<String> getCommandList() {
        return COMMAND_LIST;
    }
}

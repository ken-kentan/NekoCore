package jp.kentan.minecraft.neko_core.vote.reward;


import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Reward {
    final String NAME;
    final private List<String> COMMAND_LIST = new ArrayList<>();

    public Reward(String name, List<String> commandList){
        NAME = ChatColor.translateAlternateColorCodes('&', name);
        COMMAND_LIST.addAll(commandList);
    }

    List<String> getCommandList(){return COMMAND_LIST;}
}

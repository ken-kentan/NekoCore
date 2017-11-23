package jp.kentan.minecraft.neko_core.vote.reward;


import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Reward {
    final String NAME;
    private List<String> mCommandList = new ArrayList<>();

    public Reward(String name, List<String> commandList){
        NAME = ChatColor.translateAlternateColorCodes('&', name);
        mCommandList.addAll(commandList);
    }

    List<String> getCommandList(){return mCommandList;}
}

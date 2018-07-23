package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.manager.SpawnManager;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommandExecutor implements CommandExecutor, TabCompleter {

    private final SpawnManager MANAGER;

    public SpawnCommandExecutor(SpawnManager spawnManager) {
        MANAGER = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!Util.isPlayer(sender)){
            return true;
        }

        Player player = (Player)sender;

        String spawnName = (args.length > 0) ? args[0] : "default";

        MANAGER.addSpawnTask(player, spawnName);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 0) {
            return MANAGER.getSpawnNameList();
        }

        List<String> completeList = new ArrayList<>();

        if (args.length == 1) {
            MANAGER.getSpawnNameList().forEach(cmdArg -> {
                if (cmdArg.startsWith(args[0])) {
                    completeList.add(cmdArg);
                }
            });
        }

        return completeList;
    }
}

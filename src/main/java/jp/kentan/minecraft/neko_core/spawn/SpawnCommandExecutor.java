package jp.kentan.minecraft.neko_core.spawn;

import jp.kentan.minecraft.neko_core.util.NekoUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class SpawnCommandExecutor implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!NekoUtil.isPlayer(sender)){
            return true;
        }

        Player player = (Player)sender;
        String locationName = (args.length > 0) ? args[0] : "default";

        if(command.getName().equals("spawn")){
            SpawnManager.addSpawnTask(player, locationName);
        }else{
            SpawnManager.saveSpawn(player, locationName);
        }

        return true;
    }
}

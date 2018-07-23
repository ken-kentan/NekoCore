package jp.kentan.minecraft.neko_core.command;

import jp.kentan.minecraft.neko_core.NekoCore;
import jp.kentan.minecraft.neko_core.manager.SpawnManager;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawnCommandExecutor implements CommandExecutor {

    private final SpawnManager MANAGER;

    public SetSpawnCommandExecutor(SpawnManager spawnManager) {
        MANAGER = spawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!Util.isPlayer(sender)){
            return true;
        }

        Player player = (Player)sender;

        if (args.length <= 0) {
           player.sendMessage(NekoCore.PREFIX + ChatColor.YELLOW + "スポーン名を入力して下さい.");
           return true;
        }

        MANAGER.saveSpawn(player, args[0]);

        return true;
    }
}

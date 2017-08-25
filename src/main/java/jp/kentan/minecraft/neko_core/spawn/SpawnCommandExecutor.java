package jp.kentan.minecraft.neko_core.spawn;

import jp.kentan.minecraft.neko_core.spawn.listener.SpawnCancelListener;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


public class SpawnCommandExecutor implements CommandExecutor {
    private SpawnManager mManager;

    public SpawnCommandExecutor(JavaPlugin plugin, SpawnManager manager){
        mManager = manager;

        plugin.getServer().getPluginManager().registerEvents(new SpawnCancelListener(mManager), plugin);

        plugin.getCommand("spawn").setExecutor(this);
        plugin.getCommand("setspawn").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        final int params = args.length;

        if(!NekoUtils.isPlayer(sender)){
            return true;
        }

        Player player = (Player)sender;
        String locationName = (params > 0) ? args[0] : "default";

        if(command.getName().equals("spawn")){
            mManager.addSpawnTask(player, locationName);
        }else{

        }

        return true;
    }
}

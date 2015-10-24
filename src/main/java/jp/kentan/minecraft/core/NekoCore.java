package jp.kentan.minecraft.core;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NekoCore extends JavaPlugin {

	@Override
	public void onEnable() {
		getLogger().info(ChatColor.GREEN + "NekoCoreが有効化されました");
	}

	@Override
	public void onDisable() {
		getLogger().info(ChatColor.YELLOW + "NekoCoreが無効化されました");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("basic")) {
			// プレイヤーが /basic を実行すると、この部分の処理が実行されます...
			return true;
		} else if (cmd.getName().equalsIgnoreCase("basic2")) {
			// プレイヤーが /basic2 を実行すると、この部分の処理が実行されます...
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
			} else {
				Player player = (Player) sender;
				
				getLogger().info(player + "が/basic2を実行しました。");
				// コマンドの実行処理...
			}
			return true;
		}
		return false;
	}
}

package jp.kentan.minecraft.core;

import java.awt.Desktop;
import java.net.URI;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {

		switch (cmd.getName()) {
		case "neko":
			sender.sendMessage("にゃーん");
			break;
		case "wiki":
			try {
				openURL("http://www27.atwiki.jp/dekitateserver_neko/");
			} catch (Exception e) {
				sender.sendMessage(ChatColor.RED + "コマンドを正常に実行できませんでした");
				getLogger().info(e.toString());
				return false;
			}
			break;
		case "map":
			break;
		}
		return true;
	}

	public static void openURL(String _url) throws Exception {

		// Get client's desktop
		Desktop d = Desktop.getDesktop();

		// Use default browser to connect to the following URL
		d.browse(new URI("[url]" + _url + "[/url]"));

	}
}

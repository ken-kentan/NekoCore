package jp.kentan.minecraft.core;

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
			sender.sendMessage(ChatColor.GOLD + "にゃーん");
			getLogger().info(ChatColor.GOLD + "にゃーん");
			break;
		case "wiki":
			try {
				openURL(sender, "http://www27.atwiki.jp/dekitateserver_neko/");
			} catch (Exception e) {
				doError(sender, e);
				return false;
			}
			break;
		case "map":
			try {
				openURL(sender, "http://minecraft.kentan.jp:8123/");
			} catch (Exception e) {
				doError(sender, e);
				return false;
			}
			break;
		case "blog":
			try {
				openURL(sender, "http://blog.kentan.jp/category/minecraft/");
			} catch (Exception e) {
				doError(sender, e);
				return false;
			}
			break;
		case "hp":
			try {
				openURL(sender, "http://minecraft.kentan.jp/");
			} catch (Exception e) {
				doError(sender, e);
				return false;
			}
			break;
		case "twitter":
			try {
				openURL(sender, "https://twitter.com/ken_kentan/");
			} catch (Exception e) {
				doError(sender, e);
				return false;
			}
			break;
		}

		return true;
	}

	public static void openURL(CommandSender _sender, String _url){
		_sender.sendMessage(ChatColor.AQUA + _url);
		_sender.sendMessage(ChatColor.GRAY + "↑のアドレスをクリックして下さい");
	}

	public void doError(CommandSender _sender, Exception _e) {
		_sender.sendMessage(ChatColor.RED + "コマンドを正常に実行できませんでした");
		getLogger().info(_e.toString());
	}
}
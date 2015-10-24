package jp.kentan.minecraft.core;

import java.util.Random;

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
			Random rnd = new Random();
			int rand = rnd.nextInt(5);
			switch (rand) {
			case 0:
				sender.sendMessage(ChatColor.GOLD + "にゃーん (^・ω・^ )");
				getLogger().info("にゃーん 0");
				break;
			case 1:
				sender.sendMessage(ChatColor.GOLD + "にゃーん ฅ(●´ω｀●)ฅ");
				getLogger().info("にゃーん 1");
				break;
			case 2:
				sender.sendMessage(ChatColor.GOLD + "にゃーん ฅ⊱*•ω•*⊰ฅ");
				getLogger().info("にゃーん 2");
				break;
			case 3:
				sender.sendMessage(ChatColor.GOLD + "にゃーん ฅ(^ω^ฅ)");
				getLogger().info("にゃーん 3");
				break;
			case 4:
				sender.sendMessage(ChatColor.GOLD + "(ฅ`･ω･´)っ= にゃんぱーんち！");
				getLogger().info("にゃーん 4");
				break;
			}
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

	public static void openURL(CommandSender _sender, String _url) {
		_sender.sendMessage(" " + ChatColor.AQUA + "" + ChatColor.UNDERLINE
				+ _url);
		_sender.sendMessage(" " + ChatColor.GRAY + "↑のアドレスをクリックして下さい");
	}

	public void doError(CommandSender _sender, Exception _e) {
		_sender.sendMessage(ChatColor.RED + "コマンドを正常に実行できませんでした");
		getLogger().info(_e.toString());
	}
}
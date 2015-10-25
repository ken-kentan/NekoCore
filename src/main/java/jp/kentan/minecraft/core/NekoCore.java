package jp.kentan.minecraft.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

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
		case "report":
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください");
			} else {
				if (args.length > 1) {
					sender.sendMessage(ChatColor.RED + "文にスペースを挟まないでください");
					sender.sendMessage(ChatColor.RED + "コマンドを正常に実行できませんでした");
					return false;
				} else if (args.length == 0) {
					sender.sendMessage(ChatColor.RED + "報告内容を記入してください");
					sender.sendMessage(ChatColor.RED + "コマンドを正常に実行できませんでした");
					return false;
				}

				Player player = (Player) sender;
				try {
					File file = new File("plugins/NekoCore/report.txt");

					if (checkBeforeWritefile(file)) {
						FileWriter filewriter = new FileWriter(file, true);
						
						Calendar calendar = Calendar.getInstance();

						filewriter.write("[" + calendar.getTime().toString() + "]"+ player.toString() + ":" + args[0] + "\r\n");

						filewriter.close();
						
						sender.sendMessage(ChatColor.AQUA + "報告を正常に受け付けました！");
						getLogger().info(player.toString() + " からレポートが送信されました");
					} else {
						sender.sendMessage(ChatColor.RED + "報告内容を記録できませんでした");
						sender.sendMessage(ChatColor.RED + "コマンドを正常に実行できませんでした");
						getLogger().info("レポートをファイルに書き込めませんでした");
						
						return false;
					}
				} catch (IOException e) {
					doError(sender, e);
					return false;
				}
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

	private static boolean checkBeforeWritefile(File file) {
		if (file.exists()) {
			if (file.isFile() && file.canWrite()) {
				return true;
			}
		}

		return false;
	}
}
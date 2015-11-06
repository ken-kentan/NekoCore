package jp.kentan.minecraft.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NekoCore extends JavaPlugin {
	private int online_player = 0, voted_player = 0, rain_minute = -1;
	private CommandSender cs_player[] =new CommandSender[20];

	@Override
	public void onEnable() {
		voteReset();
		new TpsMeter().runTaskTimer(this, 0, 1);
		getLogger().info("NekoCoreが有効化されました");
	}

	@Override
	public void onDisable() {
		getLogger().info("NekoCoreが無効化されました");
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
				sender.sendMessage(ChatColor.GOLD + "にゃーん ?(●´ω｀●)?");
				getLogger().info("にゃーん 1");
				break;
			case 2:
				sender.sendMessage(ChatColor.GOLD + "にゃーん ??*?ω?*??");
				getLogger().info("にゃーん 2");
				break;
			case 3:
				sender.sendMessage(ChatColor.GOLD + "にゃーん ?(^ω^?)");
				getLogger().info("にゃーん 3");
				break;
			case 4:
				sender.sendMessage(ChatColor.GOLD + "(?`･ω･´)っ= にゃんぱーんち！");
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
				return false;
			}

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

					filewriter.write("[" + calendar.getTime().toString() + "]"
							+ player.toString() + ":" + args[0] + "\r\n");

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
			break;
		case "server":
			double tps = TpsMeter.tps;
			double per = 0;

			per = 100 - (tps * 5);// Conver to 0~100%

			showLoad(sender, per);
			break;
		case "stoprain":
			int last_minute = 5;
			
			Calendar now = Calendar.getInstance();

			// show only stats
			if (args.length == 0) {
				
				for(int i = 0;cs_player[i] != null ;i++){
					if(cs_player[i] == sender){
						sender.sendMessage(ChatColor.RED + "多重投票です！");
						return true;
					}
				}

				cs_player[voted_player] = sender;
				online_player = Bukkit.getServer().getOnlinePlayers().size();
				voted_player++;

				if (rain_minute == -1)
					rain_minute = now.get(now.MINUTE);

				// Reset 5 minutes
				if (now.get(now.MINUTE) - rain_minute >= 5 || (now.get(now.MINUTE) + (60 - rain_minute) >= 5) && (rain_minute > 55)) {
					for(int i = 0;cs_player[i] != null ;i++){
						cs_player[i].sendMessage(ChatColor.RED + "投票開始から5分以上経過しました。");
						cs_player[i].sendMessage(ChatColor.RED + "投票をリセットします。");
					}
					
					voteReset();

					return true;
				}

				if (voted_player >= online_player / 2) {
					for(int i = 0;cs_player[i] != null ;i++){
						((Entity) cs_player[i]).getWorld().setStorm(false); // 雨を止める
						((Entity) cs_player[i]).getWorld().setThundering(false); // 落雷を止める
						
						cs_player[i].sendMessage(ChatColor.AQUA + "投票の結果、天候を晴れにしました。");
					}
					
					voteReset();

					getLogger().info("投票の結果、天候を晴れにしました。");

					return true;
				}
				
				if(rain_minute > 55) last_minute = 5 - (now.get(now.MINUTE) + (60 - rain_minute));
				else                 last_minute = 5 - (now.get(now.MINUTE) - rain_minute);

				// display stats
				sender.sendMessage(ChatColor.YELLOW + "天候投票に成功しました！");
				sender.sendMessage("現在の投票数：" + ChatColor.AQUA + " " + voted_player + "人");
				sender.sendMessage("残り投票時間：" + ChatColor.GREEN + " " + last_minute + "分");
				sender.sendMessage(ChatColor.GRAY + "ログインプレイヤーの50%が投票(/stoprain)することで天候が晴れになります。");
			} else {
				if (args[0].equals("stats")) {
					
					if(rain_minute == -1){
						sender.sendMessage(ChatColor.YELLOW + "投票が開始されていないためステータスを表示できません。");
						return true;
					}
					
					if(rain_minute > 55) last_minute = 5 - (now.get(now.MINUTE) + (60 - rain_minute));
					else                 last_minute = 5 - (now.get(now.MINUTE) - rain_minute);
					
					if(last_minute > 5 || last_minute < 0){
						for(int i = 0;cs_player[i] != null ;i++){
							cs_player[i].sendMessage(ChatColor.RED + "投票開始から5分以上経過しました。");
							cs_player[i].sendMessage(ChatColor.RED + "投票をリセットします。");
						}
						
						voteReset();

						return true;						
					}
					
					sender.sendMessage("現在の投票数：" + ChatColor.AQUA + " " + voted_player + "人");
					sender.sendMessage("残り投票時間：" + ChatColor.GREEN + " " + last_minute + "分");
					sender.sendMessage(ChatColor.GRAY + "ログインプレイヤーの50%が投票(/stoprain)することで天候が晴れになります。");

					return true;
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

	public void showLoad(CommandSender _sender, double _per) {
		String str_per = String.format("%.2f%%", _per);

		if (_per <= 5)
			_sender.sendMessage("現在のサーバー負荷率は " + ChatColor.AQUA + str_per
					+ ChatColor.WHITE + " です");
		else if (_per <= 10)
			_sender.sendMessage("現在のサーバー負荷率は " + ChatColor.GREEN + str_per
					+ ChatColor.WHITE + " です");
		else if (_per <= 20)
			_sender.sendMessage("現在のサーバー負荷率は " + ChatColor.YELLOW + str_per
					+ ChatColor.WHITE + " です");
		else {
			_sender.sendMessage("現在のサーバー負荷率は " + ChatColor.RED + str_per
					+ ChatColor.WHITE + " です");
			_sender.sendMessage("サーバーに負荷がかかる行為を中止してください。");
		}
	}

	public void doError(CommandSender _sender, Exception _e) {
		_sender.sendMessage(ChatColor.RED + "コマンドを正常に実行できませんでした");
		getLogger().info(_e.toString());
	}
	
	public void voteReset(){
		voted_player = 0;
		rain_minute = -1;
		
		for(int i = 0;i < 20;i++){
			cs_player[i] = null;
		}

		getLogger().info("stoprainの投票がリセットされました");
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
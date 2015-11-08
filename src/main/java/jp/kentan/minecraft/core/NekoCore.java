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
	private int online_player = 0, voted_player = 0, first_vote = -1;
	private CommandSender cs_player[] =new CommandSender[20];

	@Override
	public void onEnable() {
		voteReset();
		new TpsMeter().runTaskTimer(this, 0, 1);
		getLogger().info("NekoCoreを有効にしました");
	}

	@Override
	public void onDisable() {
		getLogger().info("NekoCoreを無効にしました");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
		if(cmd.getName().equals("neko") || cmd.getName().equals("nk")){
			
			//Show HELP (/nk <help>)
			if(args.length == 0 || args[0].equals("help")){
				showHelp(sender);
				return true;
			}
			
			switch (args[0]){
			case "nyan":
				showNyan(sender);
				break;
			case "wiki":
				showURL(sender,"http://www27.atwiki.jp/dekitateserver_neko/");
				break;
			case "map":
				showURL(sender,"http://minecraft.kentan.jp:8123/");
				break;
			case "blog":
				showURL(sender,"http://blog.kentan.jp/category/minecraft/");
				break;
			case "hp":
				showURL(sender,"http://minecraft.kentan.jp/");
				break;
			case "twitter":
			case "tw":
				showURL(sender,"https://twitter.com/ken_kentan/");
				break;
			case "report":
				if(checkInGame(sender) == false){
					sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
					return false;
				}
				
				if(checkReportFormat(sender, args.length) == false) return true;
				
				writeReport(sender, args[1]);
				
				break;
			case "server":
				showLoad(sender, 100 - (TpsMeter.tps * (double)5));
				break;
			case "vote":
				Calendar calendar = Calendar.getInstance();
				
				if(args.length > 1 && args[1].equals("stats")){
					showVoteStats(sender, calendar);
					return true;
				}
				
				if(checkInGame(sender) == false){
					sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
					return false;
				}
				
				voteProcess(sender, calendar);
				
				break;
			}
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
	
	private boolean checkInGame(CommandSender _sender){
		if (!(_sender instanceof Player)) return false;
		else                              return true;
	}
	
	private void showHelp(CommandSender _sender){
		_sender.sendMessage("---------- NekoCoreコマンドヘルプ ----------");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko nyan" + ChatColor.WHITE + " にゃーん。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko wiki" + ChatColor.WHITE + " wikiのURLを表示します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko map" + ChatColor.WHITE + " DynmapのURLを表示します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko blog" + ChatColor.WHITE + " BlogのURLを表示します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko hp" + ChatColor.WHITE + " ホームページのURLを表示します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko <twitter|tw>" + ChatColor.WHITE + " ServerAdminのTwitterURLを表示します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko report <報告文>" + ChatColor.WHITE + " 運営に<報告文>を送信します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko server" + ChatColor.WHITE + " 現在の猫鯖の負荷率を表示します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko vote" + ChatColor.WHITE + " ワールドの天気投票を行います。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko vote stats" + ChatColor.WHITE + " 天気投票のステータスを表示します。");
		_sender.sendMessage("| " + ChatColor.GRAY + "/neko は /nk と省略することも可能です。");
		_sender.sendMessage("---------------------------------------");
	}
	
	private void showNyan(CommandSender _sender){
		Random random = new Random();
		int rand = random.nextInt(5);
		switch (rand) {
		case 0:
			_sender.sendMessage(ChatColor.GOLD + " にゃーん (^・ω・^ )");
			getLogger().info("にゃーん 0");
			break;
		case 1:
			_sender.sendMessage(ChatColor.GOLD + " にゃーん ฅ(●´ω｀●)ฅ");
			getLogger().info("にゃーん 1");
			break;
		case 2:
			_sender.sendMessage(ChatColor.GOLD + " にゃーん ฅ⊱*•ω•*⊰ฅ");
			getLogger().info("にゃーん 2");
			break;
		case 3:
			_sender.sendMessage(ChatColor.GOLD + " にゃーん ฅ(^ω^ฅ)");
			getLogger().info("にゃーん 3");
			break;
		case 4:
			_sender.sendMessage(ChatColor.GOLD + " (ฅ`･ω･´)っ= にゃんぱーんち！");
			getLogger().info("にゃーん 4");
		}
	}
	
	private static void showURL(CommandSender _sender, String _url) {
		_sender.sendMessage("| " + ChatColor.AQUA + "" + ChatColor.UNDERLINE + _url);
		_sender.sendMessage("| " + ChatColor.GRAY + "↑のアドレスをクリックして下さい。");
	}
	
	private boolean checkReportFormat(CommandSender _sender, int args_length){
		
		if(args_length == 2) return true;
		
		if (args_length > 2)       _sender.sendMessage("| " + ChatColor.RED + "文にスペースを挟まないでください。");
		else if (args_length == 1) _sender.sendMessage("| " + ChatColor.RED + "報告文を記入してください。");
		
		_sender.sendMessage("| " + ChatColor.GRAY + "上記を修正して再度送信してください。");
		
		return false;
		
	}
	
	private void writeReport(CommandSender _sender, String _str){
		try {
			File file = new File("plugins/NekoCore/report.txt");

			if (checkBeforeWritefile(file)) {
				FileWriter filewriter = new FileWriter(file, true);

				Calendar calendar = Calendar.getInstance();

				filewriter.write("[" + calendar.getTime().toString() + "]" + _sender.getName() + ":" + _str + "\r\n");

				filewriter.close();

				_sender.sendMessage("| " + ChatColor.AQUA + "報告を正常に受け付けました！");
				getLogger().info(_sender.getName() + " からレポートが送信されました");
			} else {
				_sender.sendMessage("| " + ChatColor.RED + "報告内容を記録できませんでした。");
				_sender.sendMessage("| " + ChatColor.GRAY + "再度、送信するか/mailをご利用ください。");
				getLogger().info("レポートをファイルに書き込めませんでした");
			}
		} catch (IOException e) {
			doError(_sender, e);
		}
	}
	
	private void showLoad(CommandSender _sender, double _per) {
		String str_per = String.format("%.2f%%", _per);

		if (_per <= 5){
			_sender.sendMessage("| 現在のサーバー負荷率は " + ChatColor.AQUA + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage("| " + ChatColor.GRAY + "サーバーは快適に動作しています。");
		}
		else if (_per <= 10){
			_sender.sendMessage("| 現在のサーバー負荷率は " + ChatColor.GREEN + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage("| " + ChatColor.GRAY + "サーバーは正常に動作しています。");
		}
		else if (_per <= 20){
			_sender.sendMessage("| 現在のサーバー負荷率は " + ChatColor.YELLOW + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage("| " + ChatColor.GRAY + "サーバーに少し負荷がかかっています。");
		}
		else {
			_sender.sendMessage("| 現在のサーバー負荷率は " + ChatColor.RED + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage("| " + ChatColor.GRAY + "サーバーに負荷がかかる行為を中止してください。");
		}
	}
	
	private void showVoteStats(CommandSender _sender, Calendar _calendar){
		int last_minute = 0;
		boolean find_player = false;
	
		if(first_vote == -1){
			_sender.sendMessage("| " + ChatColor.YELLOW + "投票が開始されていないためステータスを表示できません。");
			_sender.sendMessage("| " + ChatColor.GRAY + "天候を晴れにしたい場合は /neko vote を実行してください。");
			return;
		}
		
		if(first_vote > 55 && _calendar.get(_calendar.MINUTE) <= 10) last_minute = 5 - (_calendar.get(_calendar.MINUTE) + (60 - first_vote));
		else                                                          last_minute = 5 - (_calendar.get(_calendar.MINUTE) - first_vote);
		
		if(last_minute > 5 || last_minute < 0){
			
			for(int i = 0;cs_player[i] != null ;i++){
				
				if(cs_player[i] == _sender) find_player = true;
				
				voteResetMessage(cs_player[i]);
			}
			
			if(find_player == false) voteResetMessage(_sender);
			
			voteReset();

			return;						
		}
		
		_sender.sendMessage("| " + "現在の投票数：" + ChatColor.AQUA + " " + voted_player + "人");
		_sender.sendMessage("| " + "残り投票時間：" + ChatColor.GREEN + " " + last_minute + "分");
		_sender.sendMessage("| " + ChatColor.GRAY + "ログインプレイヤーの50%が投票(/neko vote)することで天候が晴れになります。");
	}
	
	private void voteProcess(CommandSender _sender, Calendar _calendar){
		int last_minute = 0;
		
		for(int i = 0;cs_player[i] != null ;i++){
			if(cs_player[i] == _sender){
				_sender.sendMessage("| " + ChatColor.RED + "多重投票です！");
				_sender.sendMessage("| " + ChatColor.GRAY + "投票状況は /neko vote stats で確認できます。");
				return;
			}
		}

		cs_player[voted_player] = _sender;
		online_player = Bukkit.getServer().getOnlinePlayers().size();
		voted_player++;

		if (first_vote == -1)
			first_vote = _calendar.get(_calendar.MINUTE);

		// Reset 5 minutes
		if (_calendar.get(_calendar.MINUTE) - first_vote >= 5 || (_calendar.get(_calendar.MINUTE) + (60 - first_vote) >= 5) && (first_vote > 55 && _calendar.get(_calendar.MINUTE) <= 10)) {
			for(int i = 0;cs_player[i] != null ;i++){
				voteResetMessage(cs_player[i]);
			}
			
			voteReset();

			return;
		}

		if (voted_player >= online_player / 2) {
			for(int i = 0;cs_player[i] != null ;i++){
				((Entity) cs_player[i]).getWorld().setStorm(false); // 雨を止める
				((Entity) cs_player[i]).getWorld().setThundering(false); // 落雷を止める
				
				cs_player[i].sendMessage("| " + ChatColor.AQUA + "投票の結果、天候を晴れにしました。");
			}
			
			voteReset();

			getLogger().info("投票の結果、天候を晴れにしました。");

			return;
		}
		
		if(first_vote > 55) last_minute = 5 - (_calendar.get(_calendar.MINUTE) + (60 - first_vote));
		else                 last_minute = 5 - (_calendar.get(_calendar.MINUTE) - first_vote);

		_sender.sendMessage("| " + ChatColor.AQUA + "天候投票に成功しました！");
		_sender.sendMessage("| 現在の投票数：" + ChatColor.AQUA + " " + voted_player + "人");
		_sender.sendMessage("| 残り投票時間：" + ChatColor.GREEN + " " + last_minute + "分");
		_sender.sendMessage("| " + ChatColor.GRAY + "ログインプレイヤーの50%が投票(/neko vote)することで天候が晴れになります。");
		_sender.sendMessage("| " + ChatColor.GRAY + "投票状況は /neko vote stats で確認できます。");
	}
	
	private void voteReset(){
		voted_player = 0;
		first_vote = -1;
		
		for(int i = 0;i < 20;i++){
			cs_player[i] = null;
		}

		getLogger().info("天候投票がリセットされました");
	}
	
	private void voteResetMessage(CommandSender _sender){
		_sender.sendMessage("| " + ChatColor.YELLOW + "投票開始から5分以上経過しました。");
		_sender.sendMessage("| " + ChatColor.RED + "投票をリセットします。");
		_sender.sendMessage("| " + ChatColor.GRAY + "天候を晴れにしたい場合は再度 /neko vote を実行してください。");
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
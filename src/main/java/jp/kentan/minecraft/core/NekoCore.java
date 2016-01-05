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
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class NekoCore extends JavaPlugin {
	BukkitTask task;
	
	private int online_player = 0, voted_player = 0, sec_time = 0;
	private CommandSender cs_player[] =new CommandSender[20];

	@Override
	public void onEnable() {
		voteReset();
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);
		
		new BukkitRunnable()
		{
		    @Override
		    public void run()
		    {
		    	//run
		    	if(voted_player > 0) sec_time++;
				if(sec_time > 300){ //5m
					voteResetMessage();
					voteReset();
					sec_time = 0;
				}
		    }
		}.runTaskTimer(this, 20, 20);//20 1s
		
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
				double tps = Lag.getTPS();
				double percentage =  Double.valueOf(String.format("%.2f", (100 - tps * 5)));
				showLoad(sender, Lag.getTPS());
				break;
			case "vote":
				
				if(args.length > 1 && args[1].equals("stats")){
					showVoteStats(sender);
					return true;
				}
				
				if(checkInGame(sender) == false){
					sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
					return false;
				}
				
				voteProcess(sender);
				
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
	
	private void showLoad(CommandSender _sender, double _tps) {
		
		if(_tps < 0) _tps = 0.0D;
		String str_per = String.format("%.2f%%", (100.0D - _tps * 5.0D));

		if (_tps >= 19.0D){
			_sender.sendMessage("| 現在のサーバー負荷率は " + ChatColor.AQUA + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage("| " + ChatColor.GRAY + "サーバーは快適に動作しています。");
		}
		else if (_tps >= 18.0D){
			_sender.sendMessage("| 現在のサーバー負荷率は " + ChatColor.GREEN + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage("| " + ChatColor.GRAY + "サーバーは正常に動作しています。");
		}
		else if (_tps >= 17.0D){
			_sender.sendMessage("| 現在のサーバー負荷率は " + ChatColor.YELLOW + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage("| " + ChatColor.GRAY + "サーバーに少し負荷がかかっています。");
		}
		else {
			_sender.sendMessage("| 現在のサーバー負荷率は " + ChatColor.RED + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage("| " + ChatColor.GRAY + "サーバーに負荷がかかる行為を中止してください。");
		}
	}
	
	private void showVoteStats(CommandSender _sender){
	
		if(voted_player == 0){
			_sender.sendMessage("| " + ChatColor.YELLOW + "投票が開始されていないためステータスを表示できません。");
			_sender.sendMessage("| " + ChatColor.GRAY + "天候を晴れにしたい場合は /neko vote を実行してください。");
			return;
		}
		
		_sender.sendMessage("| " + "現在の投票数：" + ChatColor.AQUA + " " + voted_player + "人");
		_sender.sendMessage("| " + "残り投票時間：" + ChatColor.GREEN + " " + (300 - sec_time) + "秒");
		_sender.sendMessage("| " + ChatColor.GRAY + "ログインプレイヤーの50%が投票(/neko vote)することで天候が晴れになります。");
	}
	
	private void voteProcess(CommandSender _sender){
		
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

		if (voted_player == 1){
			for(Player player : Bukkit.getServer().getOnlinePlayers())
	        {
				player.sendMessage("[" + ChatColor.GOLD + "NekoCore" + ChatColor.WHITE + "]" + _sender.getName() + "さんが天候投票を開始しました。");
	        }
			
		}

		if (voted_player >= online_player / 2) {
			for(int i = 0;cs_player[i] != null ;i++){
				((Entity) cs_player[i]).getWorld().setStorm(false); // 雨を止める
				((Entity) cs_player[i]).getWorld().setThundering(false); // 落雷を止める
			}
			
			for(Player player : Bukkit.getServer().getOnlinePlayers())
	        {
				player.sendMessage("[" + ChatColor.GOLD + "NekoCore" + ChatColor.WHITE + "]" + ChatColor.AQUA + "投票の結果、天候を晴れにしました。");
	        }
			
			voteReset();

			getLogger().info("投票の結果、天候を晴れにしました。");

			return;
		}

		_sender.sendMessage("| " + ChatColor.AQUA + "天候投票に成功しました！");
		_sender.sendMessage("| 現在の投票数：" + ChatColor.AQUA + " " + voted_player + "人");
		_sender.sendMessage("| 残り投票時間：" + ChatColor.GREEN + " " + (300 - sec_time)  + "秒");
		_sender.sendMessage("| " + ChatColor.GRAY + "ログインプレイヤーの50%が投票(/neko vote)することで天候が晴れになります。");
		_sender.sendMessage("| " + ChatColor.GRAY + "投票状況は /neko vote stats で確認できます。");
	}
	
	private void voteReset(){
		voted_player = 0;
		
		for(int i = 0;i < 20;i++){
			cs_player[i] = null;
		}

		getLogger().info("天候投票がリセットされました");
	}
	
	private void voteResetMessage(){
		for(Player player : Bukkit.getServer().getOnlinePlayers())
        {
			player.sendMessage("[" + ChatColor.GOLD + "NekoCore" + ChatColor.WHITE + "]" + ChatColor.YELLOW + "投票開始から5分以上経過しました。");
			player.sendMessage("[" + ChatColor.GOLD + "NekoCore" + ChatColor.WHITE + "]" + ChatColor.RED + "投票をリセットします。");
			player.sendMessage("[" + ChatColor.GOLD + "NekoCore" + ChatColor.WHITE + "]" + ChatColor.GRAY + "天候を晴れにしたい場合は再度 /neko vote を実行してください。");
        }
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


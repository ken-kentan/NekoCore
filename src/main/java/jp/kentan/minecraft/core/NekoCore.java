package jp.kentan.minecraft.core;

import java.util.Calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class NekoCore extends JavaPlugin implements Listener{
	private ConfigManager config = null;
	private EconomyManager economy = null;
	private TwitterBot bot = null;
	private int online_player = 0, voted_player = 0, sec_time = 0, sec_reboot = -1;
	private CommandSender cs_player[] = new CommandSender[100];
	private static String nc_tag = ChatColor.GRAY + "[" + ChatColor.GOLD  + "Neko" + ChatColor.RED + "Core" + ChatColor.GRAY + "] " + ChatColor.WHITE;

	@Override
	public void onEnable() {
		voteReset();
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Lag(), 100L, 1L);
		getServer().getPluginManager().registerEvents(this, this);
		
		config  = new ConfigManager(this);
		economy = new EconomyManager(this, config);
		bot     = new TwitterBot(this, economy, config);
		
		bot.tweet("@ken_kentan\nSuccessfully launched. " + getDescription().getName() + "　v" +getDescription().getVersion());
		
		new BukkitRunnable()
		{
		    @Override
		    public void run()
		    {
		    	//run
		    	if(voted_player > 0) sec_time++;
		    	if(sec_reboot >= 0){
		    		reboot();
		    		sec_reboot--;
		    	}
				if(sec_time > 300){ //5m
					voteResetMessage();
					voteReset();
					sec_time = 0;
				}
				bot.eventHandler();
		    }
		}.runTaskTimer(this, 20, 20);//20 1s
		
		getLogger().info("NekoCore was launched!");
	}

	@Override
	public void onDisable() {
		bot.tweet("@ken_kentan\nShutdown... " + getDescription().getName() + "　v" +getDescription().getVersion());
		bot.closeStream();
		
		getLogger().info("NekoCore was disabled.");
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
				showURL(sender,"https://twitter.com/DekitateServer");
				break;
			case "report":
				if(checkInGame(sender) == false){
					sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
					return false;
				}
				
				if(!checkReportFormat(sender, args.length)) return true;
				
				Calendar calendar = Calendar.getInstance();
				
				bot.sendDM("ken_kentan", "[" + calendar.getTime().toString() + "]" + sender.getName() + ":" + args[1]);
				bot.tweet("@ken_kentan\nレポートが送信されたよ！詳しくはDMを確認してね" + TwitterBot.getNekoFace());
				
				break;
			case "server":
				showLoad(sender, Lag.getTPS());
				break;
			case "vote":
				
				if(args.length > 1 && args[1].equals("status")){
					showVoteStatus(sender);
					return true;
				}
				
				if(checkInGame(sender) == false){
					sender.sendMessage(ChatColor.RED + "このコマンドはゲーム内から実行してください。");
					return false;
				}
				
				voteProcess(sender);
				break;
			case "account":
				if(args.length > 1){
					if(config.saveLinkedTwitterAccount((Player)sender, args[1])){
						sender.sendMessage(nc_tag + "Twitterアカウント(@" + args[1] + ")のリンクに成功しました！");
					}else{
						sender.sendMessage(nc_tag + "Twitterアカウンのリンクに失敗しました...");
					}
				}else{
					sender.sendMessage(nc_tag + "TwitterIDを入力して下さい.");
				}
				break;
			}
			
			if(args[0].equals("admin") && (sender.getName().equals("ken_kentan") || !checkInGame(sender))){
				if(args[1] == null) args[1] = "null";
				
				switch (args[1]) {
				case "reboot":
					sender.sendMessage(nc_tag + "Run, Server reboot sequence.");
					rebootModule();
					break;
				case "cancel":
					sec_reboot = -1;
					for(Player player : Bukkit.getServer().getOnlinePlayers()) player.sendMessage(nc_tag + ChatColor.RED  + "サーバーの再起動がキャンセルされました。");
					break;
				case "bot":
					bot.switchBotStatus();
					break;
				case "reload":
					config.setTwitterBotData();
					sender.sendMessage(nc_tag + "設定ファルをリロードしました.");
					break;
				case "test":
					switch(getWeather()){
					case 0:
						getLogger().info("晴れ");
						break;
					case 1:
						getLogger().info("雨");
						break;
					case 2:
						getLogger().info("雷雨");
						break;
					}
				default:
					break;
				}
			}
		}			
		return true;
	}
	
	public void rebootModule(){
		if(sec_reboot < 0) sec_reboot = 300;
		bot.tweet("@ken_kentan\n5分後に再起動します.");
	}
	
	void reboot(){
		switch(sec_reboot){
		case 0:
			getServer().dispatchCommand(getServer().getConsoleSender(), "stop");
			break;
		case 5:
			getServer().dispatchCommand(getServer().getConsoleSender(), "save-all");
			break;
		default:
			if(sec_reboot % 60 == 0 || (sec_reboot < 60 && sec_reboot % 5 == 0) || (sec_reboot <= 5)){
				for(Player player : Bukkit.getServer().getOnlinePlayers()) player.sendMessage(nc_tag + ChatColor.RED + sec_reboot + "秒後にサーバーを再起動します。");
			}
			break;
		}
	}
	
	@EventHandler
    public void TweetLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String tweet = TwitterBot.getActionMsg();
        
        tweet = tweet.replace("{player}", player.getName());
        tweet = tweet.replace("{status}", "ログイン");
        tweet = tweet.replace("{face}", TwitterBot.getNekoFace());
     
        bot.tweet(tweet);
    }
	
	@EventHandler
    public void TweetLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String tweet = TwitterBot.getActionMsg();
        
        tweet = tweet.replace("{player}", player.getName());
        tweet = tweet.replace("{status}", "ログアウト");
        tweet = tweet.replace("{face}", TwitterBot.getNekoFace());
     
        bot.tweet(tweet);
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
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko <twitter|tw>" + ChatColor.WHITE + " ServerのTwitterURLを表示します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko report <報告文>" + ChatColor.WHITE + " 運営に<報告文>を送信します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko server" + ChatColor.WHITE + " 現在の猫鯖の負荷率を表示します。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko vote" + ChatColor.WHITE + " ワールドの天気投票を行います。");
		_sender.sendMessage("| " + ChatColor.GOLD + "/neko vote status" + ChatColor.WHITE + " 天気投票のステータスを表示します。");
		_sender.sendMessage("| " + ChatColor.GRAY + "/neko は /nk と省略することも可能です。");
		_sender.sendMessage("---------------------------------------");
	}
	
	private void showNyan(CommandSender _sender){
		_sender.sendMessage(ChatColor.GOLD + " にゃーん" + TwitterBot.getNekoFace());
		getLogger().info("にゃーん");
	}
	
	private static void showURL(CommandSender _sender, String _url) {
		_sender.sendMessage(nc_tag + ChatColor.AQUA + "" + ChatColor.UNDERLINE + _url);
		_sender.sendMessage(nc_tag + ChatColor.GRAY + "↑のアドレスをクリックして下さい。");
	}
	
	private boolean checkReportFormat(CommandSender _sender, int args_length){
		
		if(args_length == 2) return true;
		
		if (args_length > 2)       _sender.sendMessage(nc_tag + ChatColor.RED + "文にスペースを挟まないでください。");
		else if (args_length == 1) _sender.sendMessage(nc_tag + ChatColor.RED + "報告文を記入してください。");
		
		_sender.sendMessage(nc_tag + ChatColor.GRAY + "上記を修正して再度送信してください。");
		
		return false;
	}
	
	private void showLoad(CommandSender _sender, double _tps) {
		
		if(_tps > 20.0D) _tps = 20.0D;
		String str_per = String.format("%.2f%%", (100.0D - _tps * 5.0D));

		if (_tps >= 19.5D){
			_sender.sendMessage(nc_tag + "現在のサーバー負荷率は " + ChatColor.AQUA + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage(nc_tag + ChatColor.GRAY + "サーバーは快適に動作しています。");
		}
		else if (_tps >= 19.0D){
			_sender.sendMessage(nc_tag + "現在のサーバー負荷率は " + ChatColor.GREEN + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage(nc_tag + ChatColor.GRAY + "サーバーは正常に動作しています。");
		}
		else if (_tps >= 18.0D){
			_sender.sendMessage(nc_tag + "現在のサーバー負荷率は " + ChatColor.YELLOW + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage(nc_tag + ChatColor.GRAY + "サーバーに少し負荷がかかっています。");
		}
		else {
			_sender.sendMessage(nc_tag + "現在のサーバー負荷率は " + ChatColor.RED + str_per + ChatColor.WHITE + " です。");
			_sender.sendMessage(nc_tag + ChatColor.GRAY + "サーバーに負荷がかかる行為を中止してください。");
		}
	}
	
	private void showVoteStatus(CommandSender _sender){
	
		if(voted_player == 0){
			_sender.sendMessage(nc_tag + ChatColor.YELLOW + "投票が開始されていないためステータスを表示できません。");
			_sender.sendMessage(nc_tag + ChatColor.GRAY + "天候を晴れにしたい場合は /neko vote を実行してください。");
			return;
		}
		
		_sender.sendMessage(nc_tag + "現在の投票数：" + ChatColor.AQUA + " " + voted_player + "人");
		_sender.sendMessage(nc_tag + "残り投票時間：" + ChatColor.GREEN + " " + (300 - sec_time) + "秒");
		_sender.sendMessage(nc_tag + ChatColor.GRAY + "ログインプレイヤーの50%が投票すると天候が晴れになります。");
	}
	
	private void voteProcess(CommandSender _sender){
		
		for(int i = 0;cs_player[i] != null ;i++){
			if(cs_player[i] == _sender){
				_sender.sendMessage(nc_tag + ChatColor.RED + "多重投票です！");
				_sender.sendMessage(nc_tag + ChatColor.GRAY + "投票状況は /neko vote status で確認できます。");
				return;
			}
		}

		cs_player[voted_player] = _sender;
		online_player = Bukkit.getServer().getOnlinePlayers().size();
		voted_player++;

		if (voted_player == 1){
			for(Player player : Bukkit.getServer().getOnlinePlayers())
	        {
				player.sendMessage(nc_tag + _sender.getName() + "さんが天候投票を開始しました。");
	        }
			
		}

		if (voted_player >= online_player / 2) {
			for(int i = 0;cs_player[i] != null ;i++){
				((Entity) cs_player[i]).getWorld().setStorm(false); // 雨を止める
				((Entity) cs_player[i]).getWorld().setThundering(false); // 落雷を止める
			}
			
			for(Player player : Bukkit.getServer().getOnlinePlayers())
	        {
				player.sendMessage(nc_tag + ChatColor.AQUA + "投票の結果、天候を晴れにしました。");
	        }
			
			voteReset();

			getLogger().info("投票の結果、天候を晴れにしました。");

			return;
		}

		_sender.sendMessage(nc_tag + ChatColor.AQUA + "天候投票に成功しました！");
		_sender.sendMessage(nc_tag + "現在の投票数：" + ChatColor.AQUA + " " + voted_player + "人");
		_sender.sendMessage(nc_tag + "残り投票時間：" + ChatColor.GREEN + " " + (300 - sec_time)  + "秒");
		_sender.sendMessage(nc_tag + ChatColor.GRAY + "ログインプレイヤーの50%が投票すると天候が晴れになります。");
		_sender.sendMessage(nc_tag + ChatColor.GRAY + "投票状況は /neko vote status で確認できます。");
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
			player.sendMessage(nc_tag + ChatColor.YELLOW + "投票開始から5分以上経過しました。");
			player.sendMessage(nc_tag + ChatColor.RED + "投票がリセットされました。");
        }
	}
	
	public int getWeather(){
		World world = Bukkit.getWorlds().get(0);
		
		if(world.hasStorm()){
			if(world.isThundering()) return 2;
			else                     return 1;
		}
		
		return 0;
	}
}
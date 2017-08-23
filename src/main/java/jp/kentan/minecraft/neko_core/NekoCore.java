package jp.kentan.minecraft.neko_core;


import jp.kentan.minecraft.neko_core.config.ConfigManager;
import jp.kentan.minecraft.neko_core.vote.RewardManager;
import jp.kentan.minecraft.neko_core.twitter.bot.TwitterBot;
import jp.kentan.minecraft.neko_core.twitter.TwitterProvider;
import jp.kentan.minecraft.neko_core.utils.Log;
import jp.kentan.minecraft.neko_core.utils.NekoUtils;
import jp.kentan.minecraft.neko_core.vote.ServerVoteListener;
import jp.kentan.minecraft.neko_core.vote.WeatherVote;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class NekoCore extends JavaPlugin implements Listener{

    public static final String TAG = ChatColor.GRAY + "[" + ChatColor.GOLD + "Neko" + ChatColor.RED + "Core" + ChatColor.GRAY + "] " + ChatColor.WHITE;

    private ConfigManager mConfig;

    private TwitterProvider mTwitter;
    private WeatherVote mWeatherVote;

    @Override
    public void onEnable() {
        new Log(getLogger());

        mConfig = new ConfigManager(getDataFolder());
        mConfig.load();

        mTwitter = new TwitterProvider(this, mConfig.getTwitterConfig(), mConfig.getBotMessages());

        mWeatherVote = new WeatherVote(this);
        RewardManager rewardManager = new RewardManager(mConfig.getRewardConfig());

        getServer().getPluginManager().registerEvents(new ServerVoteListener(rewardManager), this);

        Log.print("onEnable");
    }

    @Override
    public void onDisable() {
        mTwitter.disable();
        Log.print("onDisable");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        final int params = args.length;

        if(cmd.getName().equals("vote")){
            printUrl(sender, "https://minecraft.kentan.jp/vote");
            return true;
        }

        if(params <= 0 || args[0].equals("help")){
            printHelp(sender);
            return true;
        }

        switch (args[0]){
            case "nyan":
                sender.sendMessage("にゃーん" + TwitterBot.getInstance().getNeko());
                break;
            case "wiki":
                printUrl(sender, "http://www27.atwiki.jp/dekitateserver_neko/");
                break;
            case "map":
                printUrl(sender, "http://minecraft.kentan.jp:8123/");
                break;
            case "blog":
                printUrl(sender, "http://blog.kentan.jp/category/minecraft/");
                break;
            case "hp":
                printUrl(sender, "http://minecraft.kentan.jp/");
                break;
            case "twitter":
            case "tw":
                printUrl(sender, "https://twitter.com/DekitateServer");
                break;
            case "vote":
                if(!checkPlayer(sender)) return true;

                mWeatherVote.vote(NekoUtils.toPlayer(sender));
                break;
            case "report":
                if(!checkPlayer(sender)) return true;

                sendReport(NekoUtils.toPlayer(sender), args);
                break;
            default:
                break;
        }

        if (isAdminCommand(sender, args)) {
            switch (args[1]) {
                case "reboot":
                    break;
                case "cancel":
                    break;
                case "bot":
                    break;
                case "reload":
                    mConfig.load();
                    sender.sendMessage(TAG + "設定ファルをリロードしました.");
                    break;
                default:
                    break;
            }
        }

        return true;
    }

    private void sendReport(Player player, String[] details){
        Location location = player.getLocation();

        if (details.length < 2) {
            player.sendMessage(TAG + ChatColor.RED + "報告文を記入してください。");
            return;
        }

        String report = ":";
        String info = "[" + NekoUtils.getTime() + " World:" + player.getWorld().getName()
                + " Loc X:" + (int)location.getX() + " Y:" + (int)location.getY() + " Z:" + (int)location.getZ() + "]";

        for (int i = 1; i < details.length; ++i) {
            report = report.concat(details[i]).concat(" ");
        }

        final String msg = info + player.getName() + report;

        mTwitter.sendDirectMessage("ken_kentan", msg);
        mTwitter.sendDirectMessage("tiru_2525" , msg);
        mTwitter.sendDirectMessage("xxviachaxx", msg);

        player.sendMessage(TAG + "レポートが正常に送信されました。");
    }


    private boolean checkPlayer(CommandSender sender){
        if(!NekoUtils.isPlayer(sender)){
            Log.warn("ゲーム内専用コマンドです.");
            return false;
        }

        return true;
    }

    private boolean isAdminCommand(CommandSender sender, String[] args){
        return args.length >= 2 && (args[0].equals("admin") && (sender.getName().equals("ken_kentan") || !NekoUtils.isPlayer(sender)));
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage("---------- NekoCoreコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko nyan" + ChatColor.WHITE + " にゃーん。");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko wiki" + ChatColor.WHITE + " wikiのURLを表示します。");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko map" + ChatColor.WHITE + " DynmapのURLを表示します。");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko blog" + ChatColor.WHITE + " BlogのURLを表示します。");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko hp" + ChatColor.WHITE + " ホームページのURLを表示します。");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko <twitter|tw>" + ChatColor.WHITE + " ServerのTwitterURLを表示します。");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko report <報告文>" + ChatColor.WHITE + " 運営に<報告文>を送信します。");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko vote" + ChatColor.WHITE + " ワールドの天気投票を行います。");
//        sender.sendMessage("| " + ChatColor.GOLD + "/neko account <Twitter ID>" + ChatColor.WHITE + " MinecraftIDとTwitterIDをリンクします。");
        sender.sendMessage("| " + ChatColor.GRAY + "/neko は /nk と省略することも可能です。");
        sender.sendMessage("---------------------------------------");
    }

    private void printUrl(CommandSender sender, String url){
        sender.sendMessage(TAG + ChatColor.AQUA + ChatColor.UNDERLINE + url);
        sender.sendMessage(TAG + ChatColor.GRAY + "↑のアドレスをクリックして下さい。");
    }
}

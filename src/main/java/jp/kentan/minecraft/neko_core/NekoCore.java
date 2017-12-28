package jp.kentan.minecraft.neko_core;


import jp.kentan.minecraft.neko_core.bridge.LuckPermsProvider;
import jp.kentan.minecraft.neko_core.bridge.WorldGuardProvider;
import jp.kentan.minecraft.neko_core.config.ConfigManager;
import jp.kentan.minecraft.neko_core.bridge.VaultProvider;
import jp.kentan.minecraft.neko_core.hat.HatCommandExecutor;
import jp.kentan.minecraft.neko_core.listener.PlayerEventListener;
import jp.kentan.minecraft.neko_core.rank.RankManager;
import jp.kentan.minecraft.neko_core.spawn.SpawnManager;
import jp.kentan.minecraft.neko_core.tutorial.TutorialManager;
import jp.kentan.minecraft.neko_core.vote.reward.RewardManager;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.NekoUtil;
import jp.kentan.minecraft.neko_core.vote.reward.ServerVoteListener;
import jp.kentan.minecraft.neko_core.vote.WeatherVote;
import jp.kentan.minecraft.neko_core.zone.ZoneManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;


public class NekoCore extends JavaPlugin implements Listener{

    public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "Neko" + ChatColor.RED + "Core" + ChatColor.GRAY + "] " + ChatColor.RESET;


    @Override
    public void onEnable() {
        Log.setup(getLogger());

        ConfigManager.setup(getDataFolder());

        VaultProvider.setup();
        LuckPermsProvider.setup();
        WorldGuardProvider.setup();

//        TwitterManager.setup();
//        TwitterBot.setup(this);
        RewardManager.setup();
        RankManager.setup(this);
        TutorialManager.setup(this);
        SpawnManager.setup(this);
        WeatherVote.setup(this);

        new ZoneManager(this);

        ConfigManager.load();

        getCommand("hat").setExecutor(new HatCommandExecutor());
        getServer().getPluginManager().registerEvents(new ServerVoteListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);

        Log.info("Enabled");
    }

    @Override
    public void onDisable() {
//        TwitterManager.shutdown();

        getServer().getScheduler().cancelTasks(this);

        Log.info("Disabled");
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
                if(checkPlayer(sender)){
                    playNyan(NekoUtil.toPlayer(sender));
                }
                break;
            case "hp":
                printUrl(sender, "https://minecraft.kentan.jp");
                break;
            case "map":
                printUrl(sender, "http://minecraft.kentan.jp:8123/");
                break;
            case "discord":
                printUrl(sender, "https://discord.gg/j3a6trZ");
                break;
            case "twitter":
            case "tw":
                printUrl(sender, "https://twitter.com/DekitateServer");
                break;
            case "vote":
                if(!checkPlayer(sender)) return true;

                WeatherVote.vote(NekoUtil.toPlayer(sender));
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
                case "forcevote":
                    if(params >= 3){
                        RewardManager.vote(args[2]);
                    }
                    break;
                case "reload":
                    ConfigManager.load();
                    sender.sendMessage(PREFIX + "設定ファルをリロードしました.");
                    break;
                default:
                    break;
            }
        }

        return true;
    }


    private boolean checkPlayer(CommandSender sender){
        if(!NekoUtil.isPlayer(sender)){
            Log.warn("ゲーム内専用コマンドです.");
            return false;
        }

        return true;
    }

    private boolean isAdminCommand(CommandSender sender, String[] args){
        return args.length >= 2 && (args[0].equals("admin") && (sender.getName().equals("ken_kentan") || !NekoUtil.isPlayer(sender)));
    }

    private void playNyan(Player player){
        player.playSound(player.getLocation(), Sound.ENTITY_CAT_AMBIENT, 1, 1);
    }

    private void printHelp(CommandSender sender) {
        sender.sendMessage("---------- NekoCoreコマンドヘルプ ----------");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko nyan" + ChatColor.RESET + " にゃーん.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko hp" + ChatColor.RESET + " ホームページのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko map" + ChatColor.RESET + " WebMapのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko discord" + ChatColor.RESET + " DiscordのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko <twitter|tw>" + ChatColor.RESET + " TwitterのURLを表示します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko report <報告文>" + ChatColor.RESET + " 運営に<報告文>を送信します.");
        sender.sendMessage("| " + ChatColor.GOLD + "/neko vote" + ChatColor.RESET + " ワールドの天気投票を行います.");
        sender.sendMessage("| " + ChatColor.BLUE + "/zone" + ChatColor.RESET + " 区画管理のヘルプを表示.");
        sender.sendMessage("| " + ChatColor.GRAY + "/neko は /nk と省略することも可能です.");
        sender.sendMessage("---------------------------------------");
    }

    private void printUrl(CommandSender sender, String url){
        sender.sendMessage(PREFIX + ChatColor.AQUA + ChatColor.UNDERLINE + url);
        sender.sendMessage(PREFIX + ChatColor.GRAY + "↑のアドレスをクリックして下さい.");
    }
}

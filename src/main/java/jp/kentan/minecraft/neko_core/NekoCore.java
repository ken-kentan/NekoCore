package jp.kentan.minecraft.neko_core;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import jp.kentan.minecraft.neko_core.bridge.ChatProvider;
import jp.kentan.minecraft.neko_core.bridge.EconomyProvider;
import jp.kentan.minecraft.neko_core.bridge.PermissionProvider;
import jp.kentan.minecraft.neko_core.bridge.WorldGuardProvider;
import jp.kentan.minecraft.neko_core.command.*;
import jp.kentan.minecraft.neko_core.config.ConfigManager;
import jp.kentan.minecraft.neko_core.listener.BukkitEventListener;
import jp.kentan.minecraft.neko_core.listener.ServerVoteListener;
import jp.kentan.minecraft.neko_core.manager.*;
import jp.kentan.minecraft.neko_core.util.Log;
import me.lucko.luckperms.LuckPerms;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class NekoCore extends JavaPlugin {

    public static final String PREFIX = ChatColor.GRAY + "[" + ChatColor.GOLD + "Neko" + ChatColor.RED + "Core" + ChatColor.GRAY + "] " + ChatColor.RESET;

    private EconomyProvider mEconomyProvider;
    private ChatProvider mChatProvider;
    private PermissionProvider mPermissionProvider;
    private WorldGuardProvider mWorldGuardProvider;

    @Override
    public void onEnable() {
        Log.setLogger(getLogger());

        hookVault();
        hookLuckPerms();
        hookWorldGuard();

        ConfigManager configManager = new ConfigManager(this);

        /*
        Manager
         */
        SpawnManager spawnManager = new SpawnManager(this, configManager.getSpawnConfigProvider());
        ServerVoteManager serverVoteManager = new ServerVoteManager();
        TutorialManager tutorialManager = new TutorialManager(this, mPermissionProvider, spawnManager);
        RankManager rankManager = new RankManager(this, mPermissionProvider, mChatProvider);
        WeatherVoteManager weatherVoteManager = new WeatherVoteManager(this, mEconomyProvider);
        ZoneManager zoneManager = new ZoneManager(this, mEconomyProvider, mWorldGuardProvider);
        AdvertisementManager advertisementManager = new AdvertisementManager(this, mEconomyProvider, configManager.getPlayerConfigProvider());

        /*
        Command登録
         */
        NekoCommandExecutor nekoCommandExecutor = new NekoCommandExecutor(configManager);
        getCommand("neko").setExecutor(nekoCommandExecutor);
        getCommand("neko").setTabCompleter(nekoCommandExecutor);

        SpawnCommandExecutor spawnCommandExecutor = new SpawnCommandExecutor(spawnManager);
        getCommand("spawn").setExecutor(spawnCommandExecutor);
        getCommand("spawn").setTabCompleter(spawnCommandExecutor);

        getCommand("setspawn").setExecutor(new SetSpawnCommandExecutor(spawnManager));

        getCommand("hat").setExecutor(new HatCommandExecutor(mEconomyProvider));

        getCommand("tutorial").setExecutor(new TutorialCommandExecutor(tutorialManager));

        getCommand("vote").setExecutor(new VoteCommandExecutor(serverVoteManager));

        WeatherVoteCommandExecutor weatherVoteCommandExecutor = new WeatherVoteCommandExecutor(weatherVoteManager);
        getCommand("weathervote").setExecutor(weatherVoteCommandExecutor);
        getCommand("weathervote").setTabCompleter(weatherVoteCommandExecutor);

        ZoneCommandExecutor zoneCommandExecutor = new ZoneCommandExecutor(zoneManager);
        getCommand("zone").setExecutor(zoneCommandExecutor);
        getCommand("zone").setTabCompleter(zoneCommandExecutor);

        AdvertisementCommandExecutor advertisementCommandExecutor = new AdvertisementCommandExecutor(advertisementManager);
        getCommand("advertisement").setExecutor(advertisementCommandExecutor);
        getCommand("advertisement").setTabCompleter(advertisementCommandExecutor);


        /*
        Listener登録
         */
        configManager.bindServerVoteEvent(serverVoteManager);
        configManager.bindTutorialKeywordEvent(tutorialManager);
        configManager.bindSpawnConfigEvent(spawnManager);

        getServer().getPluginManager().registerEvents(new ServerVoteListener(serverVoteManager), this);
        getServer().getPluginManager().registerEvents(new BukkitEventListener(
                this,
                configManager.getPlayerConfigProvider(),
                tutorialManager,
                rankManager,
                spawnManager,
                zoneManager,
                advertisementManager
        ), this);


        configManager.load();
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    private void hookVault() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            Log.error("failed to hook with Vault.");
            return;
        }

        RegisteredServiceProvider<Economy> economy = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        RegisteredServiceProvider<Chat> chat = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);

        if (economy == null) {
            Log.error("failed to get Economy service.");
            return;
        }

        if (chat == null) {
            Log.error("failed to get Chat service.");
            return;
        }

        mEconomyProvider = new EconomyProvider(economy.getProvider());
        mChatProvider = new ChatProvider(chat.getProvider());

        Log.info("hooked with Vault.");
    }

    private void hookLuckPerms() {
        try {
            mPermissionProvider = new PermissionProvider(LuckPerms.getApi());
        } catch (Exception e) {
            Log.error("failed to hook with LuckPerms.");
            return;
        }

        Log.info("hooked with LuckPerms.");
    }

    private void hookWorldGuard() {
        Plugin plugin = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");


        if (plugin != null && plugin instanceof WorldGuardPlugin) {
            mWorldGuardProvider = new WorldGuardProvider((WorldGuardPlugin) plugin);

            Log.info("hooked with WorldGuard.");
            return;
        }

        Log.error("failed to hook with WorldGuard.");
    }
}

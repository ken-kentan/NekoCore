package jp.kentan.minecraft.neko_core.manager;

import jp.kentan.minecraft.neko_core.bridge.EconomyProvider;
import jp.kentan.minecraft.neko_core.component.AdvertiseFrequency;
import jp.kentan.minecraft.neko_core.component.Advertisement;
import jp.kentan.minecraft.neko_core.config.PlayerConfigProvider;
import jp.kentan.minecraft.neko_core.dao.AdvertisementDao;
import jp.kentan.minecraft.neko_core.event.AdvertisementEvent;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.ZonedDateTime;
import java.util.*;

public class AdvertisementManager implements AdvertisementEvent {

    public final static String PREFIX = ChatColor.translateAlternateColorCodes('&', "&7[&d広告&7]&r ");

    private final BukkitScheduler SCHEDULER = Bukkit.getScheduler();
    private final List<Advertisement> AD_LIST = Collections.synchronizedList(new ArrayList<>());
    private final Map<Player, AdvertiseFrequency> PLAYER_MAP = Collections.synchronizedMap(new HashMap<>());
    private final Map<Player, ConfirmTask> CONFIRM_TASK_MAP = new HashMap<>();
    private final int PRICE_PER_DAY = 100;

    private final Plugin PLUGIN;
    private final AdvertisementDao DAO;
    private final EconomyProvider ECONOMY;
    private final PlayerConfigProvider PLAYER_CONFIG;

    private int mIndexAdListHigh   = 0;
    private int mIndexAdListMiddle = 0;
    private int mIndexAdListLow    = 0;

    public AdvertisementManager(Plugin plugin, EconomyProvider economyProvider, PlayerConfigProvider playerConfigProvider) {
        PLUGIN = plugin;
        ECONOMY = economyProvider;
        PLAYER_CONFIG = playerConfigProvider;

        DAO = new AdvertisementDao();

        syncLatestAds();

        Bukkit.getOnlinePlayers().forEach(p -> PLAYER_MAP.put(p, PLAYER_CONFIG.getAdvertiseFrequency(p.getUniqueId())));

        startBroadcastAsyncTask();
    }

    private void syncLatestAds() {
        SCHEDULER.runTaskAsynchronously(PLUGIN, () -> {
            AD_LIST.clear();
            AD_LIST.addAll(DAO.getAdvertisementList());
        });
    }

    private void startBroadcastAsyncTask() {
        final long INTERVAL_TICK = 20L * 60 * 5; //5m

        SCHEDULER.runTaskTimerAsynchronously(PLUGIN, () -> {
            AD_LIST.removeIf(ad -> ad.EXPIRE_DATE.isBefore(ZonedDateTime.now()));
            if (AD_LIST.isEmpty() || PLAYER_MAP.isEmpty()) {
                return;
            }

            if (AD_LIST.size() <= ++mIndexAdListHigh) {
                mIndexAdListHigh = 0;
            }
            Advertisement ad = AD_LIST.get(mIndexAdListHigh);

            PLAYER_MAP.entrySet().stream().filter(e -> e.getValue() == AdvertiseFrequency.HIGH).forEach(e -> e.getKey().sendMessage(PREFIX + ad.CONTENT));
        }, INTERVAL_TICK, INTERVAL_TICK);

        SCHEDULER.runTaskTimerAsynchronously(PLUGIN, () -> {
            if (AD_LIST.isEmpty() || PLAYER_MAP.isEmpty()) {
                return;
            }

            if (AD_LIST.size() <= ++mIndexAdListMiddle) {
                mIndexAdListMiddle = 0;
            }
            Advertisement ad = AD_LIST.get(mIndexAdListMiddle);

            PLAYER_MAP.entrySet().stream().filter(e -> e.getValue() == AdvertiseFrequency.MIDDLE).forEach(e -> e.getKey().sendMessage(PREFIX + ad.CONTENT));
        }, INTERVAL_TICK, INTERVAL_TICK * AdvertiseFrequency.MIDDLE.getIntervalGain());

        SCHEDULER.runTaskTimerAsynchronously(PLUGIN, () -> {
            if (AD_LIST.isEmpty() || PLAYER_MAP.isEmpty()) {
                return;
            }

            if (AD_LIST.size() <= ++mIndexAdListLow) {
                mIndexAdListLow = 0;
            }
            Advertisement ad = AD_LIST.get(mIndexAdListLow);

            PLAYER_MAP.entrySet().stream().filter(e -> e.getValue() == AdvertiseFrequency.LOW).forEach(e -> e.getKey().sendMessage(PREFIX + ad.CONTENT));
        }, INTERVAL_TICK, INTERVAL_TICK * AdvertiseFrequency.LOW.getIntervalGain());
    }

    public void addSetAdConfirmTask(Player player, String strPeriodDays, String content) {
        int periodDays;

        try {
            periodDays = Integer.parseInt(strPeriodDays);
        } catch (Exception e) {
            sendWarnMessage(player, strPeriodDays + "は整数ではありません.");
            return;
        }

        if (periodDays <= 0 || periodDays > 30) {
            sendWarnMessage(player, "日数は 1～30 の範囲で入力して下さい");
            return;
        }

        if (content.length() < 10) {
            sendWarnMessage(player, "広告の内容が短すぎます.");
            return;
        }

        if (content.length() > 250) {
            sendWarnMessage(player, "250文字を超える広告は登録できません.");
            return;
        }

        if (DAO.hasAdvertisement(player.getUniqueId())) {
            sendWarnMessage(player, "すでに広告が登録されています.");
            player.sendMessage(HINT_SET_AD_OVERLAP);
            return;
        }

        sendPreview(player, content);

        CONFIRM_TASK_MAP.remove(player);

        String setMsg = AD_SET_MESSAGE
                .replace("{days}", Integer.toString(periodDays))
                .replace("{price}", Integer.toString(PRICE_PER_DAY * periodDays));

        player.sendMessage(setMsg);
        player.sendMessage(CONFIRM_MESSAGE.replace("{action}", "登録"));

        CONFIRM_TASK_MAP.put(player, new ConfirmTask(ConfirmTask.Type.SET, content, periodDays));
        SCHEDULER.runTaskLaterAsynchronously(PLUGIN, () -> CONFIRM_TASK_MAP.remove(player), 20L * 30);
    }

    public void addUnsetAdConfirmTask(Player player) {
        Advertisement ad = DAO.getAdvertisement(player.getUniqueId());

        if (ad == null) {
            sendWarnMessage(player, "広告が登録されていません.");
            return;
        }

        sendPreview(player, ad.CONTENT);

        CONFIRM_TASK_MAP.remove(player);

        player.sendMessage(AD_UNSET_MESSAGE);
        player.sendMessage(CONFIRM_MESSAGE.replace("{action}", "消去"));

        CONFIRM_TASK_MAP.put(player, new ConfirmTask(ConfirmTask.Type.UNSET, null, 0));
        SCHEDULER.runTaskLaterAsynchronously(PLUGIN, () -> CONFIRM_TASK_MAP.remove(player), 20L * 30);
    }

    public void sendPreview(CommandSender sender, String content) {
        sender.sendMessage(PREFIX + ChatColor.translateAlternateColorCodes('&', content));
    }

    public void sendPlayerAdInfo(Player player) {
        Advertisement ad = DAO.getAdvertisement(player.getUniqueId());

        player.sendMessage(AD_INFO_INDEX);
        player.sendMessage(" 受信頻度: " + PLAYER_MAP.get(player).getName());
        player.sendMessage(" 登録広告: " + ((ad != null) ? ad.CONTENT : ChatColor.DARK_GRAY + "なし"));

        if (ad != null) {
            player.sendMessage(" 配信期限: " + ChatColor.RED + Util.formatDate(ad.EXPIRE_DATE) + ChatColor.GRAY + "まで");
        }
    }

    public void setAdvertiseFrequency(Player player, String strFreq) {
        AdvertiseFrequency freq;
        try {
            freq = AdvertiseFrequency.valueOf(strFreq.toUpperCase());
        } catch (Exception e) {
            player.sendMessage(PREFIX + ChatColor.YELLOW + strFreq + "は存在しません.");
            return;
        }

        if (PLAYER_CONFIG.saveAdvertiseFrequency(player.getUniqueId(), freq)) {
            PLAYER_MAP.put(player, freq);
            player.sendMessage(PREFIX + ChatColor.GREEN + "受信頻度を" + freq.getName() + ChatColor.GREEN + "に設定しました.");
        } else {
            player.sendMessage(PREFIX + ChatColor.YELLOW + "設定に失敗しました.");
        }
    }

    public void sendAdList(CommandSender sender) {
        sender.sendMessage(AD_LIST_INDEX);

        AD_LIST.forEach(ad -> sender.sendMessage(AD_LIST.indexOf(ad)+1 + ". " + ad.CONTENT + ChatColor.RESET + ChatColor.GRAY + " (" + ad.OWNER.getName() + ')'));
    }

    public void confirmTask(Player player) {
        ConfirmTask task = CONFIRM_TASK_MAP.get(player);

        if (task == null) {
            sendWarnMessage(player, "認証が必要な処理はありません");
            return;
        }

        CONFIRM_TASK_MAP.remove(player);

        switch (task.TYPE) {
            case SET:
                setPlayerAd(player, task.CONTENT, task.PERIOD_DAYS);
                break;
            case UNSET:
                unsetPlayerAd(player);
                break;
            default:
                break;
        }
    }

    public void sync() {
        syncLatestAds();
    }

    @Override
    public void onPlayerJoin(Player player) {
        PLAYER_MAP.put(player, PLAYER_CONFIG.getAdvertiseFrequency(player.getUniqueId()));
    }

    @Override
    public void onPlayerQuit(Player player) {
        PLAYER_MAP.remove(player);
    }

    private void setPlayerAd(Player player, String content, int periodDays) {
        if (content == null || periodDays < 1) {
            sendErrorMessage(player, "不正な操作です.");
            return;
        }

        double price = periodDays * PRICE_PER_DAY;
        double balance = ECONOMY.getBalance(player);
        if (balance < price) {
            sendWarnMessage(player, "所持金が \u00A5" + (price - balance) + " 不足しています.");
            return;
        }

        if (price < 0D || !ECONOMY.withdraw(player, price)) {
            sendErrorMessage(player, "購入処理に失敗しました.");
            return;
        }

        if (!DAO.addAdvertisement(player.getUniqueId(), content, ZonedDateTime.now().plusDays(periodDays))) {
            sendErrorMessage(player, "データベースの更新に失敗しました.");

            if (!ECONOMY.deposit(player, price)) {
                sendErrorMessage(player, "復元処理に失敗しました.");
            }
            return;
        }

        player.sendMessage(PREFIX + ChatColor.GREEN + "登録しました.");

        syncLatestAds();
    }

    private void unsetPlayerAd(Player player) {
        if (!DAO.hasAdvertisement(player.getUniqueId())) {
            sendWarnMessage(player, "広告が登録されていません.");
            return;
        }

        if (!DAO.deleteAdvertisement(player.getUniqueId())) {
            sendErrorMessage(player, "データベースの更新に失敗しました.");
            return;
        }

        player.sendMessage(PREFIX + ChatColor.GREEN + "消去しました.");

        syncLatestAds();
    }

    private void sendWarnMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.YELLOW + message);
    }

    private void sendErrorMessage(CommandSender sender, String message) {
        sender.sendMessage(PREFIX + ChatColor.RED + message);
    }

    private final String AD_LIST_INDEX = ChatColor.translateAlternateColorCodes('&', "&7***************&d 広告一覧&7 ***************");
    private final String AD_INFO_INDEX = ChatColor.translateAlternateColorCodes('&', "&7***************&d 広告情報&7 ***************");

    private final static String AD_SET_MESSAGE = ChatColor.translateAlternateColorCodes('&', PREFIX + "この広告を &a{days}日間 &e\u00A5{price} &rで登録しますか？");
    private final static String AD_UNSET_MESSAGE = ChatColor.translateAlternateColorCodes('&', PREFIX + "この広告を消去しますか？");
    private final static String CONFIRM_MESSAGE = ChatColor.translateAlternateColorCodes('&', PREFIX + "&7{action}を確定するには &c/ad confirm&7 と入力して下さい.");
    private final static String HINT_SET_AD_OVERLAP = ChatColor.translateAlternateColorCodes('&', PREFIX + "/ad unset で以前の広告を消去して下さい.");


    private static class ConfirmTask {
        enum Type {SET, UNSET}

        private final Type TYPE;
        private final String CONTENT;
        private final int PERIOD_DAYS;

        ConfirmTask(Type type, String content, int periodDays) {
            TYPE = type;
            CONTENT = content;
            PERIOD_DAYS = periodDays;
        }
    }
}

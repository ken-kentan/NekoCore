package jp.kentan.minecraft.neko_core.manager;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import jp.kentan.minecraft.neko_core.bridge.EconomyProvider;
import jp.kentan.minecraft.neko_core.bridge.ShopProvider;
import jp.kentan.minecraft.neko_core.bridge.WorldGuardProvider;
import jp.kentan.minecraft.neko_core.component.zone.Area;
import jp.kentan.minecraft.neko_core.component.zone.AreaState;
import jp.kentan.minecraft.neko_core.component.zone.Zone;
import jp.kentan.minecraft.neko_core.component.zone.ZoneType;
import jp.kentan.minecraft.neko_core.dao.ZoneDao;
import jp.kentan.minecraft.neko_core.event.ZoneEvent;
import jp.kentan.minecraft.neko_core.util.Log;
import jp.kentan.minecraft.neko_core.util.Util;
import org.bukkit.*;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ZoneManager implements ZoneEvent {

    public final static String PREFIX = ChatColor.GRAY + "[" + ChatColor.BLUE + "区画" + ChatColor.GRAY + "] " + ChatColor.RESET;

    private final static String SIGN_INDEX_TEXT = ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "[" + ChatColor.BLUE + ChatColor.BOLD + "区画" + ChatColor.DARK_GRAY + ChatColor.BOLD + "]";
    private final static BukkitScheduler SCHEDULER = Bukkit.getScheduler();

    private final Map<Player, AreaTradeTask> AREA_TRADE_TASK_MAP = Collections.synchronizedMap(new HashMap<>());

    private final Plugin PLUGIN;
    private final ZoneDao DAO;
    private final EconomyProvider ECONOMY;
    private final WorldGuardProvider WORLD_GUARD;
    private final ShopProvider SHOP;

    public ZoneManager(Plugin plugin, EconomyProvider economyProvider, WorldGuardProvider worldGuardProvider) {
        PLUGIN = plugin;
        ECONOMY = economyProvider;
        WORLD_GUARD = worldGuardProvider;

        DAO = new ZoneDao();

        SHOP = new ShopProvider();

        checkAllArea();

        scheduleRentalDateCheckTask();
    }

    public void reload() {
        checkAllArea();
    }

    public void registerArea(Player player, String areaName, String zoneId, String regionId, String strRegionSize) {
        String world = player.getWorld().getName();
        Area area = DAO.getArea(world, areaName);

        if (area != null) {
            sendWarnMessage(player, areaName + "はすでに登録されています.");
            return;
        }

        int regionSize;
        try {
            regionSize = Integer.parseInt(strRegionSize);
        } catch (Exception e) {
            sendWarnMessage(player, strRegionSize + " は正しい整数ではありません.");
            return;
        }

        if (regionSize < 1) {
            sendWarnMessage(player, "区画サイズは1以上である必要があります.");
            return;
        }

        if (DAO.addArea(areaName, world, zoneId, regionId, regionSize)) {
            player.sendMessage(PREFIX + ChatColor.GREEN + areaName + "を登録しました.");
        } else {
            player.sendMessage(PREFIX + ChatColor.RED + "データベスの更新に失敗しました.");
        }
    }

    public void registerAreas(Player player, String areaNamePattern, String zoneId, String regionIdPattern, String strRegionSize, String strBegin, String strEnd) {
        String world = player.getWorld().getName();

        List<String> areaNameList = DAO.getAreaNameList(player.getWorld().getName());

        int begin, end;
        int regionSize;
        try {
            begin = Integer.parseInt(strBegin);
            end   = Integer.parseInt(strEnd);

            regionSize = Integer.parseInt(strRegionSize);
        } catch (Exception e) {
            sendWarnMessage(player, "文字列から整数への変換に失敗しました.");
            return;
        }

        if (begin == end) {
            sendWarnMessage(player, "開始番号と終了番号が一致しています.");
            return;
        } else if (begin > end) {
            sendWarnMessage(player, "終了番号が開始番号より小さいです.");
            return;
        }

        if (regionSize < 1) {
            sendWarnMessage(player, "区画サイズは1以上である必要があります.");
            return;
        }

        for (int i = begin; i <= end; i++) {
            String areaName = areaNamePattern.replace("*", Integer.toString(i));
            String regionId = regionIdPattern.replace("*", Integer.toString(i));

            if (areaNameList.contains(areaName)) {
                sendWarnMessage(player, areaName + "はすでに登録されています.");
                continue;
            }

            if (DAO.addArea(areaName, world, zoneId, regionId, regionSize)) {
                areaNameList.add(areaName);
                player.sendMessage(PREFIX + ChatColor.GREEN + areaName + "を登録しました.");
            } else {
                player.sendMessage(PREFIX + ChatColor.RED + areaName + "のデータベス更新に失敗しました.");
                return;
            }
        }
    }

    public void removeArea(Player player, String areaName) {
        String world = player.getWorld().getName();
        Area area = DAO.getArea(world, areaName);

        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        if (DAO.deleteArea(world, areaName)) {
            player.sendMessage(PREFIX + ChatColor.GREEN + areaName + "を消去しました.");
        } else {
            player.sendMessage(PREFIX + ChatColor.RED + "データベスの更新に失敗しました.");
        }
    }

    public void setAreaLock(Player player, String areaName, boolean isLock) {
        String world = player.getWorld().getName();

        Area area = DAO.getArea(world, areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        AreaState updateState;
        if (isLock) {
            updateState = AreaState.LOCK;
        } else {
            updateState = (area.OWNER != null) ? AreaState.SOLD : AreaState.ON_SALE;
        }


        if (DAO.updateAreaState(world, areaName, updateState)) {
            player.sendMessage(PREFIX + ChatColor.GREEN + areaName + "の区画状態を " + updateState.getName() + ChatColor.GREEN + " に更新しました.");
        } else {
            player.sendMessage(PREFIX + ChatColor.RED + "データベスの更新に失敗しました.");
        }

        updateAreaSign(world, areaName);
    }

    public void takeAreaFromOwner(Player player, String areaName) {
        String world = player.getWorld().getName();

        Area area = DAO.getArea(world, areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        if (area.OWNER == null) {
            sendWarnMessage(player, areaName + "に所有者がいません.");
            return;
        }

        if (DAO.takeAreaFromOwner(world, areaName)) {
            player.sendMessage(PREFIX + ChatColor.GREEN + areaName + "の区画を" + area.OWNER.getName() + "から取り上げました.");
        } else {
            player.sendMessage(PREFIX + ChatColor.RED + "データベスの更新に失敗しました.");
        }

        updateAreaSign(world, areaName);
    }

    public void registerBuyTask(Player player, String areaName) {
        String world = player.getWorld().getName();

        Area area = DAO.getArea(world, areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        Zone zone = DAO.getZone(area.ZONE_ID);
        if (zone.TYPE != ZoneType.BUY_UP) {
            sendWarnMessage(player, "買い切り区画ではありません.");
            return;
        }

        if (!area.onSale()) {
            sendWarnMessage(player, "現在,この区画は購入できません.");
            return;
        }

        if (DAO.isReachOwnedLimit(player.getUniqueId(), area.ZONE_ID)) {
            sendWarnMessage(player, zone.NAME + "の所有数が上限に達しています.");
            return;
        }

        double price = DAO.getBuyPrice(player.getUniqueId(), zone.ID, areaName);
        if (price < 0D) {
            sendErrorMessage(player, areaName + "の購入価格を取得できませんでした.");
            return;
        }


        AREA_TRADE_TASK_MAP.remove(player);

        if (zone.BUY_RENTAL_RULE != null) {
            player.sendMessage(zone.BUY_RENTAL_RULE);
        }

        String buyMsg = BUY_MESSAGE
                .replace("{zone}", zone.NAME)
                .replace("{area}", area.NAME);

        player.sendMessage(buyMsg);
        player.sendMessage(CONFIRM_MESSAGE.replace("{action}", "購入"));

        // タスク作成
        AREA_TRADE_TASK_MAP.put(player, new AreaTradeTask(AreaTradeTask.Type.BUY, areaName, price));

        SCHEDULER.runTaskLaterAsynchronously(PLUGIN, () -> AREA_TRADE_TASK_MAP.remove(player), 20L * 60);
    }

    public void registerRentalTask(Player player, String areaName) {
        String world = player.getWorld().getName();

        Area area = DAO.getArea(world, areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        Zone zone = DAO.getZone(area.ZONE_ID);
        if (zone.TYPE != ZoneType.RENTAL) {
            sendWarnMessage(player, "レンタル区画ではありません.");
            return;
        }

        // 新規レンタル
        if (!area.isOwner(player.getUniqueId())) {
            if (!area.onSale()) {
                sendWarnMessage(player, "現在,この区画はレンタルできません.");
                return;
            }

            if (DAO.isReachOwnedLimit(player.getUniqueId(), area.ZONE_ID)) {
                sendWarnMessage(player, zone.NAME + "の所有数が上限に達しています.");
                return;
            }
        }

        double price = DAO.getRentalPrice(player.getUniqueId(), zone.ID, areaName);
        if (price < 0D) {
            sendErrorMessage(player, areaName + "のレンタル価格を取得できませんでした.");
            return;
        }


        AREA_TRADE_TASK_MAP.remove(player);

        if (zone.BUY_RENTAL_RULE != null) {
            player.sendMessage(zone.BUY_RENTAL_RULE);
        }

        String rentalMsg = RENTAL_MESSAGE
                .replace("{zone}", zone.NAME)
                .replace("{area}", area.NAME)
                .replace("{price}", Double.toString(price))
                .replace("{days}", Integer.toString(zone.RENTAL_DAYS));

        player.sendMessage(rentalMsg);
        player.sendMessage(CONFIRM_MESSAGE.replace("{action}", "レンタル"));

        // タスク作成
        AREA_TRADE_TASK_MAP.put(player, new AreaTradeTask(AreaTradeTask.Type.RENTAL, areaName, price));

        SCHEDULER.runTaskLaterAsynchronously(PLUGIN, () -> AREA_TRADE_TASK_MAP.remove(player), 20L * 60);
    }

    public void registerSellTask(Player player, String areaName) {
        String world = player.getWorld().getName();
        Area area = DAO.getArea(world, areaName);

        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        if (!area.isOwner(player.getUniqueId())) {
            sendWarnMessage(player, "この区画の所有者ではありません.");
            return;
        }

        if (area.isLock()) {
            sendWarnMessage(player, "この区画はロックされています.");
            return;
        }

        double price = DAO.getSellPrice(world, areaName);
        if (price < 0D) {
            sendErrorMessage(player, areaName + "の売却価格を取得できませんでした.");
            return;
        }


        AREA_TRADE_TASK_MAP.remove(player);

        Zone zone = DAO.getZone(area.ZONE_ID);

        if (zone.SELL_RULE != null) {
            player.sendMessage(zone.SELL_RULE);

        }

        String sellMsg = SELL_MESSAGE
                .replace("{zone}", zone.NAME)
                .replace("{area}", area.NAME)
                .replace("{price}", Double.toString(price));

        player.sendMessage(sellMsg);
        player.sendMessage(CONFIRM_MESSAGE.replace("{action}", "レンタル"));

        // タスク作成
        AREA_TRADE_TASK_MAP.put(player, new AreaTradeTask(AreaTradeTask.Type.SELL, areaName, price));

        SCHEDULER.runTaskLaterAsynchronously(PLUGIN, () -> AREA_TRADE_TASK_MAP.remove(player), 20L * 60);
    }

    public void sendAreaInfo(Player player, String areaName) {
        String world = player.getWorld().getName();

        Area area = DAO.getArea(world, areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        Zone zone = DAO.getZone(area.ZONE_ID);

        OfflinePlayer owner = area.OWNER;

        double buyPrice    = -1D;
        double rentalPrice = -1D;
        double sellPrice   = -1D;
        switch (zone.TYPE) {
            case BUY_UP:
                if (area.isOwner(player.getUniqueId())) {
                    sellPrice = DAO.getSellPrice(world, area.NAME);
                } else if (area.onSale()) {
                    buyPrice = DAO.getBuyPrice(player.getUniqueId(), zone.ID, area.NAME);
                }
                break;
            case RENTAL:
                if (area.isOwner(player.getUniqueId())) {
                    sellPrice = DAO.getSellPrice(world, area.NAME);
                }

                rentalPrice = DAO.getRentalPrice(player.getUniqueId(), zone.ID, area.NAME);
                break;
            default:
                return;
        }


        player.sendMessage(AREA_INFO_INDEX);
        player.sendMessage(" 区画: " + zone.NAME);
        player.sendMessage(" タイプ: " + zone.TYPE.getName());
        player.sendMessage(" エリア: " + area.NAME);
        player.sendMessage(" 保護ID: " + area.REGION_ID);

        if (buyPrice >= 0D) {
            player.sendMessage(" 販売価格: " + ChatColor.YELLOW + '\u00A5' + Double.toString(buyPrice));
        }
        if (rentalPrice >= 0D) {
            player.sendMessage(" レンタル価格: " + ChatColor.YELLOW + '\u00A5' + Double.toString(rentalPrice));
        }
        if (sellPrice >= 0D) {
            player.sendMessage(" 売却価格: " + ChatColor.YELLOW + '\u00A5' + Double.toString(sellPrice));
        }

        player.sendMessage(" 所有者: " + ((owner != null) ? ChatColor.DARK_GRAY + owner.getName() : ""));
        player.sendMessage(" 状態: " + area.STATE.getName());

        if (area.EXPIRE_DATE != null) {
            player.sendMessage(" レンタル期限: " + ChatColor.RED + Util.formatDate(area.EXPIRE_DATE));
        }

        if (area.isLock()) {
            return;
        }

        if (area.isOwner(player.getUniqueId())) {
            player.sendMessage(ChatColor.GRAY + "売却コマンド " + ChatColor.RESET + "/zone sell " + areaName);

            if (zone.TYPE == ZoneType.RENTAL) {
                player.sendMessage(ChatColor.GRAY + "レンタル延長コマンド " + ChatColor.RESET + "/zone rental " + areaName);
            }
        } else if (area.onSale()) {
            if (zone.TYPE == ZoneType.BUY_UP) {
                player.sendMessage(ChatColor.GRAY + "購入コマンド " + ChatColor.RESET + "/zone purchase " + areaName);
            } else {
                player.sendMessage(ChatColor.GRAY + "レンタルコマンド " + ChatColor.RESET + "/zone rental " + areaName);
            }
        }
    }

    public void sendOwnerLimits(Player player) {
        List<Zone> zoneList = new ArrayList<>(DAO.getZoneList());

        if (zoneList.isEmpty()) {
            sendWarnMessage(player, "区画が存在するワールドがありません.");
            return;
        }

        player.sendMessage(ChatColor.GRAY + "**********" + ChatColor.GOLD + " 区画上限 " + ChatColor.GRAY + "**********");

        zoneList.forEach(zone -> player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7- &r" + zone.NAME + "&r: &e" +
                DAO.getCountOwnedAreaPerZone(player.getUniqueId(), zone.ID) + "/" + zone.OWNER_LIMIT)));
    }

    public void sendOwnerAreaList(Player player) {
        Map<String, List<String>> areaMap = new HashMap<>(DAO.getOwnedAreaMap(player.getUniqueId()));

        if (areaMap.isEmpty()) {
            sendWarnMessage(player, "区画を所有していません.");
            return;
        }

        player.sendMessage(ChatColor.GRAY + "**********" + ChatColor.GOLD + " 所有区画一覧 " + ChatColor.GRAY + "**********");
        areaMap.forEach((zoneName, areaList) -> {
            StringBuilder areaListText = new StringBuilder(ChatColor.translateAlternateColorCodes('&', "&7- &r" + zoneName + "&r: &e"));

            areaList.forEach(area -> {
                areaListText.append(area);
                areaListText.append(", ");
            });

            player.sendMessage(areaListText.toString().substring(0, areaListText.length() - 2));
        });
    }

    public void sendZoneRule(Player player, String areaName) {
        Area area = DAO.getArea(player.getWorld().getName(), areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        Zone zone = DAO.getZone(area.ZONE_ID);
        if (zone == null) {
            sendUnknownZone(player);
            return;
        }

        if (zone.BUY_RENTAL_RULE != null) {
            player.sendMessage(zone.BUY_RENTAL_RULE);
        }
        if (zone.SELL_RULE != null) {
            player.sendMessage(zone.SELL_RULE);
        }
    }

    public void confirmTask(Player player) {
        AreaTradeTask task = AREA_TRADE_TASK_MAP.get(player);

        if (task == null) {
            sendWarnMessage(player, "認証が必要な処理はありません.");
            return;
        }


        AREA_TRADE_TASK_MAP.remove(player);

        switch (task.TYPE) {
            case BUY:
                buyArea(player, task.AREA_NAME, task.PRICE);
                break;
            case RENTAL:
                rentalArea(player, task.AREA_NAME, task.PRICE);
                break;
            case SELL:
                sellArea(player, task.AREA_NAME, task.PRICE);
                break;
            default:
                break;
        }
    }

    private void buyArea(Player player, String areaName, double price) {
        String world = player.getWorld().getName();

        // 最新の区画情報を取得
        Area area = DAO.getArea(world, areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        Zone zone = DAO.getZone(area.ZONE_ID);
        if (zone.TYPE != ZoneType.BUY_UP) {
            sendWarnMessage(player, "買い切り区画ではありません.");
            return;
        }

        if (!area.onSale()) {
            sendWarnMessage(player, "現在,この区画は購入できません.");
            return;
        }

        // 最新の区画価格を取得
        double buyPrice = DAO.getBuyPrice(player.getUniqueId(), zone.ID, areaName);
        if (Double.compare(price, buyPrice) == 1) {
            sendWarnMessage(player, "区画の価格情報が更新されました. 再試行して下さい.");
        }

        double balance = ECONOMY.getBalance(player);
        if (balance < buyPrice) {
            sendWarnMessage(player, "所持金が \u00A5" + (buyPrice - balance) + " 不足しています.");
            return;
        }

        ProtectedRegion region = WORLD_GUARD.getProtectedRegion(player.getWorld(), area.REGION_ID);
        if (region == null) {
            sendErrorMessage(player, "リジョンIDエラーです.");
            return;
        }


        if (buyPrice < 0D || !ECONOMY.withdraw(player, buyPrice)) {
            sendErrorMessage(player, "購入処理に失敗しました.");
            return;
        }

        if (!DAO.buyArea(player.getUniqueId(), world, areaName, buyPrice)) {
            sendErrorMessage(player, "データベースの更新に失敗しました.");

            if (!ECONOMY.deposit(player, buyPrice)) {
                sendErrorMessage(player, "復元処理に失敗しました.");
            }
            return;
        }


        setRegionMember(player.getUniqueId(), region);

        updateAreaSign(world, areaName);

        String completeMsg = TASK_COMPLETE_MESSAGE
                .replace("{zone}", zone.NAME)
                .replace("{area}", area.NAME)
                .replace("{price}", Double.toString(buyPrice))
                .replace("{action}", "購入");

        player.sendMessage(completeMsg);
    }

    private void rentalArea(Player player, String areaName, double price) {
        String world = player.getWorld().getName();

        // 最新の区画情報を取得
        Area area = DAO.getArea(world, areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        Zone zone = DAO.getZone(area.ZONE_ID);
        if (zone.TYPE != ZoneType.RENTAL) {
            sendWarnMessage(player, "レンタル区画ではありません.");
            return;
        }

        if (!area.onSale() && (!area.isOwner(player.getUniqueId()) && area.STATE != AreaState.LOCK)) {
            sendWarnMessage(player, "現在,この区画はレンタルできません.");
            return;
        }

        // 最新の区画価格を取得
        double rentalPrice = DAO.getRentalPrice(player.getUniqueId(), zone.ID, areaName);
        if (Double.compare(price, rentalPrice) == 1) {
            sendWarnMessage(player, "区画の価格情報が更新されました. 再試行して下さい.");
        }

        double balance = ECONOMY.getBalance(player);
        if (balance < rentalPrice) {
            sendWarnMessage(player, "所持金が \u00A5" + (rentalPrice - balance) + " 不足しています.");
            return;
        }

        ProtectedRegion region = WORLD_GUARD.getProtectedRegion(player.getWorld(), area.REGION_ID);
        if (region == null) {
            sendErrorMessage(player, "リジョンIDエラーです.");
            return;
        }

        // ExpireDate計算
        ZonedDateTime expireDate;
        if (area.EXPIRE_DATE == null) {
            expireDate = ZonedDateTime.now();
        } else {
            expireDate = area.EXPIRE_DATE;
        }
        expireDate = expireDate.plusDays(zone.RENTAL_DAYS);

        // 180日以上レンタルさせない
        if (ChronoUnit.DAYS.between(ZonedDateTime.now(), expireDate) > 180) {
            sendWarnMessage(player, "180日間を超えるレンタルは行なえません.");
            return;
        }


        if (rentalPrice < 0D || !ECONOMY.withdraw(player, rentalPrice)) {
            sendErrorMessage(player, "レンタル処理に失敗しました.");
            return;
        }

        if (!DAO.rentalArea(player.getUniqueId(), world, areaName, rentalPrice, expireDate)) {
            sendErrorMessage(player, "データベースの更新に失敗しました.");

            if (!ECONOMY.deposit(player, rentalPrice)) {
                sendErrorMessage(player, "復元処理に失敗しました.");
            }
            return;
        }


        setRegionMember(player.getUniqueId(), region);

        updateAreaSign(world, areaName);

        String completeMsg = TASK_COMPLETE_MESSAGE
                .replace("{zone}", zone.NAME)
                .replace("{area}", area.NAME)
                .replace("{price}", Double.toString(rentalPrice))
                .replace("{action}", "レンタル");
        String periodMsg = RENTAL_PERIOD_MESSAGE
                .replace("{date}", Util.formatDate(expireDate));

        player.sendMessage(completeMsg);
        player.sendMessage(periodMsg);
    }

    private void sellArea(Player player, String areaName, double price) {
        String world = player.getWorld().getName();

        Area area = DAO.getArea(world, areaName);

        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        if (!area.isOwner(player.getUniqueId())) {
            Log.warn("この区画の所有者ではありません.");
            return;
        }

        if (area.isLock()) {
            sendWarnMessage(player, "この区画はロックされています.");
            return;
        }

        // 最新の区画価格を取得
        double sellPrice = DAO.getSellPrice(world, areaName);
        if (Double.compare(price, sellPrice) == 1) {
            sendWarnMessage(player, "区画の価格情報が更新されました. 再試行して下さい.");
        }

        ProtectedRegion region = WORLD_GUARD.getProtectedRegion(player.getWorld(), area.REGION_ID);
        if (region == null) {
            sendErrorMessage(player, "リジョンIDエラーです.");
            return;
        }

        if (containBlock(player.getWorld(), region)) {
            sendWarnMessage(player, "区画内にブロックがあります.");
            return;
        }

        if (sellPrice < 0D || !ECONOMY.deposit(player, sellPrice)) {
            sendErrorMessage(player, "入金処理に失敗しました.");
            return;
        }

        if (!DAO.sellArea(player.getUniqueId(), world, areaName)) {
            sendErrorMessage(player, "データベースの更新に失敗しました.");

            if (!ECONOMY.withdraw(player, sellPrice)) {
                sendErrorMessage(player, "復元処理に失敗しました.");
            }
            return;
        }


        setRegionMember(null, region);

        updateAreaSign(world, areaName);

        String completeMsg = TASK_COMPLETE_MESSAGE
                .replace("{zone}", DAO.getZoneName(area.ZONE_ID))
                .replace("{area}", area.NAME)
                .replace("{price}", Double.toString(sellPrice))
                .replace("{action}", "売却");

        player.sendMessage(completeMsg);
    }

    private void scheduleRentalDateCheckTask() {
        SCHEDULER.runTaskTimerAsynchronously(PLUGIN, () -> {
            List<Area> expiredAreaList = new ArrayList<>(DAO.takeExpiredRentalAreas());

            if (!expiredAreaList.isEmpty()) {
                SCHEDULER.scheduleSyncDelayedTask(PLUGIN, () -> expiredAreaList.forEach(area -> {
                    World world = Bukkit.getWorld(area.WORLD);

                    ProtectedRegion region = WORLD_GUARD.getProtectedRegion(world, area.REGION_ID);

                    setRegionMember(null, region);
                    cleanRegion(world, region);
                    area.updateSign();
                }));
            }
        }, 20L, 20L * 60 * 5); // 5分おきに走る
    }

    private void checkAllArea() {
        List<Area> areaList = DAO.getAreaList();

        areaList.forEach(a -> {
            if (!a.updateSign()) {
                DAO.updateSignLocation(a.WORLD, a.NAME, null);
            }

            if (a.OWNER != null && a.OWNER.isBanned()) {
                Log.info("区画(" + a.ZONE_ID + ") " + a.NAME + "の所有者はBANされています!");
            }
        });
    }

    private void setRegionMember(UUID uuid, ProtectedRegion region) {
        DefaultDomain domain = new DefaultDomain();

        if (uuid != null) {
            domain.addPlayer(uuid);
        }

        region.setMembers(domain);
    }

    public List<String> getAreaNameList(String worldName) {
        return DAO.getAreaNameList(worldName);
    }

    public List<String> getZoneIdList(String worldName) {
        return DAO.getZoneIdList(worldName);
    }


    @Override
    public void onPlayerJoin(Player player) {
        SCHEDULER.runTaskLaterAsynchronously(PLUGIN, () -> {
            if (!player.isOnline()) {
                return;
            }

            List<String> expireList = new ArrayList<>(DAO.getOwnedRentalAreaExpireList(player.getUniqueId(), 7));

            if (expireList.size() > 0) {
                player.sendMessage(NOTIFY_RENTAL_AREA);
                expireList.forEach(player::sendMessage);
                player.sendMessage(NOTIFY_RENTAL_AREA_HINT);
            }
        }, 20L * 10);
    }

    @Override
    public void onSignPlace(SignChangeEvent event) {
        Player player = event.getPlayer();
        String areaName = event.getLine(1);

        if (areaName.length() < 1) {
            sendWarnMessage(player, "区画名を入力して下さい.");
            return;
        }

        Area area = DAO.getArea(player.getWorld().getName(), areaName);
        if (area == null) {
            sendUnknownArea(player, areaName);
            return;
        }

        if (area.SIGN_LOCATION != null) {
            sendWarnMessage(player, "この区画の看板はすでに" + area.SIGN_LOCATION.toString() + "に設置されています.");
            return;
        }

        if (!DAO.updateSignLocation(player.getWorld().getName(), areaName, event.getBlock().getLocation())) {
            sendErrorMessage(player, "データベースの更新に失敗しました.");
            return;
        }

        // 実処理
        event.setLine(0, SIGN_INDEX_TEXT);
        if(area.OWNER != null){
            event.setLine(2, ChatColor.DARK_GRAY + area.OWNER.getName());
            event.setLine(3, (area.EXPIRE_DATE == null) ? area.STATE.getName() : (ChatColor.DARK_PURPLE + Util.formatDate(area.EXPIRE_DATE)) + ChatColor.RESET + " まで");
        }else {
            event.setLine(2, "");
            event.setLine(3, area.STATE.getName());
        }
    }

    @Override
    public void onSignBreak(Player player, Sign sign) {
        Area area = DAO.getArea(player.getWorld().getName(), sign.getLine(1));
        if (area == null) {
            sendUnknownArea(player, sign.getLine(1));
            return;
        }

        if (!DAO.updateSignLocation(sign.getWorld().getName(), area.NAME, null)) {
            sendErrorMessage(player, "データベースの更新に失敗しました.");
        }
    }

    @Override
    public void onSignClick(Player player, Sign sign) {
        String areaName = sign.getLine(1);

        sendAreaInfo(player, areaName);
    }


    private void updateAreaSign(String worldName, String areaName) {
        Area area = DAO.getArea(worldName, areaName);
        if (area == null) {
            Log.warn("Area(" + areaName + ") not found.");
            return;
        }

        area.updateSign();
    }

    private boolean containBlock(World world, ProtectedRegion region) {
        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();

        final int TO_X = max.getBlockX();
        final int TO_Y = max.getBlockY();
        final int TO_Z = max.getBlockZ();

        for (int x = min.getBlockX(); x <= TO_X; x++) {
            for (int y = min.getBlockY(); y <= TO_Y; y++) {
                for (int z = min.getBlockZ(); z <= TO_Z; z++) {
                    if (world.getBlockAt(x, y, z).getState().getBlock().getType() != Material.AIR) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void cleanRegion(World world, ProtectedRegion region) {
        SHOP.removeShopInRegion(world, region);

        BlockVector min = region.getMinimumPoint();
        BlockVector max = region.getMaximumPoint();

        final int TO_X = max.getBlockX();
        final int TO_Y = max.getBlockY();
        final int TO_Z = max.getBlockZ();

        for (int x = min.getBlockX(); x <= TO_X; x++) {
            for (int y = min.getBlockY(); y <= TO_Y; y++) {
                for (int z = min.getBlockZ(); z <= TO_Z; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }
    }

    private void sendUnknownZone(Player player) {
        player.sendMessage(PREFIX + ChatColor.YELLOW + "区画が見つかりませんでした.");
    }

    private void sendUnknownArea(Player player, String areaName) {
        player.sendMessage(PREFIX + ChatColor.YELLOW + areaName + "は存在しません.");
        player.sendMessage(PREFIX + ChatColor.GRAY + "区画名が正しいか確認して下さい.");
    }

    private void sendWarnMessage(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.YELLOW + message);
    }

    private void sendErrorMessage(Player player, String message) {
        player.sendMessage(PREFIX + ChatColor.RED + message);
        player.sendMessage(PREFIX + ChatColor.GRAY + "運営に報告して下さい.");

        Log.error(message);
    }

    private static class AreaTradeTask {
        enum Type {BUY, RENTAL, SELL}

        private final Type TYPE;
        private final String AREA_NAME;
        private final double PRICE;

        AreaTradeTask(Type type, String areaName, double price) {
            TYPE = type;
            AREA_NAME = areaName;
            PRICE = price;
        }
    }

    private final String AREA_INFO_INDEX = ChatColor.translateAlternateColorCodes('&', "&7***************&9 区画情報&7 ***************");

    private final String NOTIFY_RENTAL_AREA      = ChatColor.translateAlternateColorCodes('&', PREFIX + "&eレンタル中の区画の期限が近づいています.");
    private final String NOTIFY_RENTAL_AREA_HINT = ChatColor.translateAlternateColorCodes('&', "&7/zone rental コマンドで更新して下さい.");
    private final String BUY_MESSAGE             = ChatColor.translateAlternateColorCodes('&', PREFIX + "{zone}&r {area}&r を &e\u00A5{price}&r で購入しますか？");
    private final String RENTAL_MESSAGE          = ChatColor.translateAlternateColorCodes('&', PREFIX + "{zone}&r {area}&r を &e\u00A5{price}&r で &a{days}日間&r レンタルしますか？");
    private final String SELL_MESSAGE            = ChatColor.translateAlternateColorCodes('&', PREFIX + "{zone}&r {area}&r を &e\u00A5{price}&r で売却しますか？");
    private final String CONFIRM_MESSAGE         = ChatColor.translateAlternateColorCodes('&', PREFIX + "&7{action}を確定するには &c/zone confirm&7 と入力して下さい.");
    private final String TASK_COMPLETE_MESSAGE   = ChatColor.translateAlternateColorCodes('&', PREFIX + "{zone}&r {area}&r を &e\u00A5{price}&r で{action}しました！");
    private final String RENTAL_PERIOD_MESSAGE   = ChatColor.translateAlternateColorCodes('&', PREFIX + "&dレンタル有効期限&rは &c{date}&r です.");
}

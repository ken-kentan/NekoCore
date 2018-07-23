package jp.kentan.minecraft.neko_core.dao;

import jp.kentan.minecraft.neko_core.component.zone.Area;
import jp.kentan.minecraft.neko_core.component.zone.AreaState;
import jp.kentan.minecraft.neko_core.component.zone.Zone;
import jp.kentan.minecraft.neko_core.component.zone.ZoneType;
import jp.kentan.minecraft.neko_core.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.sql.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class ZoneDao extends Dao {

    public ZoneDao() {
        super();

        createTablesIfNeed();
    }

    private void createTablesIfNeed() {
        try (Connection conn = super.getConnection()) {
            try (Statement statement = conn.createStatement()) {

                // Zone
                statement.execute("CREATE TABLE IF NOT EXISTS neko_zones (" +
                        "id VARCHAR(36) NOT NULL PRIMARY KEY, " +
                        "name TINYTEXT NOT NULL, " +
                        "type VARCHAR(16) NOT NULL, " +
                        "world VARCHAR(36) NOT NULL, " +
                        "owner_limit INT NOT NULL, " +
                        "rental_days INT NOT NULL, " +
                        "price_rate DOUBLE NOT NULL, " +
                        "price_rate_gain DOUBLE NOT NULL, " +
                        "sell_gain DOUBLE NOT NULL, " +
                        "buy_rental_rule TEXT, " +
                        "sell_rule TEXT)");

                // Area
                statement.execute("CREATE TABLE IF NOT EXISTS neko_areas (" +
                        "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "name TINYTEXT NOT NULL, " +
                        "world VARCHAR(36) NOT NULL, " +
                        "zone_id TINYTEXT NOT NULL, " +
                        "region_id TINYTEXT NOT NULL, " +
                        "region_size INT NOT NULL, " +
                        "state VARCHAR(16) NOT NULL, " +
                        "sign_location VARCHAR(255) UNIQUE, " + // (World,x,y,z)
                        "owner VARCHAR(64), " +
                        "buy_rental_price DOUBLE, " +
                        "expire_date DATETIME)");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Zone getZone(String zoneId) {
        Zone zone = null;

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM neko_zones WHERE id = ? LIMIT 1")) {

                st.setString(1, zoneId);

                try (ResultSet result = st.executeQuery()) {

                    if (result.next()) {
                        zone = new Zone(
                                result.getString("id"),
                                result.getString("name"),
                                ZoneType.valueOf(result.getString("type")),
                                Bukkit.getWorld(result.getString("world")),
                                result.getInt("owner_limit"),
                                result.getInt("rental_days"),
                                result.getDouble("price_rate"),
                                result.getDouble("price_rate_gain"),
                                result.getDouble("sell_gain"),
                                result.getString("buy_rental_rule"),
                                result.getString("sell_rule")
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zone;
    }

    public List<Zone> getZoneList() {
        List<Zone> zoneList = new ArrayList<>();

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM neko_zones ORDER BY world")) {
                try (ResultSet result = st.executeQuery()) {

                    while (result.next()) {
                        zoneList.add(new Zone(
                                result.getString("id"),
                                result.getString("name"),
                                ZoneType.valueOf(result.getString("type")),
                                Bukkit.getWorld(result.getString("world")),
                                result.getInt("owner_limit"),
                                result.getInt("rental_days"),
                                result.getDouble("price_rate"),
                                result.getDouble("price_rate_gain"),
                                result.getDouble("sell_gain"),
                                result.getString("buy_rental_rule"),
                                result.getString("sell_rule")
                        ));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zoneList;
    }

    public Area getArea(String worldName, String areaName) {
        Area area = null;

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM neko_areas WHERE world = ? AND name = ? LIMIT 1")) {

                st.setString(1, worldName);
                st.setString(2, areaName);

                try (ResultSet result = st.executeQuery()) {

                    if (result.next()) {
                        String strUuid  = result.getString("owner");
                        java.sql.Timestamp expireDate = result.getTimestamp("expire_date");

                        area = new Area(
                                result.getString("name"),
                                result.getString("world"),
                                result.getString("zone_id"),
                                result.getString("region_id"),
                                result.getInt("region_size"),
                                AreaState.valueOf(result.getString("state")),
                                textToLocation(result.getString("sign_location")),
                                (strUuid != null) ? UUID.fromString(strUuid) : null,
                                result.getDouble("buy_rental_price"),
                                (expireDate != null) ? expireDate.toInstant().atZone(ZoneId.of("Asia/Tokyo")) : null
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return area;
    }

    public List<Area> getAreaList() {
        List<Area> areaList = new ArrayList<>();

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM neko_areas")) {
                try (ResultSet result = st.executeQuery()) {
                    while (result.next()) {
                        String strUuid  = result.getString("owner");
                        java.sql.Timestamp expireDate = result.getTimestamp("expire_date");

                        areaList.add(new Area(
                                result.getString("name"),
                                result.getString("world"),
                                result.getString("zone_id"),
                                result.getString("region_id"),
                                result.getInt("region_size"),
                                AreaState.valueOf(result.getString("state")),
                                textToLocation(result.getString("sign_location")),
                                (strUuid != null) ? UUID.fromString(strUuid) : null,
                                result.getDouble("buy_rental_price"),
                                (expireDate != null) ? expireDate.toInstant().atZone(ZONE_JAPAN) : null
                        ));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return areaList;
    }

    public List<String> getOwnedRentalAreaExpireList(UUID uuid, int days) {
        List<String> areaList = new ArrayList<>();

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT DATE_FORMAT(expire_date, '%Y/%m/%d %H:%i'), z.name, a.name FROM neko_areas a, neko_zones z WHERE a.zone_id = z.id AND owner = ? AND expire_date IS NOT NULL AND expire_date < ? ORDER BY expire_date")) {

                ZonedDateTime date = ZonedDateTime.now().plusDays(days);

                st.setString(1, uuid.toString());
                st.setTimestamp(2, java.sql.Timestamp.from(date.toInstant()));

                try (ResultSet result = st.executeQuery()) {
                    while (result.next()) {
                        areaList.add(
                                ChatColor.RED + result.getString(1) + ChatColor.RESET + ": " +
                                ChatColor.translateAlternateColorCodes('&', result.getString(2)) +
                                ChatColor.RESET + ' ' + result.getString(3)
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return areaList;
    }

    public List<Area> takeExpiredRentalAreas() {
        List<Area> areaList = new ArrayList<>();

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM neko_areas WHERE state = ? AND expire_date IS NOT NULL AND expire_date < NOW()")) {

                st.setString(1, AreaState.SOLD.toString());

                try (ResultSet result = st.executeQuery()) {
                    while (result.next()) {
                        areaList.add(new Area(
                                result.getString("name"),
                                result.getString("world"),
                                result.getString("zone_id"),
                                result.getString("region_id"),
                                result.getInt("region_size"),
                                AreaState.ON_SALE,
                                textToLocation(result.getString("sign_location")),
                                null,
                                0D,
                                null
                        ));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            try (PreparedStatement st = conn.prepareStatement("UPDATE neko_areas SET owner = NULL, state = ?, buy_rental_price = NULL, expire_date = NULL WHERE state = ? AND expire_date IS NOT NULL AND expire_date < NOW()")) {

                st.setString(1, AreaState.ON_SALE.toString());
                st.setString(2, AreaState.SOLD.toString());

                if (st.executeUpdate() != areaList.size()) {
                    areaList.clear();
                    conn.rollback();

                    Log.error("invalid expired area data.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            conn.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return areaList;
    }

    public double getBuyPrice(UUID uuid, String zoneId, String areaName) {
        int countOwnedArea = getCountOwnedAreaPerZone(uuid, zoneId);

        int regionSize = -1;

        try (Connection conn = super.getConnection()) {

            // エリア情報取得
            try (PreparedStatement st = conn.prepareStatement("SELECT region_size FROM neko_areas WHERE zone_id = ? AND name = ?")) {

                st.setString(1, zoneId);
                st.setString(2, areaName);

                try (ResultSet result = st.executeQuery()) {
                    if (result.next()) {
                        regionSize = result.getInt(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return calcAreaPrice(zoneId, regionSize, countOwnedArea);
    }

    public double getRentalPrice(UUID uuid, String zoneId, String areaName) {
        int countOwnedArea = getCountOwnedAreaPerZone(uuid, zoneId);

        int regionSize = -1;

        try (Connection conn = super.getConnection()) {

            // エリア情報取得
            try (PreparedStatement st = conn.prepareStatement("SELECT region_size, owner FROM neko_areas WHERE zone_id = ? AND name = ?")) {

                st.setString(1, zoneId);
                st.setString(2, areaName);

                try (ResultSet result = st.executeQuery()) {
                    if (result.next()) {
                        regionSize = result.getInt(1);

                        // 所有者であればトータル所有数にカウントしない
                        String strUuid = result.getString(2);
                        if (strUuid != null && UUID.fromString(strUuid).equals(uuid)) {
                            countOwnedArea--;
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return calcAreaPrice(zoneId, regionSize, countOwnedArea);
    }

    private double calcAreaPrice(String zoneId, int regionSize, int countOwnedArea) {
        if (countOwnedArea < 0) { //failed
            return -1D;
        }

        double price = -1D;

        try (Connection conn = super.getConnection()) {
            // 区画価格計算パラメータ取得
            try (PreparedStatement st = conn.prepareStatement("SELECT price_rate, price_rate_gain, owner_limit FROM neko_zones WHERE id = ?")) {

                st.setString(1, zoneId);

                try (ResultSet result = st.executeQuery()) {

                    // 価格計算処理
                    if (result.next()) {
                        double rate = result.getDouble(1);
                        double rateGain = result.getDouble(2);
                        final int OWNER_LIMIT = result.getInt(3);

                        countOwnedArea = Math.min(countOwnedArea, OWNER_LIMIT - 1);

                        if (countOwnedArea > 0) {
                            rate *= rateGain * countOwnedArea;
                        }

                        price = rate * regionSize;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return price;
    }

    public double getSellPrice(String worldName, String areaName) {
        double sellPrice = -1D;

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT z.sell_gain * a.buy_rental_price FROM neko_zones z, neko_areas a WHERE a.world = ? AND a.name = ? AND a.zone_id = z.id")) {

                st.setString(1, worldName);
                st.setString(2, areaName);

                try (ResultSet result = st.executeQuery()) {
                    if (result.next()) {
                        sellPrice = result.getDouble(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return sellPrice;
    }

    public Map<String, List<String>> getOwnedAreaMap(UUID uuid) {
        Map<String, List<String>> areaMap = new HashMap<>();

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT z.name, a.name FROM neko_zones z, neko_areas a WHERE z.id = a.zone_id AND owner = ? ORDER BY z.name, a.name")) {

                st.setString(1, uuid.toString());

                try (ResultSet result = st.executeQuery()) {

                    while (result.next()) {
                        String zoneName = result.getString(1);

                        if (areaMap.containsKey(zoneName)) {
                            areaMap.get(zoneName).add(result.getString(2));
                        } else {
                            areaMap.put(zoneName, new ArrayList<>(Collections.singletonList(result.getString(2))));
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return areaMap;
    }

    public List<String> getAreaNameList(String worldName) {
        List<String> nameList = new ArrayList<>();

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT name FROM neko_areas WHERE world = ? ORDER BY name")) {

                st.setString(1, worldName);

                try (ResultSet result = st.executeQuery()) {

                    while (result.next()) {
                        nameList.add(result.getString(1));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nameList;
    }

    public String getZoneName(String zoneId) {
        String zoneName = null;

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT name FROM neko_zones WHERE id = ? LIMIT 1")) {

                st.setString(1, zoneId);

                try (ResultSet result = st.executeQuery()) {

                    if (result.next()) {
                        zoneName = ChatColor.translateAlternateColorCodes('&', result.getString(1));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return zoneName;
    }

    public List<String> getZoneIdList(String worldName) {
        List<String> idList = new ArrayList<>();

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT id FROM neko_zones WHERE world = ? ORDER BY id")) {

                st.setString(1, worldName);

                try (ResultSet result = st.executeQuery()) {

                    while (result.next()) {
                        idList.add(result.getString(1));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return idList;
    }

    public int getCountOwnedAreaPerZone(UUID uuid, String zoneId) {
        int count = -1;

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) FROM neko_areas WHERE zone_id = ? AND owner = ?")) {

                st.setString(1, zoneId);
                st.setString(2, uuid.toString());

                try (ResultSet result = st.executeQuery()) {

                    if (result.next()) {
                        count = result.getInt(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    public boolean isReachOwnedLimit(UUID uuid, String zoneId) {
        boolean isReach = true;

        final int COUNT_OWNED_AREA = getCountOwnedAreaPerZone(uuid, zoneId);

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT owner_limit FROM neko_zones WHERE id = ?")) {

                st.setString(1, zoneId);

                try (ResultSet result = st.executeQuery()) {
                    if (result.next()) {
                        isReach = (COUNT_OWNED_AREA >= result.getInt(1));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isReach;
    }

    public boolean buyArea(UUID uuid, String worldName, String areaName, double price) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("UPDATE neko_areas SET state = ?, owner = ?, buy_rental_price = ? WHERE world = ? AND name = ?")) {

                st.setString(1, AreaState.SOLD.toString());
                st.setString(2, uuid.toString());
                st.setDouble(3, price);

                st.setString(4, worldName);
                st.setString(5, areaName);

                countRow = st.executeUpdate();

                // 複数更新されたらロールバック
                if (countRow != 1) {
                    conn.rollback();
                }

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public boolean rentalArea(UUID uuid, String worldName, String areaName, double price, ZonedDateTime expireDate) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("UPDATE neko_areas SET state = ?, owner = ?, buy_rental_price = ?, expire_date = ? WHERE world = ? AND name = ?")) {

                st.setString(1, AreaState.SOLD.toString());
                st.setString(2, uuid.toString());
                st.setDouble(3, price);
                st.setTimestamp(4, java.sql.Timestamp.from(expireDate.toInstant()));

                st.setString(5, worldName);
                st.setString(6, areaName);

                countRow = st.executeUpdate();

                // 複数更新されたらロールバック
                if (countRow != 1) {
                    conn.rollback();
                }

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public boolean sellArea(UUID uuid, String worldName, String areaName) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("UPDATE neko_areas SET state = ?, owner = NULL, buy_rental_price = NULL, expire_date = NULL WHERE world = ? AND name = ? AND owner = ?")) {

                st.setString(1, AreaState.ON_SALE.toString());

                st.setString(2, worldName);
                st.setString(3, areaName);
                st.setString(4, uuid.toString());

                countRow = st.executeUpdate();

                // 複数更新されたらロールバック
                if (countRow != 1) {
                    conn.rollback();
                }

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public boolean updateSignLocation(String worldName, String areaName, Location location) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("UPDATE neko_areas SET sign_location = ? WHERE world = ? AND name = ?")) {

                st.setString(1, locationToText(location));

                st.setString(2, worldName);
                st.setString(3, areaName);

                countRow = st.executeUpdate();

                // 複数更新されたらロールバック
                if (countRow != 1) {
                    conn.rollback();
                }

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public boolean updateAreaState(String worldName, String areaName, AreaState areaState) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("UPDATE neko_areas SET state = ? WHERE world = ? AND name = ?")) {

                st.setString(1, areaState.toString());

                st.setString(2, worldName);
                st.setString(3, areaName);

                countRow = st.executeUpdate();

                // 複数更新されたらロールバック
                if (countRow != 1) {
                    conn.rollback();
                }

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public boolean addArea(String areaName, String worldName, String zoneId, String regionId, int regionSize) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("INSERT INTO neko_areas VALUES(NULL, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL, NULL )")) {

                st.setString(1, areaName);
                st.setString(2, worldName);
                st.setString(3, zoneId);
                st.setString(4, regionId);
                st.setInt(5, regionSize);
                st.setString(6, AreaState.ON_SALE.toString());

                countRow = st.executeUpdate();

                // 複数更新されたらロールバック
                if (countRow != 1) {
                    conn.rollback();
                }

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public boolean deleteArea(String worldName, String areaName) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("DELETE FROM neko_areas WHERE world = ? AND name = ?")) {

                st.setString(1, worldName);
                st.setString(2, areaName);

                countRow = st.executeUpdate();

                // 複数更新されたらロールバック
                if (countRow != 1) {
                    conn.rollback();
                }

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public boolean takeAreaFromOwner(String worldName, String areaName) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("UPDATE neko_areas SET owner = NULL, state = ?, buy_rental_price = NULL, expire_date = NULL WHERE world = ? AND name = ? AND owner IS NOT NULL")) {

                st .setString(1, AreaState.LOCK.toString());

                st.setString(2, worldName);
                st.setString(3, areaName);

                countRow = st.executeUpdate();

                // 複数更新されたらロールバック
                if (countRow != 1) {
                    conn.rollback();
                }

                conn.commit();
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    private Location textToLocation(String text) {
        if (text == null) {
            return null;
        }

        String[] split = text.split(",", 4);

        if (split.length < 4) {
            Log.error("invalid location format(" + text + ").");
            return null;
        }

        World world = Bukkit.getWorld(split[0]);
        if (world == null) {
            Log.error("world(" + split[0] + ") not found.");
            return null;
        }

        return new Location(
                world,
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2]),
                Double.parseDouble(split[3]));
    }

    private String locationToText(Location location) {
        if (location == null) {
            return null;
        }

        return location.getWorld().getName() + ',' + location.getX() + ',' + location.getY() + ',' + location.getZ();
    }
}

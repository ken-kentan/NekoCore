package jp.kentan.minecraft.neko_core.dao;

import jp.kentan.minecraft.neko_core.component.Advertisement;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdvertisementDao extends Dao {

    public AdvertisementDao() {
        super();

        createTablesIfNeed();
    }

    private void createTablesIfNeed() {
        try (Connection conn = super.getConnection()) {
            try (Statement statement = conn.createStatement()) {

                // Zone
                statement.execute("CREATE TABLE IF NOT EXISTS neko_ads (" +
                        "id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                        "owner VARCHAR(64) NOT NULL, " +
                        "content TEXT NOT NULL, " +
                        "created_date DATETIME NOT NULL, " +
                        "expire_date DATETIME NOT NULL, " +
                        "is_delete BOOLEAN NOT NULL)");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addAdvertisement(UUID uuid, String content, ZonedDateTime expireDate) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("INSERT INTO neko_ads VALUES(NULL, ?, ?, NOW(), ?, FALSE)")) {

                st.setString(1, uuid.toString());
                st.setString(2, content);
                st.setTimestamp(3, java.sql.Timestamp.from(expireDate.toInstant()));

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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public boolean deleteAdvertisement(UUID uuid) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement st = conn.prepareStatement("UPDATE neko_ads SET is_delete = TRUE WHERE owner = ? AND is_delete IS FALSE AND expire_date > NOW() LIMIT 1")) {

                st.setString(1, uuid.toString());

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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }

    public List<Advertisement> getAdvertisementList() {
        List<Advertisement> adList = new ArrayList<>();

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM neko_ads WHERE is_delete IS FALSE AND expire_date > NOW() ORDER BY id")) {
                try (ResultSet result = st.executeQuery()) {
                    while (result.next()) {
                        adList.add(new Advertisement(
                                UUID.fromString(result.getString("owner")),
                                result.getString("content"),
                                result.getTimestamp("created_date").toInstant().atZone(ZONE_JAPAN),
                                result.getTimestamp("expire_date").toInstant().atZone(ZONE_JAPAN),
                                result.getBoolean("is_delete")
                        ));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return adList;
    }

    public Advertisement getAdvertisement(UUID uuid) {
        Advertisement ad = null;

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT * FROM neko_ads WHERE owner = ? AND is_delete IS FALSE AND expire_date > NOW() LIMIT 1")) {

                st.setString(1, uuid.toString());

                try (ResultSet result = st.executeQuery()) {
                    if (result.next()) {
                        ad = new Advertisement(
                                UUID.fromString(result.getString("owner")),
                                result.getString("content"),
                                result.getTimestamp("created_date").toInstant().atZone(ZONE_JAPAN),
                                result.getTimestamp("expire_date").toInstant().atZone(ZONE_JAPAN),
                                result.getBoolean("is_delete")
                        );
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ad;
    }

    public boolean hasAdvertisement(UUID uuid) {
        int countRow = 0;

        try (Connection conn = super.getConnection()) {
            try (PreparedStatement st = conn.prepareStatement("SELECT COUNT(*) FROM neko_ads WHERE owner = ? AND is_delete IS FALSE AND expire_date > NOW() LIMIT 1")) {

                st.setString(1, uuid.toString());

                try (ResultSet result = st.executeQuery()) {
                    if (result.next()) {
                        countRow = result.getInt(1);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return countRow == 1;
    }
}

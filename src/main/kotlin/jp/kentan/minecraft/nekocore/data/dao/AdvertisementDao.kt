package jp.kentan.minecraft.nekocore.data.dao

import jp.kentan.minecraft.nekocore.data.model.Advertisement
import java.sql.ResultSet
import java.util.*

class AdvertisementDao(
    private val database: NekoCoreDatabase
) : BaseDao() {

    fun getAdvertisementList() =
        database.connection.use { conn ->
            val adList = mutableListOf<Advertisement>()

            val st =
                conn.prepareStatement("SELECT * FROM neko_ads WHERE is_delete IS FALSE AND expire_date > NOW() ORDER BY id")

            val result = st.executeQuery()
            while (result.next()) {
                adList.add(result.toAdvertisement())
            }

            st.close()
            return@use adList
        }

    fun getAdvertisement(uuid: UUID) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("SELECT * FROM neko_ads WHERE owner = ? AND is_delete IS FALSE AND expire_date > NOW() LIMIT 1")
            st.setString(1, uuid.toString())

            val result = st.executeQuery()
            val ad = if (result.next()) result.toAdvertisement() else null

            st.close()
            return@use ad
        }

    fun getAdvertisementCount(uuid: UUID) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("SELECT COUNT(id) FROM neko_ads WHERE owner = ? AND is_delete IS FALSE AND expire_date > NOW() LIMIT 1")
            st.setString(1, uuid.toString())

            val result = st.executeQuery()
            val count = if (result.next()) result.getInt(1) else 0

            st.close()
            return@use count
        }

    fun insertAdvertisement(uuid: UUID, content: String, expiredDate: Date) =
        database.connection.use { conn ->
            val st = conn.prepareStatement("INSERT INTO neko_ads VALUES(NULL, ?, ?, NOW(), ?, FALSE)")
            st.setString(1, uuid.toString())
            st.setString(2, content)
            st.setTimestamp(3, java.sql.Timestamp(expiredDate.time))

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }

    fun deleteAdvertisement(uuid: UUID) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("UPDATE neko_ads SET is_delete = TRUE WHERE owner = ? AND is_delete IS FALSE AND expire_date > NOW() LIMIT 1")
            st.setString(1, uuid.toString())

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }

    private fun ResultSet.toAdvertisement(): Advertisement =
        Advertisement(getOfflinePlayer(2), getChatColoredString(3), getTimestamp(5))
}
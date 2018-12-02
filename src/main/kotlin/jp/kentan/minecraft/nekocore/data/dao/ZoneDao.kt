package jp.kentan.minecraft.nekocore.data.dao

import jp.kentan.minecraft.nekocore.data.model.Zone
import java.sql.ResultSet

class ZoneDao(
    private val database: NekoCoreDatabase
) : BaseDao() {

    fun getZoneList() =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT * FROM neko_zones ORDER BY world")

            val zoneList = mutableListOf<Zone>()

            val result = st.executeQuery()
            while (result.next()) {
                result.toZone()?.let { zoneList.add(it) }
            }

            st.close()
            return@use zoneList
        }

    fun getZone(id: String) =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT * FROM neko_zones WHERE id = ? LIMIT 1")
            st.setString(1, id)

            val result = st.executeQuery()
            val zone = if (result.next()) result.toZone() else null

            st.close()
            return@use zone
        }

    private fun ResultSet.toZone(): Zone? {
        return Zone(
            id = getString(1),
            name = getChatColoredString(2),
            type = Zone.Type.valueOf(getString(3)),
            world = getWorldOrNull(4) ?: return null,
            ownedLimit = getInt(5),
            rentalDays = getInt(6),
            priceRate = getDouble(7),
            priceRateGain = getDouble(8),
            sellGain = getDouble(9),
            buyRentalRule = getChatColoredStringOrNull(10),
            sellRule = getChatColoredStringOrNull(11)
        )
    }
}
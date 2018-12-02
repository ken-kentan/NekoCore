package jp.kentan.minecraft.nekocore.data.dao

import jp.kentan.minecraft.nekocore.data.model.Area
import jp.kentan.minecraft.nekocore.util.dayToMills
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*

class AreaDao(
    private val database: NekoCoreDatabase
) : BaseDao() {

    fun getAreaList(world: String?) =
        database.connection.use { conn ->
            val st = if (world == null) {
                conn.prepareStatement("SELECT name FROM neko_areas")
            } else {
                conn.prepareStatement("SELECT name FROM neko_areas WHERE world = ?").apply {
                    setString(1, world)
                }
            }
            st.queryTimeout = 2

            val areaList = mutableListOf<String>()

            val result = st.executeQuery()
            while (result.next()) {
                areaList.add(result.getString(1))
            }

            st.close()
            return@use areaList
        }

    fun getExpiredAreaList() =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("SELECT * FROM neko_areas WHERE expire_date IS NOT NULL AND expire_date < NOW() AND state = ?")
            st.setString(1, Area.State.SOLD.toString())

            val result = st.executeQuery()
            val areaList = mutableListOf<Area>()
            while (result.next()) {
                result.toArea()?.let { areaList.add(it) }
            }

            st.close()
            return@use areaList
        }

    fun getOwnedRentalExpireAreaList(owner: UUID, days: Int) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("SELECT DATE_FORMAT(expire_date, '%Y/%m/%d %H:%i'), z.name, a.name FROM neko_areas a INNER JOIN neko_zones z ON z.id = a.zone_id WHERE expire_date IS NOT NULL AND expire_date < ? AND owner = ? ORDER BY expire_date")
            st.setTimestamp(1, Timestamp(System.currentTimeMillis() + days.dayToMills()))
            st.setString(2, owner.toString())

            val result = st.executeQuery()
            val areaList = mutableListOf<String>()
            while (result.next()) {
                areaList.add("§c${result.getString(1)}§r: ${result.getChatColoredString(2)}§r ${result.getString(3)}")
            }

            st.close()
            return@use areaList
        }

    fun getOwnedAreaMap(owner: UUID) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("SELECT z.name, a.name FROM neko_areas a INNER JOIN neko_zones z ON z.id = a.zone_id WHERE owner = ? ORDER BY z.name, a.name")
            st.setString(1, owner.toString())

            val result = st.executeQuery()
            val areaMap = mutableMapOf<String, MutableList<String>>()
            while (result.next()) {
                val zone = result.getChatColoredString(1)
                val area = result.getChatColoredString(2)

                if (areaMap.containsKey(zone)) {
                    areaMap[zone]?.add(area)
                } else {
                    areaMap[zone] = mutableListOf(area)
                }
            }

            st.close()
            return@use areaMap
        }

    fun getArea(world: String, name: String) =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT * FROM neko_areas WHERE world = ? AND name = ? LIMIT 1")
            st.setString(1, world)
            st.setString(2, name)

            val result = st.executeQuery()
            val area = if (result.next()) result.toArea() else null

            st.close()
            return@use area
        }

    fun getArea(id: Int) =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT * FROM neko_areas WHERE id = ?")
            st.setInt(1, id)

            val result = st.executeQuery()
            val area = if (result.next()) result.toArea() else null

            st.close()
            return@use area
        }

    fun insertArea(name: String, world: String, zoneId: String, regionId: String, regionSize: Int) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("INSERT INTO neko_areas VALUES(NULL, ?, ?, ?, ?, ?, ?, NULL, NULL, NULL, NULL )")
            st.setString(1, name)
            st.setString(2, world)
            st.setString(3, zoneId)
            st.setString(4, regionId)
            st.setInt(5, regionSize)
            st.setString(6, Area.State.ON_SALE.name)

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }

    fun deleteArea(world: String, name: String) =
        database.connection.use { conn ->
            val st = conn.prepareStatement("DELETE FROM neko_areas WHERE world = ? AND name = ?")
            st.setString(1, world)
            st.setString(2, name)

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }

    fun updateArea(area: Area) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("UPDATE neko_areas SET state = ?, sign_location = ?, owner = ?, buy_rental_price = ?, expire_date = ? WHERE id = ?")
            st.setString(1, area.state.name)
            st.setLocation(2, area.signLocation)
            st.setString(3, area.owner?.uniqueId?.toString())
            st.setDouble(4, area.purchasedPrice)
            if (area.expiredDate != null) {
                st.setTimestamp(5, Timestamp(area.expiredDate.time))
            } else {
                st.setTimestamp(5, null)
            }
            st.setInt(6, area.id)

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }

    fun getAreaCount(world: String, name: String) =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT COUNT(id) FROM neko_areas WHERE world = ? AND name = ?")
            st.setString(1, world)
            st.setString(2, name)
            st.setString(6, Area.State.ON_SALE.name)

            val result = st.executeQuery()
            val count = if (result.next()) result.getInt(1) else 0

            st.close()
            return@use count
        }

    fun getOwnedAreaCount(owner: UUID, zoneId: String) =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT COUNT(id) FROM neko_areas WHERE owner = ? AND zone_id = ?")
            st.setString(1, owner.toString())
            st.setString(2, zoneId)

            val result = st.executeQuery()
            val count = if (result.next()) result.getInt(1) else 0

            st.close()
            return@use count
        }

    private fun ResultSet.toArea(): Area? {
        return Area(
            id = getInt(1),
            name = getString(2),
            world = getWorldOrNull(3) ?: return null,
            zoneId = getString(4),
            regionId = getString(5),
            regionSize = getInt(6),
            state = Area.State.valueOf(getString(7)),
            signLocation = getLocation(8),
            owner = getOfflinePlayerOrNull(9),
            purchasedPrice = getDouble(10),
            expiredDate = getTimestamp(11)
        )
    }
}
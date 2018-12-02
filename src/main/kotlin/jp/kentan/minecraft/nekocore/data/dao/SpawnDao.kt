package jp.kentan.minecraft.nekocore.data.dao

import org.bukkit.Bukkit
import org.bukkit.Location

class SpawnDao(
    private val database: NekoCoreDatabase
) : BaseDao() {

    fun getSpawnMap() =
        database.connection.use { conn ->
            val st = conn.prepareStatement("SELECT * FROM neko_spawn")

            val spawnMap = mutableMapOf<String, Location>()

            val result = st.executeQuery()
            while (result.next()) {
                val name = result.getString(2)
                val location = Location(
                    Bukkit.getWorld(result.getString(3)),
                    result.getDouble(4),
                    result.getDouble(5),
                    result.getDouble(6),
                    result.getFloat(7),
                    result.getFloat(8)
                )

                spawnMap[name] = location
            }

            st.close()
            return@use spawnMap
        }

    fun insertSpawn(name: String, location: Location) =
        database.connection.use { conn ->
            val st =
                conn.prepareStatement("INSERT INTO neko_spawn VALUES(null,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE world=VALUES(world), x=VALUES(x), y=VALUES(y), z=VALUES(z), yaw=VALUES(yaw), pitch=VALUES(pitch)")
            st.setString(1, name)
            st.setString(2, location.world.name)
            st.setDouble(3, location.x)
            st.setDouble(4, location.y)
            st.setDouble(5, location.z)
            st.setFloat(6, location.yaw)
            st.setFloat(7, location.pitch)

            val count = st.executeUpdate()

            st.close()
            return@use count > 0
        }
}
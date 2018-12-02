package jp.kentan.minecraft.nekocore.data

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.dao.SpawnDao
import org.bukkit.Location
import java.util.logging.Level


class SpawnRepository(plugin: NekoCorePlugin) {

    private val spawnDao = SpawnDao(plugin.database)
    private val logger = plugin.logger

    fun getSpawnMap(): Map<String, Location> = try {
        spawnDao.getSpawnMap()
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get spawn map.", e)
        emptyMap()
    }

    fun addSpawn(name: String, location: Location): Boolean = try {
        spawnDao.insertSpawn(name, location)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to insert spawn map.", e)
        false
    }
}
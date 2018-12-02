package jp.kentan.minecraft.nekocore.data

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.dao.AreaDao
import jp.kentan.minecraft.nekocore.data.dao.ZoneDao
import jp.kentan.minecraft.nekocore.data.model.Area
import jp.kentan.minecraft.nekocore.data.model.Zone
import java.util.*
import java.util.logging.Level

class ZoneRepository(plugin: NekoCorePlugin) {

    private val zoneDao = ZoneDao(plugin.database)
    private val areaDao = AreaDao(plugin.database)
    private val logger = plugin.logger

    fun getZoneList(): List<Zone> = try {
        zoneDao.getZoneList()
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get zone list.", e)
        emptyList()
    }

    fun getZone(id: String): Zone? = try {
        zoneDao.getZone(id)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get zone($id).", e)
        null
    }

    fun getAreaNameList(world: String?): List<String> = try {
        areaDao.getAreaList(world)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get area name list.", e)
        emptyList()
    }

    fun getArea(world: String, name: String): Area? = try {
        areaDao.getArea(world, name)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get area($name).", e)
        null
    }

    fun getArea(id: Int): Area? = try {
        areaDao.getArea(id)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get area($id).", e)
        null
    }

    fun getExpiredAreaList(): List<Area> = try {
        areaDao.getExpiredAreaList()
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get expired area list.", e)
        emptyList()
    }

    fun getOwnedRentalExpireAreaList(owner: UUID, days: Int): List<String> = try {
        areaDao.getOwnedRentalExpireAreaList(owner, days)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get expire area list.", e)
        emptyList()
    }

    fun getOwnedAreaMap(owner: UUID): Map<String, List<String>> = try {
        areaDao.getOwnedAreaMap(owner)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get owned($owner) area map.", e)
        emptyMap()
    }

    fun addArea(name: String, world: String, zoneId: String, regionId: String, regionSize: Int): Boolean = try {
        areaDao.insertArea(name, world, zoneId, regionId, regionSize)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to insert area($name).", e)
        false
    }

    fun removeArea(world: String, name: String): Boolean = try {
        areaDao.deleteArea(world, name)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to delete area($name).", e)
        false
    }

    fun existArea(world: String, name: String): Boolean = try {
        areaDao.getAreaCount(world, name) > 0
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get area($name) count.", e)
        false
    }

    fun updateArea(area: Area): Boolean = try {
        areaDao.updateArea(area)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to update area(${area.id}, ${area.name}).", e)
        false
    }

    fun getOwnedAreaCount(owner: UUID, zoneId: String): Int = try {
        areaDao.getOwnedAreaCount(owner, zoneId)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get owned($owner) area count.", e)
        0
    }
}
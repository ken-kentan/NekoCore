package jp.kentan.minecraft.nekocore.data

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.dao.AdvertisementDao
import jp.kentan.minecraft.nekocore.data.model.Advertisement
import java.util.*
import java.util.logging.Level

class AdvertisementRepository(plugin: NekoCorePlugin) {

    private val adDao = AdvertisementDao(plugin.database)
    private val logger = plugin.logger

    fun getAdvertisementList(): List<Advertisement> = try {
        adDao.getAdvertisementList()
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get ad list.", e)
        emptyList()
    }

    fun getAdvertisement(uuid: UUID): Advertisement? = try {
        adDao.getAdvertisement(uuid)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get ad($uuid).", e)
        null
    }

    fun addAdvertisement(uuid: UUID, content: String, expiredDate: Date): Boolean = try {
        adDao.insertAdvertisement(uuid, content, expiredDate)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to insert ad($uuid).", e)
        false
    }

    fun deleteAdvertisement(uuid: UUID): Boolean = try {
        adDao.deleteAdvertisement(uuid)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to delete ad($uuid).", e)
        false
    }

    fun hasAdvertisement(uuid: UUID): Boolean = try {
        adDao.getAdvertisementCount(uuid) > 0
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get ad($uuid) count.", e)
        false
    }
}
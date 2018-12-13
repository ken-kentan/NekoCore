package jp.kentan.minecraft.nekocore.data

import com.squareup.moshi.Moshi
import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.adapter.UuidJsonAdapter
import jp.kentan.minecraft.nekocore.data.api.MojangService
import jp.kentan.minecraft.nekocore.data.dao.PlayerDao
import jp.kentan.minecraft.nekocore.data.model.AdvertiseFrequency
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.TlsVersion
import org.bukkit.Bukkit
import org.jsoup.Jsoup
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*
import java.util.logging.Level


class PlayerRepository(plugin: NekoCorePlugin) {

    companion object {
        private const val MOJANG_API_URL = "https://api.mojang.com"
        private const val JAPAN_MINECRAFT_SERVERS_URL = "https://minecraft.jp/servers/dekitateserver.com/"
    }

    private val mojangService: MojangService
    private val httpClient: OkHttpClient
    private val playerDao = PlayerDao(plugin.database)
    private val logger = plugin.logger

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(MOJANG_API_URL)
            .addConverterFactory(
                MoshiConverterFactory.create(
                    Moshi.Builder()
                        .add(UuidJsonAdapter())
                        .build()
                )
            )
            .build()
        mojangService = retrofit.create(MojangService::class.java)

        val spec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .allEnabledCipherSuites()
            .build()
        httpClient = OkHttpClient.Builder()
            .connectionSpecs(listOf(spec))
            .build()
    }

    fun getUniqueId(username: String): UUID? {
        @Suppress("DEPRECATION")
        val offlinePlayer = Bukkit.getOfflinePlayer(username)
        if (offlinePlayer != null) {
            return offlinePlayer.uniqueId
        }

        try {
            val response = mojangService.getMojangUser(username).execute()
            return response.body()?.uniqueId
        } catch (e: Exception) {
            logger.warning(e.message)
            e.printStackTrace()
        }

        return null
    }

    fun getVoteData(uuid: UUID): Pair<Int, Date?> = try {
        playerDao.getVoteData(uuid)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get vote data($uuid).", e)
        Pair(0, null)
    }

    fun updateLastServerVoteDate(uuid: UUID, continuous: Int): Boolean = try {
        playerDao.updateVoteData(uuid, continuous)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to update last vote data($uuid, $continuous).", e)
        false
    }

    fun getPendingCommandList(uuid: UUID): List<String> = try {
        playerDao.getPendingCommandList(uuid)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get pending command list($uuid).", e)
        emptyList()
    }

    fun addPendingCommandList(uuid: UUID, commandList: List<String>): Unit = try {
        playerDao.addPendingCommandList(uuid, commandList)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to update pending command list($uuid).", e)
    }

    fun clearPendingCommandList(uuid: UUID): Boolean = try {
        playerDao.deletePendingCommandList(uuid)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to delete pending command list($uuid).", e)
        false
    }

    fun hasVotedOnJapanMinecraftServers(targetUuid: UUID, targetUsername: String): Boolean? {
        val request = Request.Builder()
            .url(JAPAN_MINECRAFT_SERVERS_URL)
            .build()

        val body = try {
            val response = httpClient.newCall(request).execute()
            response.body() ?: return null
        } catch (e: Exception) {
            logger.log(Level.WARNING, "failed to get jms page.", e)
            return null
        }

        val votedUsernameList = Jsoup.parse(body.string()).select("ul.nav.avatar-list.players-icon li a img")
            .mapNotNull { it?.attr("alt") }
        if (votedUsernameList.none { it == targetUsername }) {
            return null
        }

        val (_, targetLastVoteDate) = getVoteData(targetUuid)
        if (targetLastVoteDate == null) {
            return true
        }

        // 投票を確認したら, 前のプレイヤーの投票時間から判断
        var hasVoted = false
        votedUsernameList.forEach { username ->
            if (!hasVoted) {
                hasVoted = username == targetUsername
                return@forEach
            }

            val uuid = getUniqueId(username) ?: return@forEach
            val (_, lastVoteDate) = getVoteData(uuid)

            return targetLastVoteDate.time < lastVoteDate?.time ?: return@forEach
        }

        return false
    }

    fun getAdvertiseFrequency(uuid: UUID): AdvertiseFrequency = try {
        val name = playerDao.getAdvertiseFrequency(uuid) ?: AdvertiseFrequency.MIDDLE.name
        AdvertiseFrequency.valueOf(name)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to get ad freq($uuid).", e)
        AdvertiseFrequency.MIDDLE
    }

    fun updateAdvertiseFrequency(uuid: UUID, freq: AdvertiseFrequency): Boolean = try {
        playerDao.updateAdvertiseFrequency(uuid, freq.name)
    } catch (e: Exception) {
        logger.log(Level.SEVERE, "failed to update ad freq($uuid).", e)
        false
    }
}
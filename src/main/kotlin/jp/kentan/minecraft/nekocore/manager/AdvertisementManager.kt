package jp.kentan.minecraft.nekocore.manager

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.AdvertisementRepository
import jp.kentan.minecraft.nekocore.data.PlayerRepository
import jp.kentan.minecraft.nekocore.data.model.AdvertiseFrequency
import jp.kentan.minecraft.nekocore.data.model.Advertisement
import jp.kentan.minecraft.nekocore.util.dayToMills
import jp.kentan.minecraft.nekocore.util.formatYearMonthDayHm
import jp.kentan.minecraft.nekocore.util.sendDatabaseUpdateError
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList

class AdvertisementManager(
    private val plugin: NekoCorePlugin
) {

    companion object {
        private const val PREFIX = "§7[§d広告§7]§r "
        private const val BASE_INTERVAL_TICK = 20L * 60 * 5 //5m
        private const val PRICE_PER_DAY = 100
    }

    private enum class Type { SET, UNSET }

    private val playerRepo = PlayerRepository(plugin)
    private val adRepo = AdvertisementRepository(plugin)

    private val advertisementList = Collections.synchronizedList(ArrayList<Advertisement>())
    private val playerMap: MutableMap<Player, AdvertiseFrequency> = ConcurrentHashMap()

    private val confirmTaskMap: MutableMap<Player, ConfirmTask> = ConcurrentHashMap()

    init {
        GlobalScope.launch {
            syncLatestAds()
            playerMap.putAll(Bukkit.getOnlinePlayers().map { it to playerRepo.getAdvertiseFrequency(it.uniqueId) })
        }

        plugin.bukkitEventListener.subscribePlayerJoin(::onPlayerJoined)
        plugin.bukkitEventListener.subscribePlayerQuit(::onPlayerQuited)

        startBroadcastAsyncTask()
    }

    private fun syncLatestAds() {
        GlobalScope.launch {
            advertisementList.clear()
            advertisementList.addAll(adRepo.getAdvertisementList())
        }
    }

    fun addSetAdConfirmTask(player: Player, strPeriodDays: String, contents: List<String>) {
        val periodDays = strPeriodDays.toIntOrNull() ?: let {
            player.sendMessage("$PREFIX${strPeriodDays}§eは整数ではありません.")
            return
        }

        if (periodDays !in 1..15) {
            player.sendMessage("${PREFIX}§e日数は 1～15 の範囲で入力してください.")
            return
        }

        val content: String = ChatColor.translateAlternateColorCodes('&', contents.joinToString(separator = " "))

        if (content.length < 10) {
            player.sendMessage("${PREFIX}§e広告の内容が短すぎます.")
            return
        }
        if (content.length > 140) {
            player.sendMessage("${PREFIX}§e140文字を超える広告は登録できません.")
            return
        }

        GlobalScope.launch {
            if (adRepo.hasAdvertisement(player.uniqueId)) {
                player.sendMessage("${PREFIX}§eすでに広告が登録されています.")
                player.sendMessage("$PREFIX/ad unset で以前の広告を消去して下さい.")
                return@launch
            }

            // preview
            player.sendMessage(PREFIX + content)

            confirmTaskMap.remove(player)

            player.sendMessage("${PREFIX}この広告を §a${periodDays}日間 §e\u00A5${PRICE_PER_DAY * periodDays} §rで登録しますか？")
            player.sendMessage("${PREFIX}§7登録を確定するには §c/ad confirm§7 と入力して下さい.")

            confirmTaskMap[player] = ConfirmTask(Type.SET, content, periodDays)
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable { confirmTaskMap.remove(player) }, 20L * 30)
        }
    }

    fun addUnsetAdConfirmTask(player: Player) {
        GlobalScope.launch {
            val ad = adRepo.getAdvertisement(player.uniqueId) ?: let {
                player.sendMessage("${PREFIX}§e広告が登録されていません.")
                return@launch
            }

            // preview
            player.sendMessage(PREFIX + ad.content)

            confirmTaskMap.remove(player)

            player.sendMessage("${PREFIX}この広告を消去しますか？")
            player.sendMessage("${PREFIX}§7消去を確定するには §c/ad confirm§7 と入力して下さい.")

            confirmTaskMap[player] = ConfirmTask(Type.UNSET, "", 0)
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable { confirmTaskMap.remove(player) }, 20L * 30)
        }
    }

    fun sendInfo(player: Player) {
        GlobalScope.launch {
            val ad = adRepo.getAdvertisement(player.uniqueId)

            player.sendMessage("§7***************§d 広告情報§7 ***************")
            player.sendMessage(" 受信頻度: ${playerMap[player]!!.displayName}")
            player.sendMessage(" 登録広告: ${ad?.content ?: "§8なし"}")
            if (ad != null) {
                player.sendMessage(" 配信期限: §c${ad.expiredDate.formatYearMonthDayHm()}§7まで")
            }
        }
    }

    fun sendList(sender: CommandSender) {
        sender.sendMessage("§7***************§d 広告一覧§7 ***************")
        advertisementList.forEachIndexed { index, ad ->
            sender.sendMessage("${index + 1}. ${ad.content} §7(${ad.owner.name})")
        }
    }

    fun preview(sender: CommandSender, contents: List<String>) {
        val content: String = ChatColor.translateAlternateColorCodes('&', contents.joinToString(separator = " "))
        sender.sendMessage(PREFIX + content)
    }

    fun setAdvertiseFrequency(player: Player, strFreq: String) {
        val freq = try {
            AdvertiseFrequency.valueOf(strFreq.toUpperCase())
        } catch (e: Exception) {
            player.sendMessage("${PREFIX}§e${strFreq}は存在しません.")
            return
        }

        GlobalScope.launch {
            if (playerRepo.updateAdvertiseFrequency(player.uniqueId, freq)) {
                playerMap[player] = freq
                player.sendMessage("${PREFIX}§a受信頻度を${freq.displayName}§aに設定しました.")
            } else {
                player.sendMessage("${PREFIX}§e設定に失敗しました.")
            }
        }
    }

    fun confirmTask(player: Player) {
        confirmTaskMap.remove(player)?.run(player) ?: let {
            player.sendMessage("${PREFIX}§e認証が必要な処理はありません.")
            return
        }
    }

    fun sync(sender: CommandSender) {
        syncLatestAds()
        sender.sendMessage("${PREFIX}§a最新のデータベースと同期しました.")
    }

    private fun startBroadcastAsyncTask() {
        var indexHigh = 0
        var indexMiddle = 0
        var indexLow = 0

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            val now = System.currentTimeMillis()
            advertisementList.removeIf { it.expiredDate.time <= now }
            if (advertisementList.isEmpty()) {
                return@Runnable
            }

            if (advertisementList.size <= ++indexHigh) {
                indexHigh = 0
            }
            broadcast(AdvertiseFrequency.HIGH, indexHigh)
        }, BASE_INTERVAL_TICK, BASE_INTERVAL_TICK * AdvertiseFrequency.HIGH.intervalGain)

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            if (advertisementList.isEmpty()) {
                return@Runnable
            }

            if (advertisementList.size <= ++indexMiddle) {
                indexMiddle = 0
            }
            broadcast(AdvertiseFrequency.MIDDLE, indexMiddle)
        }, BASE_INTERVAL_TICK, BASE_INTERVAL_TICK * AdvertiseFrequency.MIDDLE.intervalGain)

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            if (advertisementList.isEmpty()) {
                return@Runnable
            }

            if (advertisementList.size <= ++indexLow) {
                indexLow = 0
            }
            broadcast(AdvertiseFrequency.LOW, indexLow)
        }, BASE_INTERVAL_TICK, BASE_INTERVAL_TICK * AdvertiseFrequency.LOW.intervalGain)
    }

    private fun broadcast(freq: AdvertiseFrequency, index: Int) {
        val ad = advertisementList[index]
        val message = "$PREFIX${ad.content}"

        playerMap.filterValues { it == freq }
            .forEach { (player, _) -> player.sendMessage(message) }
    }

    private fun onPlayerJoined(player: Player) {
        GlobalScope.launch { playerMap[player] = playerRepo.getAdvertiseFrequency(player.uniqueId) }
    }

    private fun onPlayerQuited(player: Player) {
        playerMap.remove(player)
    }

    private inner class ConfirmTask(
        val type: Type,
        val content: String,
        val periodDays: Int
    ) {

        fun run(player: Player) {
            GlobalScope.launch {
                when (type) {
                    Type.SET -> runSetTask(player)
                    Type.UNSET -> runUnsetTask(player)
                }
            }
        }

        private fun runSetTask(player: Player) {
            val price = periodDays * PRICE_PER_DAY
            val balance = plugin.economy.getBalance(player)

            if (balance < price) {
                player.sendMessage("${PREFIX}§e所持金が \u00A5${price - balance} 不足しています.")
                return
            }

            if (!plugin.economy.withdrawPlayer(player, price.toDouble()).transactionSuccess()) {
                player.sendMessage("${PREFIX}§e購入処理に失敗しました.")
                return
            }

            val expiredDate = Date(System.currentTimeMillis() + (periodDays.dayToMills()))

            if (!adRepo.addAdvertisement(player.uniqueId, content, expiredDate)) {
                player.sendDatabaseUpdateError()

                plugin.economy.depositPlayer(player, price.toDouble())
                return
            }

            player.sendMessage("${PREFIX}§a登録しました.")

            syncLatestAds()
        }

        private fun runUnsetTask(player: Player) {
            if (!adRepo.deleteAdvertisement(player.uniqueId)) {
                player.sendDatabaseUpdateError()
                return
            }

            player.sendMessage("${PREFIX}§a消去しました.")

            syncLatestAds()
        }
    }
}
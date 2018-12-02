package jp.kentan.minecraft.nekocore.manager

import com.sk89q.worldguard.bukkit.RegionContainer
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.data.ZoneRepository
import jp.kentan.minecraft.nekocore.data.model.Area
import jp.kentan.minecraft.nekocore.data.model.Zone
import jp.kentan.minecraft.nekocore.event.ZoneEvent
import jp.kentan.minecraft.nekocore.util.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.SignChangeEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level

class ZoneManager(
    private val plugin: NekoCorePlugin
) : ZoneEvent {

    private enum class TradeType { BUY, RENTAL, SELL }

    companion object {
        private const val PREFIX = "§7[§9区画§7]§r "
        private const val CONFIRM_DELAY_TICK = 20L * 60
        private const val EXPIRE_CHECK_INTERVAL_TICK = 20L * 60 * 5
    }

    private val zoneRepo = ZoneRepository(plugin)
    private val regionContainer = plugin.worldGuard.regionContainer
    private val tradeTaskMap: MutableMap<Player, TradeTask> = ConcurrentHashMap()

    init {
        scheduleExpiredAreaCheckTask()

        plugin.bukkitEventListener.zoneEvent = this
    }

    fun getAreaNameList(world: String?) = zoneRepo.getAreaNameList(world)

    fun registerArea(player: Player, name: String, zoneId: String, regionId: String, strRegionSize: String) {
        val world = player.world.name

        GlobalScope.launch {
            if (zoneRepo.existArea(world, name)) {
                player.sendWarnMessage("${name}はすでに登録されています.")
                return@launch
            }

            val regionSize = strRegionSize.toIntOrNull() ?: 0
            if (regionSize < 1) {
                player.sendWarnMessage("${strRegionSize}は1以上の整数ではありません.")
                return@launch
            }

            if (zoneRepo.addArea(name, world, zoneId, regionId, regionSize)) {
                player.sendSuccessMessage("${name}を登録しました.")
            } else {
                player.sendDatabaseUpdateError()
            }
        }
    }

    fun removeArea(player: Player, name: String) {
        val world = player.world.name

        GlobalScope.launch {
            if (!zoneRepo.existArea(world, name)) {
                player.sendUnknownArea(name)
                return@launch
            }

            if (zoneRepo.removeArea(name, world)) {
                player.sendSuccessMessage("${name}を消去しました.")
            } else {
                player.sendDatabaseUpdateError()
            }
        }
    }

    fun setAreaLock(player: Player, name: String, isLock: Boolean) {
        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, name) ?: return@launch

            val state = when {
                isLock -> Area.State.LOCK
                area.owner != null -> Area.State.SOLD
                else -> Area.State.ON_SALE
            }

            val updateArea = area.copy(state = state)

            if (!zoneRepo.updateArea(updateArea)) {
                player.sendDatabaseUpdateError()
                return@launch
            }

            player.sendMessage("$PREFIX§a${name}の区画状態を${state.displayName}§aに更新しました.")
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) { updateArea.updateSign() }
        }
    }

    fun takeArea(player: Player, name: String) {
        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, name) ?: return@launch
            if (area.owner == null) {
                player.sendWarnMessage("${name}に所有者がいません.")
                return@launch
            }

            val updateArea =
                area.copy(owner = null, state = Area.State.ON_SALE, purchasedPrice = 0.0, expiredDate = null)

            if (!zoneRepo.updateArea(updateArea)) {
                player.sendDatabaseUpdateError()
                return@launch
            }

            player.sendMessage("$PREFIX§a${name}の区画を${area.owner.name}から取り上げました.")
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) { updateArea.updateSign() }
        }
    }

    fun registerBuyTask(player: Player, name: String) {
        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, name) ?: return@launch
            val zone = zoneRepo.getZoneOrError(player, area.zoneId) ?: return@launch
            if (zone.type != Zone.Type.BUY_UP) {
                player.sendWarnMessage("${Zone.Type.BUY_UP.displayName}区画ではありません.")
                return@launch
            }

            if (area.state != Area.State.ON_SALE) {
                player.sendWarnMessage("現在, この区画は購入できません.")
                return@launch
            }

            val ownedAreaCount = zoneRepo.getOwnedAreaCount(player.uniqueId, area.zoneId)
            if (ownedAreaCount >= zone.ownedLimit) {
                player.sendMessage("$PREFIX${zone.name}§eの所有数が上限に達しています.")
                return@launch
            }

            val price = zone.calcAreaPurchasePrice(area.regionSize, ownedAreaCount)
            if (price < 0.0) {
                player.sendErrorMessage("${name}の購入価格を取得できませんでした.")
                return@launch
            }

            zone.buyRentalRule?.let { player.sendMessage(it) }

            player.sendMessage("$PREFIX${zone.name}§r ${area.name}§r を §e\u00A5$price§r で購入しますか？")
            player.sendMessage("$PREFIX§7確定するには §c/zone confirm§7 と入力して下さい.")

            tradeTaskMap[player] = TradeTask(TradeType.BUY, zone, area, price)
            Bukkit.getScheduler()
                .runTaskLaterAsynchronously(plugin, { tradeTaskMap.remove(player) }, CONFIRM_DELAY_TICK)
        }
    }

    fun registerRentalTask(player: Player, name: String) {
        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, name) ?: return@launch
            val zone = zoneRepo.getZoneOrError(player, area.zoneId) ?: return@launch
            if (zone.type != Zone.Type.RENTAL) {
                player.sendWarnMessage("${Zone.Type.RENTAL.displayName}区画ではありません.")
                return@launch
            }

            val ownedAreaCount = zoneRepo.getOwnedAreaCount(player.uniqueId, area.zoneId)

            // 新規レンタル
            if (!area.isOwner(player)) {
                if (area.state != Area.State.ON_SALE) {
                    player.sendWarnMessage("現在, この区画はレンタルできません.")
                    return@launch
                }

                if (ownedAreaCount >= zone.ownedLimit) {
                    player.sendMessage("$PREFIX${zone.name}§eの所有数が上限に達しています.")
                    return@launch
                }
            } else if (area.state == Area.State.LOCK) {
                player.sendWarnMessage("この区画はロックされています.")
                return@launch
            }

            val price = zone.calcAreaPurchasePrice(area.regionSize, ownedAreaCount)
            if (price < 0.0) {
                player.sendErrorMessage("${name}のレンタル価格を取得できませんでした.")
                return@launch
            }

            zone.buyRentalRule?.let { player.sendMessage(it) }

            player.sendMessage("$PREFIX${zone.name}§r ${area.name}§r を §e\u00A5$price§r でレンタルしますか？")
            player.sendMessage("$PREFIX§7確定するには §c/zone confirm§7 と入力して下さい.")

            tradeTaskMap[player] = TradeTask(TradeType.RENTAL, zone, area, price)
            Bukkit.getScheduler()
                .runTaskLaterAsynchronously(plugin, { tradeTaskMap.remove(player) }, CONFIRM_DELAY_TICK)
        }
    }

    fun registerSellTask(player: Player, name: String) {
        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, name) ?: return@launch
            if (area.state == Area.State.LOCK) {
                player.sendWarnMessage("この区画はロックされています.")
                return@launch
            }
            if (!area.isOwner(player)) {
                player.sendWarnMessage("あなたは, この区画の所有者ではありません.")
                return@launch
            }

            val zone = zoneRepo.getZoneOrError(player, area.zoneId) ?: return@launch

            val price = zone.calcAreaSellPrice(area.purchasedPrice)
            if (price < 0.0) {
                player.sendErrorMessage("${name}の売却価格を取得できませんでした.")
                return@launch
            }

            zone.sellRule?.let { player.sendMessage(it) }

            player.sendMessage("$PREFIX${zone.name}§r ${area.name}§r を §e\u00A5$price§r で売却しますか？")
            player.sendMessage("$PREFIX§7確定するには §c/zone confirm§7 と入力して下さい.")

            tradeTaskMap[player] = TradeTask(TradeType.SELL, zone, area, price)
            Bukkit.getScheduler()
                .runTaskLaterAsynchronously(plugin, { tradeTaskMap.remove(player) }, CONFIRM_DELAY_TICK)
        }
    }

    fun confirmTradeTask(player: Player) {
        tradeTaskMap[player]?.run(player) ?: let {
            player.sendWarnMessage("認証が必要な処理はありません.")
            return
        }
    }

    fun sendAreaInfo(player: Player, name: String) {
        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, name) ?: return@launch
            val zone = zoneRepo.getZoneOrError(player, area.zoneId) ?: return@launch

            player.sendMessage("§7***************§9 区画情報§7 ***************")
            player.sendMessage(" 区画: ${zone.name}")
            player.sendMessage(" タイプ: ${zone.type.displayName}")
            player.sendMessage(" エリア: ${area.name}")
            player.sendMessage(" 保護ID: ${area.regionId}")

            if (area.isOwner(player)) {
                player.sendMessage(" 売却価格: §e\u00A5${zone.calcAreaSellPrice(area.purchasedPrice)}")
            } else {
                val ownedCount = zoneRepo.getOwnedAreaCount(player.uniqueId, zone.id)
                val price = zone.calcAreaPurchasePrice(area.regionSize, ownedCount)

                when (zone.type) {
                    Zone.Type.BUY_UP -> player.sendMessage(" 販売価格: §e\u00A5$price")
                    Zone.Type.RENTAL -> player.sendMessage(" レンタル価格: §e\u00A5$price")
                }
            }

            player.sendMessage(" 所有者: §8${area.owner?.name ?: "-"}")
            player.sendMessage(" 状態: ${area.state.displayName}")

            area.expiredDate?.let { date ->
                player.sendMessage(" レンタル期限: §c${date.formatYearMonthDayHm()}")
            }

            if (area.state == Area.State.LOCK) {
                return@launch
            }

            if (area.isOwner(player)) {
                player.sendMessage("§7売却コマンド §r/zone sell $name")

                if (zone.type === Zone.Type.RENTAL) {
                    player.sendMessage("§7レンタル延長コマンド §r/zone rental $name")
                }
            } else if (area.state == Area.State.ON_SALE) {
                when (zone.type) {
                    Zone.Type.BUY_UP -> player.sendMessage("§7購入コマンド §r/zone buy $name")
                    Zone.Type.RENTAL -> player.sendMessage("§7レンタルコマンド §r/zone rental $name")
                }
            }
        }
    }

    fun sendOwnerLimits(player: Player) {
        GlobalScope.launch {
            player.sendMessage("§7********** §6区画上限§7 **********")
            zoneRepo.getZoneList().forEach { zone ->
                val count = zoneRepo.getOwnedAreaCount(player.uniqueId, zone.id)
                player.sendMessage("§7- §r${zone.name}§r: §e$count/${zone.ownedLimit}")
            }
        }
    }

    fun sendOwnerAreaList(player: Player) {
        GlobalScope.launch {
            val areaMap = zoneRepo.getOwnedAreaMap(player.uniqueId)
            if (areaMap.isEmpty()) {
                player.sendWarnMessage("区画を所有していません.")
                return@launch
            }

            player.sendMessage("§7********** §6所有区画一覧§7 **********")
            areaMap.forEach { zone, areaList ->
                player.sendMessage("§7- §r${zone}§r: §e${areaList.joinToString()}")
            }
        }
    }

    fun sendZoneRule(player: Player, name: String) {
        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, name) ?: return@launch
            val zone = zoneRepo.getZoneOrError(player, area.zoneId) ?: return@launch

            zone.buyRentalRule?.let { player.sendMessage(it) }
            zone.sellRule?.let { player.sendMessage(it) }
        }
    }

    override fun onPlayerJoin(player: Player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, {
            if (!player.isOnline) {
                return@runTaskLaterAsynchronously
            }

            val expireList = zoneRepo.getOwnedRentalExpireAreaList(player.uniqueId, 7)
            if (expireList.isNotEmpty()) {
                player.sendMessage("$PREFIX§eレンタル中の区画の期限が近づいています.")
                expireList.forEach(player::sendMessage)
                player.sendMessage("$PREFIX§7/zone rental コマンドで更新して下さい.")
            }
        }, 20L * 10)
    }

    override fun onSignPlace(event: SignChangeEvent) {
        val player = event.player
        val name = event.getLine(1)
        if (name.isNullOrBlank()) {
            player.sendWarnMessage("区画名を入力してください.")
            return
        }

        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, name) ?: return@launch

            if (area.signLocation != null) {
                player.sendWarnMessage("この看板は既に${area.signLocation}に設置されています.")
                return@launch
            }

            val updateArea = area.copy(signLocation = event.block.location)
            if (!zoneRepo.updateArea(updateArea)) {
                player.sendDatabaseUpdateError()
                return@launch
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) { updateArea.updateSign() }
        }
    }

    override fun onSignBreak(player: Player, sign: Sign) {
        GlobalScope.launch {
            val area = zoneRepo.getAreaOrError(player, sign.getLine(1)) ?: return@launch

            val updateArea = area.copy(signLocation = null)
            if (!zoneRepo.updateArea(updateArea)) {
                player.sendDatabaseUpdateError()
            }
        }
    }

    override fun onSignClick(player: Player, sign: Sign) {
        sendAreaInfo(player, sign.getLine(1))
    }

    private fun scheduleExpiredAreaCheckTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, {
            zoneRepo.getExpiredAreaList().forEach { area ->
                val region = regionContainer.get(area.world)?.getRegion(area.regionId) ?: let {
                    plugin.logger.warning("failed to get a region(${area.regionId}")
                    return@forEach
                }

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) {
                    try {
                        region.clean(area.world)
                    } catch (e: Exception) {
                        plugin.logger.log(Level.SEVERE, "failed to clean a region(${area.regionId}).")
                        return@scheduleSyncDelayedTask
                    }

                    GlobalScope.launch {
                        val updateArea =
                            area.copy(
                                state = Area.State.ON_SALE,
                                owner = null,
                                purchasedPrice = 0.0,
                                expiredDate = null
                            )

                        zoneRepo.updateArea(updateArea)
                        region.setMember(null)
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) { updateArea.updateSign() }
                    }
                }
            }
        }, EXPIRE_CHECK_INTERVAL_TICK, EXPIRE_CHECK_INTERVAL_TICK)
    }

    private fun ZoneRepository.getAreaOrError(player: Player, name: String): Area? =
        zoneRepo.getArea(player.world.name, name) ?: run {
            player.sendUnknownArea(name)
            return@run null
        }

    private fun ZoneRepository.getAreaOrError(player: Player, id: Int): Area? =
        zoneRepo.getArea(id) ?: run {
            player.sendErrorMessage("エリアIDが不正です.")
            return@run null
        }

    private fun ZoneRepository.getZoneOrError(player: Player, zoneId: String): Zone? =
        zoneRepo.getZone(zoneId) ?: run {
            player.sendErrorMessage("区画IDが不正です.")
            return@run null
        }

    private fun RegionContainer.getRegionOrError(player: Player, regionId: String): ProtectedRegion? =
        get(player.world)?.getRegion(regionId) ?: let {
            player.sendErrorMessage("保護リージョン($regionId)は存在しません.")
            return@let null
        }

    private fun CommandSender.sendUnknownArea(name: String) {
        sendMessage("$PREFIX§e${name}は存在しません.")
        sendMessage("$PREFIX§7区画名が正しいか確認してください.")
    }

    private fun CommandSender.sendSuccessMessage(message: String) {
        sendMessage("$PREFIX§a$message")
    }

    private fun CommandSender.sendWarnMessage(message: String) {
        sendMessage("$PREFIX§e$message")
    }

    private fun CommandSender.sendErrorMessage(message: String) {
        sendMessage("$PREFIX§c$message")
    }

    private inner class TradeTask(
        val type: TradeType,
        val zone: Zone,
        val area: Area,
        val price: Double
    ) {
        fun run(player: Player) {
            tradeTaskMap.remove(player)

            GlobalScope.launch {
                val area = zoneRepo.getAreaOrError(player, area.id) ?: return@launch
                val zone = zoneRepo.getZoneOrError(player, zone.id) ?: return@launch
                val region = regionContainer.getRegionOrError(player, area.regionId) ?: return@launch

                if (this@TradeTask.zone != zone || this@TradeTask.area != area) {
                    player.sendWarnMessage("区画が更新されています. 再試行して下さい.")
                    return@launch
                }

                when (type) {
                    TradeType.BUY -> runBuyTask(player, region)
                    TradeType.RENTAL -> runRentalTask(player, region)
                    TradeType.SELL -> runSellTask(player, region)
                }
            }
        }

        private fun runBuyTask(player: Player, region: ProtectedRegion) {
            val balance = plugin.economy.getBalance(player)
            if (balance < price) {
                player.sendWarnMessage("所持金が \u00A5${price - balance} 不足しています.")
                return
            }

            if (price < 0.0 || !plugin.economy.withdrawPlayer(player, price).transactionSuccess()) {
                player.sendErrorMessage("購入処理に失敗しました.")
                return
            }

            regionContainer.get(area.world)?.getRegion(area.regionId)

            val area = area.copy(state = Area.State.SOLD, owner = player, purchasedPrice = price, expiredDate = null)

            if (!zoneRepo.updateArea(area)) {
                plugin.economy.depositPlayer(player, price)
                player.sendDatabaseUpdateError()
                return
            }

            region.setMember(player)

            player.sendMessage("$PREFIX${zone.name}§r ${area.name}§r を §e\u00A5$price§r で購入しました！")
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) { area.updateSign() }
        }

        private fun runRentalTask(player: Player, region: ProtectedRegion) {
            val oldExpiredTime = if (area.expiredDate == null) System.currentTimeMillis() else area.expiredDate.time
            val expiredDate = Date(oldExpiredTime + (zone.rentalDays.dayToMills()))

            if ((expiredDate.time - System.currentTimeMillis()).millsToDay() > 180.0) {
                player.sendWarnMessage("180日を超えるレンタルは行なえません.")
                return
            }

            val balance = plugin.economy.getBalance(player)
            if (balance < price) {
                player.sendWarnMessage("所持金が \u00A5${price - balance} 不足しています.")
                return
            }

            if (price < 0.0 || !plugin.economy.withdrawPlayer(player, price).transactionSuccess()) {
                player.sendErrorMessage("購入処理に失敗しました.")
                return
            }

            val area =
                area.copy(state = Area.State.SOLD, owner = player, purchasedPrice = price, expiredDate = expiredDate)

            if (!zoneRepo.updateArea(area)) {
                plugin.economy.depositPlayer(player, price)
                player.sendDatabaseUpdateError()
                return
            }

            region.setMember(player)

            player.sendMessage("$PREFIX${zone.name}§r ${area.name}§r を §e\u00A5$price§r でレンタルしました！")
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) { area.updateSign() }
        }

        private fun runSellTask(player: Player, region: ProtectedRegion) {
            if (region.containBlock(area.world)) {
                player.sendWarnMessage("区画内にブロックがあります.")
                return
            }

            val balance = plugin.economy.getBalance(player)
            if (balance < price) {
                player.sendWarnMessage("所持金が \u00A5${price - balance} 不足しています.")
                return
            }

            if (price < 0.0 || !plugin.economy.depositPlayer(player, price).transactionSuccess()) {
                player.sendErrorMessage("入金処理に失敗しました.")
                return
            }

            val area = area.copy(state = Area.State.ON_SALE, owner = null, purchasedPrice = 0.0, expiredDate = null)

            if (!zoneRepo.updateArea(area)) {
                plugin.economy.withdrawPlayer(player, price)
                player.sendDatabaseUpdateError()
                return
            }

            region.setMember(null)

            player.sendMessage("$PREFIX${zone.name}§r ${area.name}§r を §e\u00A5$price§r で売却しました！")
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin) { area.updateSign() }
        }
    }
}
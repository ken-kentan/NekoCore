package jp.kentan.minecraft.nekocore.data.model

import org.bukkit.World
import kotlin.math.max
import kotlin.math.min

data class Zone(
    val id: String,
    val name: String,
    val type: Type,
    val world: World,
    val ownedLimit: Int,
    val rentalDays: Int,
    val priceRate: Double,
    val priceRateGain: Double,
    val sellGain: Double,
    val buyRentalRule: String?,
    val sellRule: String?
) {
    enum class Type(
        val displayName: String
    ) {
        BUY_UP("買い切り"),
        RENTAL("レンタル");
    }

    fun calcAreaPurchasePrice(size: Int, ownedAreaCount: Int, isOwned: Boolean = false): Double {
        val ownedCount = if (isOwned) max(0, ownedAreaCount - 1) else ownedAreaCount
        val count = min(ownedCount, ownedLimit - 1)
        val rate = if (count > 0) priceRate * max(1.0, priceRateGain * count) else priceRate
        return rate * size
    }

    fun calcAreaSellPrice(purchasedPrice: Double): Double = sellGain * purchasedPrice
}
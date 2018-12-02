package jp.kentan.minecraft.nekocore.data.model

import org.bukkit.World
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

    fun calcAreaPurchasePrice(size: Int, ownedAreaCount: Int): Double {
        val count = min(ownedAreaCount, ownedLimit - 1)
        val rate = if (count > 0) priceRate * priceRateGain * count else priceRate
        return rate * size
    }

    fun calcAreaSellPrice(purchasedPrice: Double): Double = sellGain * purchasedPrice
}
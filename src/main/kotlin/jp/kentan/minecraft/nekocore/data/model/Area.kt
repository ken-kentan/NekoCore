package jp.kentan.minecraft.nekocore.data.model

import jp.kentan.minecraft.nekocore.util.formatYearMonthDayHm
import org.bukkit.Location
import org.bukkit.OfflinePlayer
import org.bukkit.World
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import java.util.*

data class Area(
    val id: Int,
    val name: String,
    val world: World,
    val zoneId: String,
    val regionId: String,
    val regionSize: Int,
    val state: State,
    val signLocation: Location?,
    val owner: OfflinePlayer?,
    val purchasedPrice: Double, // ownerが購入・レンタルに支払った金額
    val expiredDate: Date?
) {

    companion object {
        const val SIGN_INDEX_TEXT = "§8§l[§9§l区画§8§l]"
    }

    enum class State(
        val displayName: String
    ) {
        ON_SALE("§c販売中"),
        SOLD("§6契約済み"),
        LOCK("§8ロック中");
    }

    fun isOwner(player: Player) = owner != null && owner.uniqueId == player.uniqueId

    fun updateSign(): Boolean {
        if (signLocation == null) {
            return true
        }

        val sign = signLocation.block.state as? Sign ?: return false

        sign.setLine(0, SIGN_INDEX_TEXT)
        sign.setLine(1, name)
        if (owner != null) {
            sign.setLine(2, "§8${owner.name}")
            sign.setLine(
                3,
                if (expiredDate == null) state.displayName else "§5${expiredDate.formatYearMonthDayHm()}§r まで"
            )
        } else {
            sign.setLine(2, "")
            sign.setLine(3, state.displayName)
        }

        return sign.update()
    }
}
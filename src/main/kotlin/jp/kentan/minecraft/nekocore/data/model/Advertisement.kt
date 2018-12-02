package jp.kentan.minecraft.nekocore.data.model

import org.bukkit.OfflinePlayer
import java.util.*

data class Advertisement(
    val owner: OfflinePlayer,
    val content: String,
    val expiredDate: Date
)
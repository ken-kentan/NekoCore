package jp.kentan.minecraft.nekocore.data.model

import com.squareup.moshi.Json
import java.util.*

data class MojangUser(
    @field:Json(name = "id") val uniqueId: UUID,
    @field:Json(name = "name") val name: String
)
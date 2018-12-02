package jp.kentan.minecraft.nekocore.data.adapter

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.util.*

class UuidJsonAdapter {

    private val regex = Regex("(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)")

    @ToJson
    fun toJson(uuid: UUID): String = uuid.toString()

    @FromJson
    fun fromJson(uuid: String): UUID = UUID.fromString(uuid.replaceFirst(regex, "$1-$2-$3-$4-$5"))
}
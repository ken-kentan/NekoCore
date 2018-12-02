package jp.kentan.minecraft.nekocore.data.model

enum class AdvertiseFrequency(
    val intervalGain: Int,
    val displayName: String
) {
    OFF(0, "§7停止"),
    LOW(6, "§6低"),
    MIDDLE(3, "§b中"),
    HIGH(1, "§9高");
}
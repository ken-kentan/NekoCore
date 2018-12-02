package jp.kentan.minecraft.nekocore.util

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*

fun CommandSender.sendSuccessMessage(message: String) {
    sendMessage("${NekoCorePlugin.PREFIX}§a$message")
}

fun CommandSender.sendWarnMessage(message: String) {
    sendMessage("${NekoCorePlugin.PREFIX}§e$message")
}

fun CommandSender.sendErrorMessage(message: String) {
    sendMessage("${NekoCorePlugin.PREFIX}§c$message")
}

fun CommandSender.sendDatabaseUpdateError() {
    sendMessage("${NekoCorePlugin.PREFIX}§cデータベースの更新に失敗しました. 運営に報告してください.")
}

fun Player?.broadcastMessageWithoutMe(message: String) {
    if (this == null) {
        Bukkit.broadcastMessage(message)
        return
    }

    Bukkit.getOnlinePlayers().forEach { p ->
        if (this != p) {
            p.sendMessage(message)
        }
    }
}

private val YMD_HM_FORMAT = SimpleDateFormat("yyyy/MM/dd HH:mm")

fun Date.formatYearMonthDayHm(): String = YMD_HM_FORMAT.format(this)

fun Int.dayToMills(): Long = this * 86400000L
fun Long.millsToDay(): Double = this / 86400000.0
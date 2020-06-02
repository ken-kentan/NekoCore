package jp.kentan.minecraft.nekocore.data.dao

import org.bukkit.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.*

abstract class BaseDao {

    protected fun ResultSet.getChatColoredString(index: Int): String =
        ChatColor.translateAlternateColorCodes('&', getString(index))

    protected fun ResultSet.getChatColoredStringOrNull(index: Int): String? {
        return ChatColor.translateAlternateColorCodes('&', getString(index) ?: return null)
    }

    protected fun ResultSet.getOfflinePlayer(index: Int): OfflinePlayer =
        Bukkit.getOfflinePlayer(UUID.fromString(getString(index)))

    protected fun ResultSet.getOfflinePlayerOrNull(index: Int): OfflinePlayer? {
        return Bukkit.getOfflinePlayer(UUID.fromString(getString(index) ?: return null))
    }

    protected fun ResultSet.getWorldOrNull(index: Int): World? = Bukkit.getWorld(getString(index))

    protected fun ResultSet.getLocation(index: Int): Location? {
        val spited = getString(index)?.split(',') ?: return null
        if (spited.size < 4) {
            return null
        }

        return Location(
            Bukkit.getWorld(spited[0]),
            spited[1].toDouble(),
            spited[2].toDouble(),
            spited[3].toDouble()
        )
    }

    protected fun PreparedStatement.setLocation(index: Int, location: Location?) {
        if (location != null) {
            setString(index, "${location.world?.name},${location.x},${location.y},${location.z}")
        } else {
            setString(index, null)
        }
    }
}
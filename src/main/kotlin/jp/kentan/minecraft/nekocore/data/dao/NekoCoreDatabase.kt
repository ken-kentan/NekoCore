package jp.kentan.minecraft.nekocore.data.dao

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.config.ConfigKeys
import org.mariadb.jdbc.MariaDbPoolDataSource
import java.sql.Connection

class NekoCoreDatabase(plugin: NekoCorePlugin) {

    private val mariaDbPoolDataSource = MariaDbPoolDataSource(plugin.configuration.get(ConfigKeys.DATABASE_URL))

    val connection: Connection
        get() = mariaDbPoolDataSource.connection

    init {
        mariaDbPoolDataSource.apply {
            user = plugin.configuration.get(ConfigKeys.DATABASE_USERNAME)
            setPassword(plugin.configuration.get(ConfigKeys.DATABASE_PASSWORD))
        }
    }

    fun shutdown() {
        mariaDbPoolDataSource.close()
    }
}
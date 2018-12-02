package jp.kentan.minecraft.nekocore.config

import org.bukkit.configuration.ConfigurationSection

interface ConfigKey<T> {
    val ordinal: Int
    fun get(config: ConfigurationSection): T
}
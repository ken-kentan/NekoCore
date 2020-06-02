package jp.kentan.minecraft.nekocore.config

import jp.kentan.minecraft.nekocore.NekoCorePlugin

class NekoCoreConfiguration(
    private val plugin: NekoCorePlugin
) {

    private val handlerList = mutableListOf<() -> Unit>()
    private var values = arrayOfNulls<Any>(ConfigKeys.size())

    init {
        plugin.saveDefaultConfig()
        load()
    }

    fun subscribe(handler: () -> Unit) {
        handlerList.add(handler)
    }

    private fun load() {
        val values = values

        val config = plugin.config

        ConfigKeys.KEYS.forEach { key ->
            values[key.ordinal] = key.get(config)
        }

        this.values = values
    }

    fun reload() {
        plugin.reloadConfig()
        load()

        handlerList.forEach { it.invoke() }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: ConfigKey<T>) = values[key.ordinal] as T
}
package jp.kentan.minecraft.nekocore.config

import jp.kentan.minecraft.nekocore.NekoCorePlugin

class NekoCoreConfiguration(
    private val plugin: NekoCorePlugin
) {

    private var values: Array<Any>? = null
    private val handlerList = mutableListOf<() -> Unit>()

    init {
        plugin.saveDefaultConfig()
        load()
    }

    fun subscribe(handler: () -> Unit) {
        handlerList.add(handler)
    }

    private fun load() {
        val values = values ?: Array(ConfigKeys.size()) { Any() }

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
    fun <T> get(key: ConfigKey<T>) = values?.get(key.ordinal) as T
}
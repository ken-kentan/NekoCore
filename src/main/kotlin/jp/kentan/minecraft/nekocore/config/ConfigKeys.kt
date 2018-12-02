package jp.kentan.minecraft.nekocore.config

import jp.kentan.minecraft.nekocore.data.model.VoteReward
import org.bukkit.configuration.ConfigurationSection
import java.util.concurrent.atomic.AtomicInteger

class ConfigKeys {
    companion object {
        private val ORDINAL_COUNTER = AtomicInteger(0)

        val TUTORIAL_KEYWORD = stringKey("tutorial.keyword", "keyword")

        val VOTE_REWARD_LIST: ConfigKey<List<VoteReward>> = customKey { config ->
            val rewardList = mutableListOf<VoteReward>()

            for (day in 1..10) {
                val path = "vote.reward.$day"
                if (!config.isConfigurationSection(path)) {
                    break
                }

                rewardList.add(
                    VoteReward(
                        config.getString("$path.name"),
                        config.getStringList("$path.commands")
                    )
                )
            }

            return@customKey rewardList
        }

        val DATABASE_URL = stringKey("database.url", "localhost")
        val DATABASE_USERNAME = stringKey("database.username", "root")
        val DATABASE_PASSWORD = stringKey("database.password")

        val KEYS = listOf(
            TUTORIAL_KEYWORD,
            VOTE_REWARD_LIST,
            DATABASE_URL,
            DATABASE_USERNAME,
            DATABASE_PASSWORD
        )

        fun size() = ORDINAL_COUNTER.get()

        private fun stringKey(path: String, def: String? = null) =
            object : ConfigKey<String> {
                override val ordinal: Int = ORDINAL_COUNTER.getAndIncrement()

                override fun get(config: ConfigurationSection): String {
                    return config.getString(path, def)
                }
            }

        private fun <T> customKey(transform: (ConfigurationSection) -> T) =
            object : ConfigKey<T> {
                override val ordinal: Int = ORDINAL_COUNTER.getAndIncrement()

                override fun get(config: ConfigurationSection) = transform(config)
            }
    }
}
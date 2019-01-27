package jp.kentan.minecraft.nekocore.listener

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import jp.kentan.minecraft.nekocore.util.JaroWinklerDistance
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.concurrent.ConcurrentLinkedQueue

class AntiSpamChatListener(plugin: NekoCorePlugin) : Listener {

    private companion object {
        const val SIMILARITY_THRESHOLD = 0.9
        const val MAX_CHAT_QUEUE_SIZE = 10
        const val IGNORE_MESSAGE_LENGTH = 20

        val JARO_WINKLER_DISTANCE = JaroWinklerDistance()
    }

    private val logger = plugin.logger
    private val chatQueue = ConcurrentLinkedQueue<String>()

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        val message = event.message
        if (message.length < IGNORE_MESSAGE_LENGTH) {
            return
        }

        if (chatQueue.any { JARO_WINKLER_DISTANCE.getDistance(it, message) > SIMILARITY_THRESHOLD }) {
            event.isCancelled = true
            logger.info("Spam detected: $message")
        }

        chatQueue.offer(event.message)
        if (chatQueue.size > MAX_CHAT_QUEUE_SIZE) {
            chatQueue.poll()
        }
    }
}
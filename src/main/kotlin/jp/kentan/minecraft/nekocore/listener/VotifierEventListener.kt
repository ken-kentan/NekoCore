package jp.kentan.minecraft.nekocore.listener

import com.vexsoftware.votifier.model.VotifierEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class VotifierEventListener : Listener {

    private var handler: ((String) -> Unit)? = null

    fun subscribe(handler: (String) -> Unit) {
        this.handler = handler
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onVotifierEvent(event: VotifierEvent) {
        handler?.invoke(event.vote.username)
    }
}
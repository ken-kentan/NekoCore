package jp.kentan.minecraft.neko_core.listener;

import com.vexsoftware.votifier.model.VotifierEvent;
import jp.kentan.minecraft.neko_core.manager.ServerVoteManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ServerVoteListener implements Listener {

    private ServerVoteManager MANAGER;

    public ServerVoteListener(ServerVoteManager manager) {
        MANAGER = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVotifierEvent(VotifierEvent event) {
        MANAGER.vote(event.getVote().getUsername());
    }
}

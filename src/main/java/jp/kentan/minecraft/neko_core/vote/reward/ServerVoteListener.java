package jp.kentan.minecraft.neko_core.vote.reward;

import com.vexsoftware.votifier.model.VotifierEvent;
import jp.kentan.minecraft.neko_core.vote.reward.RewardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ServerVoteListener implements Listener {

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVotifierEvent(VotifierEvent event) {
        RewardManager.vote(event.getVote().getUsername());
    }

}

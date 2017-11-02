package jp.kentan.minecraft.neko_core.vote;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ServerVoteListener implements Listener {

    private RewardManager mReward;

    public ServerVoteListener(RewardManager rewardManager){
        mReward = rewardManager;
    }

    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onVotifierEvent(VotifierEvent event) {
        mReward.vote(event.getVote().getUsername());
    }
}

package jp.kentan.minecraft.neko_core.listener;

import jp.kentan.minecraft.neko_core.utils.RankUtils;
import me.lucko.luckperms.api.event.LuckPermsEvent;
import me.lucko.luckperms.api.event.user.UserDataRecalculateEvent;
import org.bukkit.Bukkit;

public class LuckPermsEventListener {
    public void onUserDataRecalculate(UserDataRecalculateEvent event) {
        RankUtils.update(Bukkit.getPlayer(event.getUser().getUuid()));
    }
}

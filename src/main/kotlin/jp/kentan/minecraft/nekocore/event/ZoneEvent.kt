package jp.kentan.minecraft.nekocore.event

import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.SignChangeEvent

interface ZoneEvent {
    fun onPlayerJoin(player: Player)

    fun onSignPlace(event: SignChangeEvent)

    fun onSignBreak(player: Player, sign: Sign)

    fun onSignClick(player: Player, sign: Sign)
}
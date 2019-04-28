package jp.kentan.minecraft.nekocore.manager

import jp.kentan.minecraft.nekocore.NekoCorePlugin
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.TextComponent
import org.apache.commons.lang.RandomStringUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList


class AntiSpamManager(
    private val plugin: NekoCorePlugin
) {

    private companion object {
        const val AUTH_KEY_LENGTH = 20
        const val AUTH_REQUEST_SEND_DELAY_TICK = 20L
    }

    private val authKeyMap: MutableMap<Player, String> = ConcurrentHashMap()
    private val authorizedPlayerList: MutableList<Player> = Collections.synchronizedList(ArrayList())

    init {
        plugin.bukkitEventListener.subscribeAsyncPlayerChatEvent(::onAsyncPlayerChat)
        plugin.bukkitEventListener.subscribePlayerJoin(::onPlayerJoin)
        plugin.bukkitEventListener.subscribePlayerQuit(::onPlayerQuit)
    }

    fun auth(player: Player, key: String) {
        if (authorizedPlayerList.contains(player)) {
            player.sendMessage("§aすでに認証しています.")
            return
        }

        val authKey = authKeyMap[player] ?: let {
            player.sendMessage("§cキーが見つかりません. 運営に報告してください.")
            return
        }

        if (authKey != key) {
            player.sendMessage("§eキーが不正です.")
            return
        }

        player.sendMessage("§a認証しました!")
        authorizedPlayerList.add(player)
        authKeyMap.remove(player)
    }

    private fun sendAuthRequestMessage(player: Player) {
        val authKey: String = authKeyMap[player] ?: let {
            val generatedKey = RandomStringUtils.randomAlphanumeric(AUTH_KEY_LENGTH)
            authKeyMap[player] = generatedKey
            return@let generatedKey
        }

        val message = TextComponent(">> ここをクリックしてチャットをつかう <<")
        message.color = ChatColor.AQUA
        message.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/auth $authKey")

        player.spigot().sendMessage(message)
    }

    private fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        if (!authorizedPlayerList.contains(event.player)) {
            event.isCancelled = true
            sendAuthRequestMessage(event.player)
        }
    }

    private fun onPlayerJoin(player: Player) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            if (player.isOnline) {
                sendAuthRequestMessage(player)
            }
        }, AUTH_REQUEST_SEND_DELAY_TICK)
    }

    private fun onPlayerQuit(player: Player) {
        authorizedPlayerList.remove(player)
        authKeyMap.remove(player)
    }
}
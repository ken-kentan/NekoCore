package jp.kentan.minecraft.nekocore

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import jp.kentan.minecraft.nekocore.command.*
import jp.kentan.minecraft.nekocore.config.NekoCoreConfiguration
import jp.kentan.minecraft.nekocore.data.dao.NekoCoreDatabase
import jp.kentan.minecraft.nekocore.data.dao.PlayerDao
import jp.kentan.minecraft.nekocore.listener.BukkitEventListener
import jp.kentan.minecraft.nekocore.listener.VotifierEventListener
import jp.kentan.minecraft.nekocore.manager.*
import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class NekoCorePlugin : JavaPlugin() {
    companion object {
        const val PREFIX = "§7[§6Neko§cCore§7] §r"
    }

    lateinit var configuration: NekoCoreConfiguration
    lateinit var database: NekoCoreDatabase

    lateinit var tutorialManager: TutorialManager
    lateinit var spawnManager: SpawnManager
    lateinit var serverVoteManager: ServerVoteManager
    lateinit var weatherVoteManager: WeatherVoteManager
    lateinit var advertisementManager: AdvertisementManager
    lateinit var zoneManager: ZoneManager
    lateinit var antiSpamManager: AntiSpamManager
    private lateinit var rankManager: RankManager

    val chat: Chat by lazy { server.servicesManager.getRegistration(Chat::class.java).provider }
    val economy: Economy by lazy { server.servicesManager.getRegistration(Economy::class.java).provider }
    val worldGuard: WorldGuardPlugin by lazy {
        Bukkit.getServer().pluginManager.getPlugin("WorldGuard") as WorldGuardPlugin
    }

    internal val bukkitEventListener = BukkitEventListener()
    internal val votifierEventListener = VotifierEventListener()

    override fun onEnable() {
        configuration = NekoCoreConfiguration(this)
        database = NekoCoreDatabase(this)

        tutorialManager = TutorialManager(this)
        spawnManager = SpawnManager(this)
        rankManager = RankManager(this)
        serverVoteManager = ServerVoteManager(this)
        weatherVoteManager = WeatherVoteManager(this)
        advertisementManager = AdvertisementManager(this)
        zoneManager = ZoneManager(this)
        antiSpamManager = AntiSpamManager(this)

        setCommand(NekoCommand(this))
        setCommand(TutorialCommand(this))
        setCommand(SpawnCommand(this))
        setCommand(SetSpawnCommand(this))
        setCommand(VoteCommand(this))
        setCommand(WeatherVoteCommand(this))
        setCommand(HatCommand(this))
        setCommand(AdvertisementCommand(this))
        setCommand(ZoneCommand(this))
        setCommand(AuthCommand(this))

        bukkitEventListener.subscribePlayerPreLogin(PlayerDao(database)::insertDefaultRecordIfNeeded)

        server.pluginManager.registerEvents(bukkitEventListener, this)
        server.pluginManager.registerEvents(votifierEventListener, this)
    }

    override fun onDisable() {
        rankManager.unregisterLuckPermsEventHandler()
        database.shutdown()
    }

    private fun setCommand(command: BaseCommand) =
        getCommand(command.name)?.apply {
            executor = command
            tabCompleter = command
        }
}
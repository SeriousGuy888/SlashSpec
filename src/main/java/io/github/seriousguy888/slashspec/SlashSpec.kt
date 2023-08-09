package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.commands.SpecCommand
import io.github.seriousguy888.slashspec.listeners.PlayerGameModeChangeListener
import io.github.seriousguy888.slashspec.listeners.PlayerMoveListener
import io.github.seriousguy888.slashspec.listeners.PlayerQuitListener
import io.github.seriousguy888.slashspec.packets.FloatingHeadManager
import io.github.seriousguy888.slashspec.yaml.ConfigReader
import io.github.seriousguy888.slashspec.yaml.PlayerPreferencesManager
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File
import java.util.*

class SlashSpec : JavaPlugin() {
    val playerPrefsManager = PlayerPreferencesManager(this, File(dataFolder, "playerprefs.yml"))
    val playerManager = PlayerManager(this)
    val floatingHeadManager = FloatingHeadManager(this)
    val tabCompletionUtil = TabCompletionUtil(this)
    val configReader = ConfigReader(this)

    override fun onEnable() {
        registerCommands()
        registerListeners()
        registerTasks()
        checkForDependencies()
    }

    private fun registerCommands() {
        getCommand("spec")?.setExecutor(SpecCommand(this))
        logger.info("Registered commands.")
    }

    private fun registerListeners() {
        val pm = server.pluginManager
        pm.registerEvents(PlayerGameModeChangeListener(this), this)
        pm.registerEvents(PlayerMoveListener(this), this)
        pm.registerEvents(PlayerQuitListener(this), this)
    }

    private fun registerTasks() {
        object : BukkitRunnable() {
            override fun run() {
                playerManager.stateManager.stateMap.forEach {
                    val player = Bukkit.getPlayer(UUID.fromString(it.key)) ?: return@forEach

                    val isInSpec = playerManager.isPlayerInSpec(player)
                    if (!isInSpec)
                        return@forEach

                    floatingHeadManager.displayHead(player)
                }
            }
        }.runTaskTimer(this, 0, 20)

        logger.info("Registered periodic tasks.")
    }

    private fun checkForDependencies() {
        if (!isProtocolLibInstalled()) {
            logger.warning(
                    "ProtocolLib is not installed on this server!" +
                            "\nProtocolLib is required for the floating heads feature." +
                            " You will need to install it from https://www.spigotmc.org/resources/protocollib.1997/" +
                            " for the floating heads feature to work." +
                            "\nSlashSpec will display fallback particles in place of the" +
                            " floating heads feature.")
        }
    }

    override fun onDisable() {
        playerManager.stateManager.savePlayerData()
        playerPrefsManager.save()

        Bukkit.getOnlinePlayers().forEach {
            floatingHeadManager.removeFloatingHead(it)
        }
    }

    fun isProtocolLibInstalled(): Boolean {
        return server.pluginManager.getPlugin("ProtocolLib") != null
    }
}
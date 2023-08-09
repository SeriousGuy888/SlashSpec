package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.commands.SpecCommand
import io.github.seriousguy888.slashspec.listeners.PlayerGameModeChangeListener
import io.github.seriousguy888.slashspec.listeners.PlayerMoveListener
import io.github.seriousguy888.slashspec.listeners.PlayerQuitListener
import io.github.seriousguy888.slashspec.packets.FloatingHeadManager
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

    override fun onEnable() {
        saveDefaultConfig()
        registerCommands()
        registerListeners()
        registerTasks()
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
                    if (playerPrefsManager.get(player).isGhostMode)
                        return@forEach

                    floatingHeadManager.displayHead(player)
                }
            }
        }.runTaskTimer(this, 0, 20)

        logger.info("Registered periodic tasks.")
    }

    override fun onDisable() {
        playerManager.stateManager.savePlayerData()
        playerPrefsManager.save()

        Bukkit.getOnlinePlayers().forEach {
            floatingHeadManager.removeFloatingHead(it)
        }
    }
}
package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.commands.SpecCommand
import io.github.seriousguy888.slashspec.listeners.PlayerMoveListener
import io.github.seriousguy888.slashspec.packets.FloatingHeadManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class SlashSpec : JavaPlugin() {
    val playerManager = PlayerManager(this)
    var floatingHeadManager = FloatingHeadManager(this)

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
        pm.registerEvents(PlayerMoveListener(this), this)
    }

    private fun registerTasks() {

        object : BukkitRunnable() {
            override fun run() {
                playerManager.stateManager.stateMap.forEach {
                    if (!it.value.isSpecGlowing)
                        return@forEach
                    val player = Bukkit.getPlayer(UUID.fromString(it.key)) ?: return@forEach

                    if (player.gameMode == GameMode.SPECTATOR) {
                        floatingHeadManager.displayHead(player)
                    }
                }
            }
        }.runTaskTimer(this, 0, 20)

        logger.info("Registered periodic tasks.")
    }

    override fun onDisable() {
        playerManager.stateManager.savePlayerData()
    }
}
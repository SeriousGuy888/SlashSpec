package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.commands.SpecCommand
import io.github.seriousguy888.slashspec.listeners.PlayerMoveListener
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class SlashSpec : JavaPlugin() {
    val playerManager = PlayerManager(this)

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
        pm.registerEvents(PlayerMoveListener(), this)
    }

    private fun registerTasks() {
        val dustOptions = Particle.DustOptions(Color.WHITE, 3f)

        object : BukkitRunnable() {
            override fun run() {
                playerManager.stateManager.stateMap.forEach {
                    if (!it.value.isSpecGlowing)
                        return@forEach
                    val player = Bukkit.getPlayer(UUID.fromString(it.key)) ?: return@forEach
                    val world = player.world

                    if (player.gameMode == GameMode.SPECTATOR) {
                        world.spawnParticle(Particle.REDSTONE, player.eyeLocation, 25, dustOptions)
                    }
                }
            }
        }.runTaskTimer(this, 0, 10)

        logger.info("Registered periodic tasks.")
    }

    override fun onDisable() {
        playerManager.stateManager.savePlayerData()
    }
}
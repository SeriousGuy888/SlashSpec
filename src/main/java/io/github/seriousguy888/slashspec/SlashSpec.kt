package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.commands.SpecCommand
import io.github.seriousguy888.slashspec.listeners.*
import io.github.seriousguy888.slashspec.packets.FloatingHeadManager
import io.github.seriousguy888.slashspec.state.CombatTimerManager
import io.github.seriousguy888.slashspec.state.PlayerManager
import io.github.seriousguy888.slashspec.state.PlayerPreferencesManager
import io.github.seriousguy888.slashspec.state.PlayerStateManager
import io.github.seriousguy888.slashspec.yaml.ConfigReader
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.io.File

class SlashSpec : JavaPlugin() {
    val playerPrefsManager = PlayerPreferencesManager(this, File(dataFolder, "playerprefs.yml"))
    val playerStateManager = PlayerStateManager(this)
    val playerManager = PlayerManager(this)
    val combatTimerManager = CombatTimerManager(this)
    val floatingHeadManager = FloatingHeadManager(this)
    val tabCompletionUtil = TabCompletionUtil(this)
    val configReader = ConfigReader(this)

    val specCommand = SpecCommand(this)

    override fun onEnable() {
        registerCommands()
        registerListeners()
        registerTasks()
        checkForDependencies()
    }

    private fun registerCommands() {
        getCommand("spec")?.setExecutor(specCommand)
        logger.info("Registered commands.")
    }

    private fun registerListeners() {
        val pm = server.pluginManager
        pm.registerEvents(CombatTimerListener(this), this)
        pm.registerEvents(EntityDamageByEntityListener(), this)
        pm.registerEvents(PlayerGameModeChangeListener(this), this)
        pm.registerEvents(PlayerMoveListener(this), this)
        pm.registerEvents(PlayerQuitListener(this), this)
        pm.registerEvents(PlayerTeleportListener(), this)
    }

    private fun registerTasks() {
        // Every second, update the floating head location for all players that need it
        object : BukkitRunnable() {
            override fun run() {
                playerStateManager.stateCache.forEach {
                    val uuid = it.key

                    // Try to get the player. If not online, return.
                    val player = Bukkit.getPlayer(uuid) ?: return@forEach

                    val isInSpec = playerManager.isPlayerInSpec(player)
                    if (!isInSpec)
                        return@forEach

                    floatingHeadManager.displayHead(player)
                }
            }
        }.runTaskTimer(this, 0, 20)

        // Every five minutes, save all the yaml files
        object : BukkitRunnable() {
            override fun run() {
                saveYamls()
            }
        }.runTaskTimer(this, 0, 20 * 60 * 5)

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
                        " floating heads feature."
            )
        }
    }

    override fun onDisable() {
        saveYamls()

        Bukkit.getOnlinePlayers().forEach {
            floatingHeadManager.removeFloatingHead(it)
        }
    }

    private fun saveYamls() {
//        playerStateManager.save()
        playerPrefsManager.save()
    }

    fun isProtocolLibInstalled(): Boolean {
        return server.pluginManager.getPlugin("ProtocolLib") != null
    }
}
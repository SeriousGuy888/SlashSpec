package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.commands.SpecCommand
import org.bukkit.plugin.java.JavaPlugin

class SlashSpec : JavaPlugin() {
    val specPlayersManager = SpecPlayersManager(this)

    override fun onEnable() {
        registerCommands()
    }

    private fun registerCommands() {
        getCommand("spec")?.setExecutor(SpecCommand(this))
        logger.info("Registered commands.")
    }

    override fun onDisable() {
        logger.info("f")
    }
}
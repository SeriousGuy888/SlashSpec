package io.github.seriousguy888.slashspec

import org.bukkit.plugin.java.JavaPlugin

class SlashSpec : JavaPlugin() {
    override fun onEnable() {
        logger.info("woggi")
    }

    override fun onDisable() {
        logger.info("froggi")
    }
}
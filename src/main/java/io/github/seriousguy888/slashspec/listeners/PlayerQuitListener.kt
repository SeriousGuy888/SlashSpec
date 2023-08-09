package io.github.seriousguy888.slashspec.listeners

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerQuitListener(private val plugin: SlashSpec) : Listener {
    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val player = event.player
        plugin.floatingHeadManager.removeFloatingHead(player)
    }
}
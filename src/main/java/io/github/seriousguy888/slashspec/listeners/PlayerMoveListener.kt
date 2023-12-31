package io.github.seriousguy888.slashspec.listeners

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PlayerMoveListener(private val plugin: SlashSpec) : Listener {
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player

        val isInSpec = plugin.playerManager.isPlayerInSpec(player)
        if(!isInSpec)
            return

        plugin.floatingHeadManager.displayHead(player)
    }
}
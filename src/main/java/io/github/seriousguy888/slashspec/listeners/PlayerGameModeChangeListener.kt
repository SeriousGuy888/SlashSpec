package io.github.seriousguy888.slashspec.listeners

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent

class PlayerGameModeChangeListener(private val plugin: SlashSpec) : Listener {
    /**
     * When a player that is using /spec and in spectator mode switches out of spectator mode,
     * the player will automatically have /spec disabled.
     */
    @EventHandler
    fun autoUnspecOnGamemodeSwitch(event: PlayerGameModeChangeEvent) {
        val player = event.player
        val previousGamemode = player.gameMode

        if (previousGamemode != GameMode.SPECTATOR)
            return
        if (event.newGameMode == GameMode.SPECTATOR)
            return

        val isPlayerInSpec = plugin.playerManager.isPlayerInSpec(player)
        if (!isPlayerInSpec)
            return

        plugin.playerManager.stopTrackingPlayerForSpec(player)
    }
}
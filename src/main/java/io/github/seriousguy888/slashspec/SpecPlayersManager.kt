package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.state.PlayerState
import io.github.seriousguy888.slashspec.state.StateManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player

class SpecPlayersManager(private val plugin: SlashSpec) {
    private val stateManager = StateManager(plugin)

    fun togglePlayer(player: Player) {
        if (player.gameMode == GameMode.SPECTATOR) {
            val playerState = stateManager.getPlayer(player)
            if (playerState == null) {
                player.sendMessage("Cannot put you back to where you were. " +
                        "(You did not use /spec to enter spectator mode)")
                return
            }

            restorePlayerState(player, playerState)
        } else {
            stateManager.addPlayer(player)
            player.gameMode = GameMode.SPECTATOR
        }
    }

    private fun restorePlayerState(player: Player, state: PlayerState) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.teleport(state.location)
            player.gameMode = state.gameMode
        })

        stateManager.removePlayer(player)
    }
}
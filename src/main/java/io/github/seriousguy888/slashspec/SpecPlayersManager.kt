package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.state.PlayerState
import io.github.seriousguy888.slashspec.state.StateManager
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player

class SpecPlayersManager(private val plugin: SlashSpec) {
    private val stateManager = StateManager(plugin)

    fun togglePlayer(player: Player) {
        togglePlayer(player, SpecToggleDirection.TOGGLE)
    }

    fun togglePlayer(player: Player, dir: SpecToggleDirection): Boolean {
        return when (dir) {
            SpecToggleDirection.TOGGLE -> {
                if (player.gameMode == GameMode.SPECTATOR) {
                    putPlayerOutOfSpec(player)
                } else {
                    putPlayerIntoSpec(player)
                }
            }
            SpecToggleDirection.INTO_SPEC -> putPlayerIntoSpec(player)
            SpecToggleDirection.OUT_OF_SPEC -> putPlayerOutOfSpec(player)
        }
    }

    private fun putPlayerIntoSpec(player: Player): Boolean {
        if (player.gameMode == GameMode.SPECTATOR) {
            return false
        }

        stateManager.addPlayer(player)
        player.gameMode = GameMode.SPECTATOR
        return true
    }

    private fun putPlayerOutOfSpec(player: Player): Boolean {
        val playerState = stateManager.getPlayer(player)
        if (playerState == null) {
            player.sendMessage("Cannot put you back to where you were. " +
                    "(You did not use /spec to enter spectator mode)")
            return false
        }

        restorePlayerState(player, playerState)
        return true
    }

    private fun restorePlayerState(player: Player, state: PlayerState) {
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.teleport(state.location)
            player.gameMode = state.gameMode
        })

        stateManager.removePlayer(player)
    }
}

/**
 * Whether to force the player into spec, out of spec, or to toggle based on the current state.
 */
enum class SpecToggleDirection {
    INTO_SPEC,      // Force into spectator mode
    OUT_OF_SPEC,    // Force out of spectator mode
    TOGGLE,         // Change gamemode based on whether the player is in spectator mode right now
}
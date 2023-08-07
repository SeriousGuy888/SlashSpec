package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.state.StateManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.io.File

class PlayerManager(private val plugin: SlashSpec) {
    val stateManager = StateManager(plugin, File(plugin.dataFolder, "playerdata.yml"))

    fun togglePlayer(player: Player) {
        togglePlayer(
                player = player,
                dir = SpecToggleDirection.TOGGLE)
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
        stateManager.savePlayerData()
        player.gameMode = GameMode.SPECTATOR

        return true
    }

    private fun putPlayerOutOfSpec(player: Player): Boolean {
        val playerState = stateManager.getPlayer(player)
        if (playerState == null) {
            player.sendMessage(Component.text(
                    "You did not use /spec to enter spectator mode.",
                    NamedTextColor.RED))
            return false
        }

        playerState.restore(player)
        stateManager.removePlayer(player)
        return true
    }

    fun isPlayerInSpec(player: Player): Boolean {
        return stateManager.hasPlayer(player)
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
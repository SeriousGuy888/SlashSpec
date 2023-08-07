package io.github.seriousguy888.slashspec.state

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player

class PlayerState(val location: Location, val gameMode: GameMode) {
    companion object {
        fun fromPlayer(player: Player): PlayerState {
            return PlayerState(
                    location = player.location,
                    gameMode = player.gameMode)
        }
    }
}

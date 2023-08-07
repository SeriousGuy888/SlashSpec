package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player

class PlayerState(private val plugin: SlashSpec,
                  val location: Location,
                  val gameMode: GameMode,
                  val isFlying: Boolean) {
    companion object {
        fun fromPlayer(player: Player, plugin: SlashSpec): PlayerState {
            return PlayerState(
                    plugin = plugin,
                    location = player.location,
                    gameMode = player.gameMode,
                    isFlying = player.isFlying)
        }
    }

    fun restore(player: Player) {
        // runTask one tick later to prevent the player moving too fast warning in the console
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.teleport(location)
            player.gameMode = gameMode
            player.isFlying = isFlying
        })
    }

    fun serialise(): HashMap<String, Any> {
        val serialisation = HashMap<String, Any>()

        serialisation["location"] = location
        serialisation["gamemode"] = gameMode.name
        serialisation["isFlying"] = isFlying

        return serialisation
    }
}

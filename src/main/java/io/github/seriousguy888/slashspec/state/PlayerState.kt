package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

data class PlayerState(
    private val plugin: SlashSpec,
    private val worldName: String,  // Storing the Location object broken up like this because the World of the Location
    private val xyz: Vector,        // might not be loaded when the plugin loads the data, causing an exception in the
    private val yaw: Float,         // console & a failure to load in a player's state, causing players to be stuck in
    private val pitch: Float,       // spec after a server restart. This fixes that bug.
    private val gameMode: GameMode,
    private val isFlying: Boolean,
) {
    companion object {
        fun fromPlayer(player: Player, plugin: SlashSpec): PlayerState {
            val location = player.location

            return PlayerState(
                plugin = plugin,
                worldName = location.world!!.name,
                xyz = location.toVector(),
                yaw = location.yaw,
                pitch = location.pitch,
                gameMode = player.gameMode,
                isFlying = player.isFlying
            )
        }
    }

    fun restore(player: Player) {
        val world = Bukkit.getWorld(worldName) ?: throw IllegalArgumentException("World of location is not loaded.")
        val location = Location(world, xyz.x, xyz.y, xyz.z, yaw, pitch)

        // runTask one tick later to prevent the player moving too fast warning in the console
        Bukkit.getScheduler().runTask(plugin, Runnable {
            player.teleport(location)
            player.gameMode = gameMode
            player.isFlying = isFlying
        })
    }

    fun serialise(): HashMap<String, Any> {
        val serialisation = HashMap<String, Any>()

        serialisation["worldName"] = worldName
        serialisation["xyz"] = xyz
        serialisation["yaw"] = yaw
        serialisation["pitch"] = pitch
        serialisation["gamemode"] = gameMode.name
        serialisation["isFlying"] = isFlying

        return serialisation
    }
}

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
    private val fallDistance: Float,
    private val remainingAir: Int,
    private val fireTicks: Int,
    private val freezeTicks: Int,
) {
    companion object {
        fun fromPlayer(player: Player, plugin: SlashSpec): PlayerState {
            var location = player.location

            // If the player is in a boat or minecart, their y-pos will be lower than the vehicle itself,
            // causing the player to glitch into the floor when coming back from spec, so use the vehicle's
            // position instead to prevent this.
            if (player.isInsideVehicle) {
                val vehicle = player.vehicle!!
                if (player.location.y < vehicle.location.y) {
                    location = vehicle.location
                    location.yaw = player.location.yaw
                    location.pitch = player.location.pitch
                }
            }

            return PlayerState(
                plugin = plugin,
                worldName = location.world!!.name,
                xyz = location.toVector(),
                yaw = location.yaw,
                pitch = location.pitch,
                gameMode = player.gameMode,
                isFlying = player.isFlying,
                fallDistance = player.fallDistance,
                remainingAir = player.remainingAir,
                fireTicks = player.fireTicks,
                freezeTicks = player.freezeTicks,
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
            player.fallDistance = fallDistance
            player.remainingAir = remainingAir
            player.fireTicks = fireTicks
            player.freezeTicks = freezeTicks
        })
    }

    fun serialise(): HashMap<String, Any> {
        val ser = HashMap<String, Any>()

        ser["worldName"] = worldName
        ser["xyz"] = xyz
        ser["yaw"] = yaw
        ser["pitch"] = pitch
        ser["gamemode"] = gameMode.name
        ser["isFlying"] = isFlying
        ser["fallDistance"] = fallDistance
        ser["remainingAir"] = remainingAir
        ser["fireTicks"] = fireTicks
        ser["freezeTicks"] = freezeTicks

        return ser
    }
}

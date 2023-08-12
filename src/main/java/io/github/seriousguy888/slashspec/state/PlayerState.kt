package io.github.seriousguy888.slashspec.state

import com.google.gson.Gson
import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.Player

data class PlayerState(
    private val plugin: SlashSpec,
    private val worldName: String,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val yaw: Float,
    private val pitch: Float,
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
                x = location.x,
                y = location.y,
                z = location.z,
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

        fun fromJson(json: String, plugin: SlashSpec): PlayerState {
            val gson = Gson()
            val map = gson.fromJson(json, HashMap::class.java)
                .toMutableMap()

            return PlayerState(
                plugin = plugin,
                worldName = map["worldName"].toString(),
                x = map["x"].toString().toDoubleOrNull() ?: 0.0,
                y = map["y"].toString().toDoubleOrNull() ?: 0.0,
                z = map["z"].toString().toDoubleOrNull() ?: 0.0,
                yaw = map["yaw"].toString().toFloatOrNull() ?: 0f,
                pitch = map["pitch"].toString().toFloatOrNull() ?: 0f,
                gameMode = GameMode.entries
                    .find { it.name == map["gameMode"] }
                    ?: GameMode.SURVIVAL,
                isFlying = map["isFlying"].toString().toBooleanStrictOrNull() ?: false,
                fallDistance = map["fallDistance"].toString().toFloatOrNull() ?: 0f,
                remainingAir = map["remainingAir"].toString().toDoubleOrNull()?.toInt() ?: 0,
                fireTicks = map["fireTicks"].toString().toDoubleOrNull()?.toInt() ?: 0,
                freezeTicks = map["freezeTicks"].toString().toDoubleOrNull()?.toInt() ?: 0,
            )
        }
    }

    fun restore(player: Player) {
        val world = Bukkit.getWorld(worldName) ?: throw IllegalArgumentException("World of location is not loaded.")
        val location = Location(world, x, y, z, yaw, pitch)

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

    fun toJson(): String {
        val ser = HashMap<String, Any>()

        ser["worldName"] = worldName
        ser["x"] = x
        ser["y"] = y
        ser["z"] = z
        ser["yaw"] = yaw
        ser["pitch"] = pitch
        ser["gamemode"] = gameMode.name
        ser["isFlying"] = isFlying
        ser["fallDistance"] = fallDistance
        ser["remainingAir"] = remainingAir
        ser["fireTicks"] = fireTicks
        ser["freezeTicks"] = freezeTicks

        return Gson().toJson(ser)
    }
}

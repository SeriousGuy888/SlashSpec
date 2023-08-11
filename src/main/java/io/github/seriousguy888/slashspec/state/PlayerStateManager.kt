package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import io.github.seriousguy888.slashspec.yaml.AbstractStateManager
import org.bukkit.GameMode
import org.bukkit.entity.Player
import java.io.File

class PlayerStateManager(
    private val plugin: SlashSpec,
    private val dataFileLoc: File
) : AbstractStateManager<String, PlayerState>(plugin, dataFileLoc) {

    init {
        load()
    }

    override fun save() {
        // If an existing player section defines data for a player that is no longer
        // being tracked, delete it from the file as well.
        yamlConfig.getKeys(false).forEach { uuid ->
            if (!map.containsKey(uuid)) {
                yamlConfig.set(uuid, null)
            }
        }

        map.forEach {
            yamlConfig.set(it.key, it.value.serialise())
        }

        yamlConfig.save(dataFileLoc)
    }

    override fun load() {
        val keys = yamlConfig.getKeys(false)

        for (uuid in keys) {
            val section = yamlConfig.getConfigurationSection(uuid) ?: continue

            val state = PlayerState(
                plugin = plugin,
                worldName = section.getString("worldName") ?: continue,
                xyz = section.getVector("xyz") ?: continue,
                yaw = section.getDouble("yaw", 0.0).toFloat(),
                pitch = section.getDouble("pitch", 0.0).toFloat(),
                gameMode = GameMode.entries
                    .find { it.name == section.getString("gamemode", "SURVIVAL") }
                    ?: GameMode.SURVIVAL,
                isFlying = section.getBoolean("isFlying", false),
                remainingAir = section.getInt("remainingAir", 0),
                fireTicks = section.getInt("fireTicks", 0),
                freezeTicks = section.getInt("freezeTicks", 0),
            )

            map[uuid] = state
        }
    }

    fun addPlayer(player: Player) {
        map[player.uniqueId.toString()] = PlayerState.fromPlayer(player, plugin)
    }

    fun getPlayer(player: Player): PlayerState? {
        return map[player.uniqueId.toString()]
    }

    fun hasPlayer(player: Player): Boolean {
        return map.containsKey(player.uniqueId.toString())
    }

    fun removePlayer(player: Player) {
        map.remove(player.uniqueId.toString())
    }
}
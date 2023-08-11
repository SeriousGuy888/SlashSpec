package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import io.github.seriousguy888.slashspec.yaml.AbstractStateManager
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
            val state = PlayerState.fromConfigSection(section, plugin) ?: continue

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
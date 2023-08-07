package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.GameMode
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class StateManager(private val plugin: SlashSpec,
                   private val dataFileLoc: File) {
    private val stateMap = HashMap<String /* UUID */, PlayerState>()
    private val dataFile: FileConfiguration = YamlConfiguration.loadConfiguration(dataFileLoc)

    init {
        loadPlayerData()
    }

    private fun loadPlayerData() {
        val keys = dataFile.getKeys(false)

        for (uuid in keys) {
            val section = dataFile.getConfigurationSection(uuid) ?: continue

            val state = PlayerState(
                    plugin = plugin,
                    location = section.getLocation("location") ?: continue,
                    gameMode = GameMode
                            .values()
                            .find { it.name == section.getString("gamemode") }
                            ?: continue,
                    isFlying = section.getBoolean("isFlying", false)
            )

            stateMap[uuid] = state
        }
    }

    fun savePlayerData() {
        // If an existing player section defines data for a player that is no longer
        // being tracked, delete it from the file as well.
        dataFile.getKeys(false).forEach { uuid ->
            if (!stateMap.containsKey(uuid)) {
                dataFile.set(uuid, null)
            }
        }

        stateMap.forEach {
            dataFile.set(it.key, it.value.serialise())
        }

        dataFile.save(dataFileLoc)
    }

    fun addPlayer(player: Player) {
        stateMap[player.uniqueId.toString()] = PlayerState.fromPlayer(player, plugin)
    }

    fun getPlayer(player: Player): PlayerState? {
        return stateMap[player.uniqueId.toString()]
    }

    fun removePlayer(player: Player) {
        stateMap.remove(player.uniqueId.toString())
    }
}
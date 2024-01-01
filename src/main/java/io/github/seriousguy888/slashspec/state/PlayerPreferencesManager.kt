package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

class PlayerPreferencesManager(
    private val plugin: SlashSpec,
    private val prefsFileLoc: File
) {
    private val yamlConfig = YamlConfiguration.loadConfiguration(prefsFileLoc)
    private val map = HashMap<String, PlayerPrefs>()

    init {
        load()
    }

    fun get(player: Player): PlayerPrefs {
        return map[player.uniqueId.toString()] ?: PlayerPrefs.getDefault(plugin)
    }

    fun set(player: Player, newPrefs: PlayerPrefs) {
        map[player.uniqueId.toString()] = newPrefs
    }

    fun save() {
        map.forEach {
            val serialisation = HashMap<String, Any>()
            serialisation["isGhostMode"] = it.value.isGhostMode
            serialisation["isTeleportableTo"] = it.value.isTeleportableTo
            yamlConfig.set(it.key, serialisation)
        }

        yamlConfig.save(prefsFileLoc)
    }

    private fun load() {
        val keys = yamlConfig.getKeys(false)

        keys.forEach { uuid ->
            val section = yamlConfig.getConfigurationSection(uuid) ?: return@forEach

            val state = PlayerPrefs(
                isGhostMode = section.getBoolean("isGhostMode", false),
                isTeleportableTo = section.getBoolean("isTeleportableTo", true)
            )

            map[uuid] = state
        }
    }
}
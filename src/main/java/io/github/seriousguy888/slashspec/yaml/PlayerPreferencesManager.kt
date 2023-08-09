package io.github.seriousguy888.slashspec.yaml

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File

// TODO: merge this with StateManager somehow

class PlayerPreferencesManager(private val plugin: SlashSpec,
                               private val prefsFileLoc: File) {
    private var prefsFile: YamlConfiguration = YamlConfiguration.loadConfiguration(prefsFileLoc)
    private val prefsMap = HashMap<String /* UUID */, PlayerPrefs>()

    data class PlayerPrefs(var isGhostMode: Boolean = false)

    init {
        load()
    }

    fun get(player: Player): PlayerPrefs {
        return prefsMap[player.uniqueId.toString()] ?: PlayerPrefs()
    }

    fun set(player: Player, newPrefs: PlayerPrefs) {
        prefsMap[player.uniqueId.toString()] = newPrefs
    }

    fun save() {
        prefsMap.forEach {
            val serialisation = HashMap<String, Any>()
            serialisation["isGhostMode"] = it.value.isGhostMode
            prefsFile.set(it.key, serialisation)
        }

        prefsFile.save(prefsFileLoc)
    }

    private fun load() {
        val keys = prefsFile.getKeys(false)

        keys.forEach { uuid ->
            val section = prefsFile.getConfigurationSection(uuid) ?: return@forEach

            val state = PlayerPrefs(
                    isGhostMode = section.getBoolean("isGhostMode", true)
            )

            prefsMap[uuid] = state
        }
    }
}
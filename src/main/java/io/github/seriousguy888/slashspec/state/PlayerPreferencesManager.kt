package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import io.github.seriousguy888.slashspec.yaml.AbstractStateManager
import org.bukkit.entity.Player
import java.io.File

class PlayerPreferencesManager(
    private val plugin: SlashSpec,
    private val prefsFileLoc: File
) : AbstractStateManager<String, PlayerPrefs>(plugin, prefsFileLoc) {

    init {
        load()
    }

    fun get(player: Player): PlayerPrefs {
        return map[player.uniqueId.toString()] ?: PlayerPrefs.getDefault(plugin)
    }

    fun set(player: Player, newPrefs: PlayerPrefs) {
        map[player.uniqueId.toString()] = newPrefs
    }

    override fun save() {
        map.forEach {
            val serialisation = HashMap<String, Any>()
            serialisation["isGhostMode"] = it.value.isGhostMode
            yamlConfig.set(it.key, serialisation)
        }

        yamlConfig.save(prefsFileLoc)
    }

    override fun load() {
        val keys = yamlConfig.getKeys(false)

        keys.forEach { uuid ->
            val section = yamlConfig.getConfigurationSection(uuid) ?: return@forEach

            val state = PlayerPrefs(
                isGhostMode = section.getBoolean("isGhostMode", true)
            )

            map[uuid] = state
        }
    }
}
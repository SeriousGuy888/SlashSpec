package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec

data class PlayerPrefs(var isGhostMode: Boolean, var isTeleportableTo: Boolean) {
    companion object {
        fun getDefault(plugin: SlashSpec): PlayerPrefs {
            return PlayerPrefs(
                isGhostMode = plugin.configReader.ghostModeDefault,
                isTeleportableTo = plugin.configReader.teleportableToDefault
            )
        }
    }
}
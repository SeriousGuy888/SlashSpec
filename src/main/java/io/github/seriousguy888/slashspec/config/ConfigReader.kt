package io.github.seriousguy888.slashspec.config

import io.github.seriousguy888.slashspec.SlashSpec

class ConfigReader(private val plugin: SlashSpec) {
    private val config = plugin.config
    private val defaults = config.defaults

    init {
        plugin.saveDefaultConfig()

        val defaultKeys = defaults!!.getKeys(true)

        defaultKeys.forEach { path ->
            val isPresentInConfig = config.getKeys(true).contains(path)
            if (!isPresentInConfig) {
                config[path!!] = defaults.get(path)
            }
        }

        // Copy comments from the default config
        config
            .getKeys(true)
            .forEach { key ->
                config.setComments(key, defaults.getComments(key))
                config.setInlineComments(key, defaults.getInlineComments(key))
            }

        plugin.saveConfig()
    }

    val shouldUseFloatingHead: Boolean
        get() = config.getBoolean("use-floating-head", true)

    val ghostModeDefault: Boolean
        get() = config.getBoolean("ghost-mode-default", false)

    val teleportableToDefault: Boolean
        get() = config.getBoolean("teleportable-to-default", true)

    val combatTimerSeconds: Int
        get() = config.getInt("combat-timer-seconds", 30)

    val shouldPreventVoidDamage: Boolean
        get() = config.getBoolean("prevent-void-damage-in-spec", true)
}
package io.github.seriousguy888.slashspec.yaml

import io.github.seriousguy888.slashspec.SlashSpec

class ConfigReader(private val plugin: SlashSpec) {
    private val config = plugin.config
    private val defaults = config.defaults

    init {
        plugin.saveDefaultConfig()

        val defaultKeys = defaults!!.getKeys(true)

        var needsSaving = false
        defaultKeys.forEach { path ->
            val isPresentInConfig = config.getKeys(true).contains(path)
            if (!isPresentInConfig) {
                config[path!!] = defaults.get(path)
                needsSaving = true
            }
        }

        if(needsSaving) {
            plugin.saveConfig()
        }
    }

    val shouldUseFloatingHead: Boolean
        get() = config.getBoolean("use-floating-head", true)

    val ghostModeDefault: Boolean
        get() = config.getBoolean("ghost-mode-default", false)
}
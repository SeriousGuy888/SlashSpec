package io.github.seriousguy888.slashspec.yaml

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

abstract class AbstractStateManager<K, V>(
    plugin: SlashSpec,
    yamlFileLoc: File,
) {
    protected val yamlConfig = YamlConfiguration.loadConfiguration(yamlFileLoc)
    val map = HashMap<K, V>()

    abstract fun save()
    protected abstract fun load()
}
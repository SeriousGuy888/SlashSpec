package io.github.seriousguy888.slashspec

import org.bukkit.Bukkit

class TabCompletionUtil(plugin: SlashSpec) {
    fun getCompletions(currInput: String, possibleCompletions: List<String>): List<String> {
        return possibleCompletions.filter { it.startsWith(currInput, true) }
    }

    fun getPlayerNames(currInput: String): List<String> {
        return getCompletions(
                currInput,
                Bukkit.getOnlinePlayers()
                        .map { it.name })
    }
}
package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.entity.Player

class StateManager(plugin: SlashSpec) {
    private val stateMap = HashMap<Player, PlayerState>()

    fun addPlayer(player: Player) {
        stateMap[player] = PlayerState.fromPlayer(player)
    }

    fun getPlayer(player: Player): PlayerState? {
        return stateMap[player]
    }

    fun removePlayer(player: Player) {
        stateMap.remove(player)
    }
}
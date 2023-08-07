package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.entity.Player

class StateManager(private val plugin: SlashSpec) {
    private val stateMap = HashMap<Player, PlayerState>()

    fun addPlayer(player: Player) {
        stateMap[player] = PlayerState.fromPlayer(player, plugin)
    }

    fun getPlayer(player: Player): PlayerState? {
        return stateMap[player]
    }

    fun removePlayer(player: Player) {
        stateMap.remove(player)
    }
}
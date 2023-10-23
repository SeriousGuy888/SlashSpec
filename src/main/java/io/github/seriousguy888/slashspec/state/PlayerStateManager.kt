package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import io.github.seriousguy888.slashspec.persistentdata.PlayerStateDataType
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.util.*

class PlayerStateManager(private val plugin: SlashSpec) {
    val stateCache = HashMap<UUID, PlayerState>()

    private val namespacedKey = NamespacedKey(plugin, "spec_state")
    private val dataType = PlayerStateDataType(plugin)

    fun addPlayer(player: Player) {
        val container = player.persistentDataContainer
        val state = PlayerState.fromPlayer(player, plugin)
        container.set(namespacedKey, dataType, state)

        stateCache[player.uniqueId] = state
    }

    fun getPlayer(player: Player): PlayerState? {
        if (stateCache.containsKey(player.uniqueId)) {
            return stateCache[player.uniqueId]
        }

        val container = player.persistentDataContainer
        val hasStateStored = container.has(namespacedKey, dataType)

        if (hasStateStored) {
            return container.get(namespacedKey, dataType)
        }

        return null
    }

    fun hasPlayer(player: Player): Boolean {
        return stateCache.containsKey(player.uniqueId) || player.persistentDataContainer.has(namespacedKey, dataType)
    }

    fun removePlayer(player: Player) {
        stateCache.remove(player.uniqueId)
        player.persistentDataContainer.remove(namespacedKey)
    }
}
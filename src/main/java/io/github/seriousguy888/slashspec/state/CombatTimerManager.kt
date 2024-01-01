package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.entity.Player
import java.util.*

class CombatTimerManager(private val plugin: SlashSpec) {
    private val combatTimestampMap = HashMap<UUID, Long>()

    private val timerLengthMillis: Long
        get() = plugin.configReader.combatTimerSeconds * 1000L


    fun putPlayerInCombat(player: Player) {
        combatTimestampMap[player.uniqueId] = System.currentTimeMillis()
    }

    fun getTimeRemainingMillis(player: Player): Long {
        val timeElapsedMillis = getTimeElapsedMillis(player) ?: return 0
        return timerLengthMillis - timeElapsedMillis
    }

    private fun getTimeElapsedMillis(player: Player): Long? {
        if (!combatTimestampMap.containsKey(player.uniqueId)) {
            return null
        }

        val currTimeMillis = System.currentTimeMillis()
        val prevTimeMillis = combatTimestampMap[player.uniqueId]!!

        return currTimeMillis - prevTimeMillis
    }
}
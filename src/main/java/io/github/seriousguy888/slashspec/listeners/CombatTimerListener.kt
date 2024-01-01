package io.github.seriousguy888.slashspec.listeners

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class CombatTimerListener(private val plugin: SlashSpec) : Listener {
    @EventHandler
    fun onDamage(event: EntityDamageByEntityEvent) {

        if (event.entity is Player) {
            if (
                (event.damager is Player) ||
                (event.damager is Projectile && (event.damager as Projectile).shooter is Player)
            ) {
                val victim = event.entity as Player
                val attacker =
                    if (event.damager is Projectile) {
                        (event.damager as Projectile).shooter as Player
                    } else {
                        event.damager as Player
                    }

                plugin.combatTimerManager.putPlayerInCombat(attacker)
                plugin.combatTimerManager.putPlayerInCombat(victim)
            }
        }

    }
}
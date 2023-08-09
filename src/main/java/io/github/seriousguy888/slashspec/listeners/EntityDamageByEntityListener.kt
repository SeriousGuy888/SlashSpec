package io.github.seriousguy888.slashspec.listeners

import org.bukkit.entity.Firework
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class EntityDamageByEntityListener : Listener {
    @EventHandler
    fun preventFireworkDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager
        if (damager !is Firework)
            return
        if (!damager.hasMetadata("no_damage_firework"))
            return

        event.isCancelled = true
    }
}
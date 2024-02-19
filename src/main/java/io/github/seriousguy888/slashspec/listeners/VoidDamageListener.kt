package io.github.seriousguy888.slashspec.listeners

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent

class VoidDamageListener(private val plugin: SlashSpec) : Listener {

    @EventHandler
    fun preventVoidDamage(event: EntityDamageEvent) {

        if (event.cause != EntityDamageEvent.DamageCause.VOID)
            return
        if(event.entity !is Player)
            return

        if(!plugin.configReader.shouldPreventVoidDamage)
            return

        val player = event.entity as Player
        if(!plugin.playerManager.isPlayerInSpec(player))
            return

        event.isCancelled = true
    }
}
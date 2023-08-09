package io.github.seriousguy888.slashspec.listeners

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerTeleportEvent

class PlayerTeleportListener : Listener {
    @EventHandler
    fun preventSpectatorTeleportMenu(event: PlayerTeleportEvent) {
        if(event.cause != PlayerTeleportEvent.TeleportCause.SPECTATE)
            return

        val player = event.player
        if(player.hasPermission("slashspec.use_hotbar_teleport_menu"))
            return

        event.isCancelled = true
        player.sendMessage(Component
            .text("You do not have permission to use the hotbar teleport menu. " +
                    "Ask an admin if this is mistake.")
            .color(NamedTextColor.RED))
    }
}
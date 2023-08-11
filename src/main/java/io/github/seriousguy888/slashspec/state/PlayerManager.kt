package io.github.seriousguy888.slashspec.state

import io.github.seriousguy888.slashspec.SlashSpec
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class PlayerManager(private val plugin: SlashSpec) {
    val playerStateManager = PlayerStateManager(plugin, File(plugin.dataFolder, "playerdata.yml"))

    fun toggleSpec(player: Player, to: Boolean? = null): Boolean {
        return when (to) {
            null -> {
                if (player.gameMode == GameMode.SPECTATOR) {
                    putPlayerOutOfSpec(player)
                } else {
                    putPlayerIntoSpec(player)
                }
            }

            true -> putPlayerIntoSpec(player)
            false -> putPlayerOutOfSpec(player)
        }
    }

    fun toggleGhost(player: Player, to: Boolean? = null): Boolean {
        val prefs = plugin.playerPrefsManager.get(player)
        prefs.isGhostMode = to ?: !prefs.isGhostMode
        plugin.playerPrefsManager.set(player, prefs)

        if (prefs.isGhostMode) {
            plugin.floatingHeadManager.removeFloatingHead(player)
        }

        return prefs.isGhostMode
    }

    private fun putPlayerIntoSpec(player: Player): Boolean {
        if (player.gameMode == GameMode.SPECTATOR) {
            return false
        }

        playerStateManager.addPlayer(player)
        playerStateManager.save()


        // A lead can stretch a maximum of 10 blocks.
        // https://minecraft.fandom.com/wiki/Lead
        val nearbyEntities = player.getNearbyEntities(10.0, 10.0, 10.0)
        nearbyEntities
            .forEach {
                if (it !is LivingEntity || !it.isLeashed || it.leashHolder != player as Entity)
                    return@forEach

                it.setLeashHolder(null)
                if (player.gameMode != GameMode.CREATIVE) {
                    it.world.dropItemNaturally(it.location, ItemStack(Material.LEAD))
                }
            }

        player.gameMode = GameMode.SPECTATOR
        plugin.floatingHeadManager.displayHead(player)

        val actionbarComponents =
            ComponentBuilder(if (isPlayerInGhostMode(player)) "Invisible" else "Visible")
                .color(ChatColor.AQUA)
                .append(" to non-spectators. ")
                .color(ChatColor.WHITE)

        val subcmd = plugin.specCommand.getSubcommand("ghost") ?: return true
        val playerCanUseGhost = plugin.specCommand.hasPermissionForSubcommand(player, subcmd)
        if (playerCanUseGhost) {
            actionbarComponents
                .append("Toggle with")
                .color(ChatColor.WHITE)
                .append(" /spec ghost")
                .color(ChatColor.AQUA)
                .append(".")
                .color(ChatColor.WHITE)
        }

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *actionbarComponents.create())

        return true
    }

    private fun putPlayerOutOfSpec(player: Player): Boolean {
        val playerState = playerStateManager.getPlayer(player)
        if (playerState == null) {
            player.spigot().sendMessage(
                *ComponentBuilder("You did not use /spec to enter spectator mode.")
                    .color(ChatColor.RED)
                    .create()
            )
            return false
        }

        playerState.restore(player)
        stopTrackingPlayerForSpec(player)
        return true
    }

    /**
     * Different from #putPlayerOutOfSpec, in that this simply sets it so that the player
     * is no longer considered to be in spec.
     *
     * It does not change the gamemode of the player, nor does it teleport the player anywhere.
     */
    fun stopTrackingPlayerForSpec(player: Player) {
        playerStateManager.removePlayer(player)
        plugin.floatingHeadManager.removeFloatingHead(player)
    }

    fun isPlayerInSpec(player: Player): Boolean {
        return playerStateManager.hasPlayer(player)
    }

    private fun isPlayerInGhostMode(player: Player): Boolean {
        return plugin.playerPrefsManager.get(player).isGhostMode
    }
}
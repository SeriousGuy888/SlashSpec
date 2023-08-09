package io.github.seriousguy888.slashspec

import io.github.seriousguy888.slashspec.state.StateManager
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.io.File

class PlayerManager(private val plugin: SlashSpec) {
    val stateManager = StateManager(plugin, File(plugin.dataFolder, "playerdata.yml"))

    fun toggleSpec(player: Player) {
        toggleSpec(
                player = player,
                dir = SpecToggleDirection.TOGGLE)
    }

    fun toggleSpec(player: Player, dir: SpecToggleDirection): Boolean {
        return when (dir) {
            SpecToggleDirection.TOGGLE -> {
                if (player.gameMode == GameMode.SPECTATOR) {
                    putPlayerOutOfSpec(player)
                } else {
                    putPlayerIntoSpec(player)
                }
            }
            SpecToggleDirection.INTO_SPEC -> putPlayerIntoSpec(player)
            SpecToggleDirection.OUT_OF_SPEC -> putPlayerOutOfSpec(player)
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

        stateManager.addPlayer(player)
        stateManager.savePlayerData()


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
        player.sendActionBar(LegacyComponentSerializer
                .legacyAmpersand()
                .deserialize(buildString {
                    append("&b")
                    append(if (isPlayerInGhostMode(player)) "Invisible" else "Visible")
                    append("&f to non-spectators.")

                    val subcmd = plugin.specCommand.getSubcommand("ghost") ?: return true
                    if (plugin.specCommand.hasPermissionForSubcommand(player, subcmd)) {
                        append(" Toggle with &b/spec ghost&f.")
                    }
                }))

        return true
    }

    private fun putPlayerOutOfSpec(player: Player): Boolean {
        val playerState = stateManager.getPlayer(player)
        if (playerState == null) {
            player.sendMessage(Component.text(
                    "You did not use /spec to enter spectator mode.",
                    NamedTextColor.RED))
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
        stateManager.removePlayer(player)
        plugin.floatingHeadManager.removeFloatingHead(player)
    }

    fun isPlayerInSpec(player: Player): Boolean {
        return stateManager.hasPlayer(player)
    }

    fun isPlayerInGhostMode(player: Player): Boolean {
        return plugin.playerPrefsManager.get(player).isGhostMode
    }
}

/**
 * Whether to force the player into spec, out of spec, or to toggle based on the current state.
 */
enum class SpecToggleDirection {
    INTO_SPEC,      // Force into spectator mode
    OUT_OF_SPEC,    // Force out of spectator mode
    TOGGLE,         // Change gamemode based on whether the player is in spectator mode right now
}
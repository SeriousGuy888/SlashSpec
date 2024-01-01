package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpecTeleportToggleCommand(private val plugin: SlashSpec) : SubCommand() {
    override val name: String
        get() = "tptoggle"
    override val syntax: String
        get() = "/spec tptoggle [<enable/disable>] [<player>]"
    override val permission: String
        get() = "slashspec.teleport.toggle"

    private val adminPerm = "slashspec.teleport.toggle.others"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        var willBeEnabled: Boolean? = null
        if (args.size >= 2) {
            willBeEnabled = if (args[1].equals("enable", true)) {
                true
            } else if (args[1].equals("disable", true)) {
                false
            } else {
                sender.sendMessage(syntax)
                return
            }
        }

        var player = sender as Player
        if (args.size >= 3) {
            if (!sender.hasPermission(adminPerm)) {
                sender.spigot().sendMessage(
                    *ComponentBuilder("Insufficient permissions.")
                        .color(ChatColor.RED)
                        .create()
                )
                return
            }

            val specifiedPlayer = Bukkit.getPlayer(args[2])
            if (specifiedPlayer == null) {
                sender.spigot().sendMessage(
                    *ComponentBuilder("Invalid player.")
                        .color(ChatColor.RED)
                        .create()
                )
                return
            }

            player = specifiedPlayer
        }

        val isForcingOther = player != sender

        val isNowInGhostMode = plugin.playerManager.toggleTeleportableTo(player, willBeEnabled)

        sender.spigot().sendMessage(
            *ComponentBuilder(
                buildString {
                    append("Spectators can ")
                    append(
                        if (isNowInGhostMode) "now "
                        else "no longer "
                    )
                    append("teleport to ")
                    append(
                        if (isForcingOther) "this player "
                        else "you "
                    )
                    append("using '/spec tp'.")
                }
            )
                .color(ChatColor.AQUA)
                .create()
        )
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String>? {
        if (args.size == 2) {
            return plugin.tabCompletionUtil.getCompletions(args[1], listOf("enable", "disable"))
        }
        if (args.size == 3 && sender.hasPermission(adminPerm)) {
            return plugin.tabCompletionUtil.getPlayerNames(args[2])
        }

        return null
    }
}
package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpecGhostCommand(private val plugin: SlashSpec) : SubCommand() {
    override val name: String
        get() = "ghost"
    override val syntax: String
        get() = "/spec ghost [<enable/disable>] [<player>]"
    override val permission: String
        get() = "slashspec.ghost"

    private val adminPerm = "slashspec.ghost.others"

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
                sender.sendMessage(Component.text("Insufficient permissions.", NamedTextColor.RED))
                return
            }

            val specifiedPlayer = Bukkit.getPlayer(args[2])
            if (specifiedPlayer == null) {
                sender.sendMessage(Component.text("Invalid player.", NamedTextColor.RED))
                return
            }

            player = specifiedPlayer
        }

        val isForcingOther = player != sender

        val isNowInGhostMode = plugin.playerManager.toggleGhost(player, willBeEnabled)

        sender.sendMessage(Component.text(
                buildString {
                    append(
                            if (isForcingOther) "Player is "
                            else "You are ")
                    append(
                            if (isNowInGhostMode) "now "
                            else "no longer "
                    )
                    append("in ghost mode. Ghost mode prevents non-spectators from seeing spectators.")
                },
                NamedTextColor.AQUA
        ))
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
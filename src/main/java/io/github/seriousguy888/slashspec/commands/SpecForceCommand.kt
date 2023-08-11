package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.command.CommandSender

class SpecForceCommand(private val plugin: SlashSpec) : SubCommand() {
    override val name: String
        get() = "force"
    override val syntax: String
        get() = "/spec force <player> [<in/out>]"
    override val permission: String
        get() = "slashspec.force"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        // if the <player> arg has not been specified
        if (args.size <= 1) {
            sender.sendMessage(syntax)
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.spigot().sendMessage(
                *ComponentBuilder("Invalid player.")
                    .color(ChatColor.RED)
                    .create()
            )
            return
        }

        var willBeInSpec: Boolean? = null

        // if the [in/out] arg is set
        if (args.size >= 3) {
            willBeInSpec = if (args[2].equals("in", true))
                true
            else if (args[2].equals("out", true))
                false
            else {
                sender.sendMessage(syntax)
                return
            }
        }

        val isPlayerInSpec = plugin.playerManager.isPlayerInSpec(targetPlayer)
        if (!isPlayerInSpec && willBeInSpec == false) {
            sender.spigot().sendMessage(
                *ComponentBuilder("This player is not using /spec's spectator mode.")
                    .color(ChatColor.RED)
                    .create()
            )
            return
        }
        if (targetPlayer.gameMode == GameMode.SPECTATOR && willBeInSpec == true) {
            sender.spigot().sendMessage(
                *ComponentBuilder("This player is already in spectator mode.")
                    .color(ChatColor.RED)
                    .create()
            )
            return
        }
        if (targetPlayer.gameMode == GameMode.SPECTATOR && !isPlayerInSpec) {
            sender.spigot().sendMessage(
                *ComponentBuilder("This player is already in spectator mode and did not use /spec to enter it.")
                    .color(ChatColor.RED)
                    .create()
            )
            return
        }

        val success = plugin.playerManager.toggleSpec(
            player = targetPlayer,
            to = willBeInSpec
        )

        if (success) {
            sender.spigot().sendMessage(
                *ComponentBuilder(buildString {
                    append("Forced ")
                    append(targetPlayer.name)
                    when (willBeInSpec) {
                        true -> append(" into spec.")
                        false -> append(" out of spec.")
                        null -> append(" to toggle spec.")
                    }
                })
                    .color(ChatColor.AQUA)
                    .create()
            )
        } else {
            sender.spigot().sendMessage(
                *ComponentBuilder("Command did not change anything.")
                    .color(ChatColor.RED)
                    .create()
            )

        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 2) { // if user is on the <player> arg
            return plugin.tabCompletionUtil.getPlayerNames(args[1])
        }
        if (args.size == 3) { // if user is on the [in/out] arg
            return plugin.tabCompletionUtil.getCompletions(args[2], listOf("in", "out"))
        }

        // after all that, just don't provide any suggestions
        // overrides the default thing of suggesting playernames
        return listOf()
    }
}

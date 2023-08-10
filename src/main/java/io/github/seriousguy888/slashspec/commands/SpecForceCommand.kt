package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import io.github.seriousguy888.slashspec.SpecToggleDirection
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

        var dir = SpecToggleDirection.TOGGLE

        // if the [in/out] arg is set
        if (args.size >= 3) {
            dir = if (args[2].equals("in", true))
                SpecToggleDirection.INTO_SPEC
            else if (args[2].equals("out", true))
                SpecToggleDirection.OUT_OF_SPEC
            else {
                sender.sendMessage(syntax)
                return
            }
        }

        val isPlayerInSpec = plugin.playerManager.isPlayerInSpec(targetPlayer)
        if (!isPlayerInSpec && dir == SpecToggleDirection.OUT_OF_SPEC) {
            sender.spigot().sendMessage(
                *ComponentBuilder("This player is not using /spec's spectator mode.")
                    .color(ChatColor.RED)
                    .create()
            )
            return
        }
        if ((targetPlayer.gameMode == GameMode.SPECTATOR && dir == SpecToggleDirection.INTO_SPEC)) {
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
            dir = dir
        )

        if (success) {
            sender.spigot().sendMessage(
                *ComponentBuilder(buildString {
                    append("Forced ")
                    append(targetPlayer.name)
                    when (dir) {
                        SpecToggleDirection.INTO_SPEC -> append(" into spec.")
                        SpecToggleDirection.OUT_OF_SPEC -> append(" out of spec.")
                        SpecToggleDirection.TOGGLE -> append(" to toggle spec.")
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

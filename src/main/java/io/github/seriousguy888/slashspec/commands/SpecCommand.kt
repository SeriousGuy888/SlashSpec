package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class SpecCommand(private val plugin: SlashSpec) : TabExecutor {
    private val subcommands = ArrayList<SubCommand>()

    init {
        subcommands.add(SpecForceCommand(plugin))
        subcommands.add(SpecGhostCommand(plugin))
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage("The console cannot enter spectator mode.")
                return true
            }

            plugin.playerManager.toggleSpec(sender)

        } else {
            subcommands.forEach { subcommand ->
                if (args[0].equals(subcommand.name, true)) {
                    subcommand.execute(sender, args)
                }
            }
        }

        return true
    }

    override fun onTabComplete(sender: CommandSender,
                               command: Command,
                               label: String,
                               args: Array<out String>): List<String>? {
        if (args.size == 1) {
            return subcommands
                    .map { it.name }
                    .filter { it.startsWith(args[0], true) }
        } else if (args.size >= 2) {
            subcommands.forEach { subcommand ->
                if (args[0].equals(subcommand.name, true)) {
                    return subcommand.tabComplete(sender, args)
                }
            }
        }

        return null
    }
}
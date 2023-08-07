package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpecCommand(private val plugin: SlashSpec) : CommandExecutor {
    private val subcommands = ArrayList<SubCommand>()

    init {
        subcommands.add(SpecForceCommand())
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            if (sender !is Player) {
                sender.sendMessage("The console cannot enter spectator mode.")
                return true
            }

            plugin.specPlayersManager.togglePlayer(sender)

        } else {
            Bukkit.getLogger().info(subcommands.size.toString())
            subcommands.forEach { subcommand ->
                if (args[0].equals(subcommand.name, true)) {
                    subcommand.execute(sender, args)
                }
            }
        }

        return true
    }
}
package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class SpecCommand(private val plugin: SlashSpec) : TabExecutor {
    private val subcommands = ArrayList<SubCommand>()

    init {
        subcommands.add(SpecFireworkCommand(plugin))
        subcommands.add(SpecForceCommand(plugin))
        subcommands.add(SpecGhostCommand(plugin))
        subcommands.add(SpecTeleportCommand(plugin))
        subcommands.add(SpecTeleportToggleCommand(plugin))
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
                    if (!hasPermissionForSubcommand(sender, subcommand)) {
                        sender.spigot().sendMessage(
                            *ComponentBuilder("Insufficient permissions.")
                                .color(ChatColor.RED)
                                .create()
                        )
                        return true
                    }
                    subcommand.execute(sender, args)
                    return true
                }
            }

            sender.spigot().sendMessage(
                *ComponentBuilder("This subcommand does not exist.")
                    .color(ChatColor.RED)
                    .create()
            )
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        if (args.size == 1) {
            return plugin.tabCompletionUtil.getCompletions(args[0],
                subcommands
                    .filter { hasPermissionForSubcommand(sender, it) }
                    .map { it.name })
        } else if (args.size >= 2) {
            subcommands.forEach { subcommand ->
                if (args[0].equals(subcommand.name, true)) {
                    if (!hasPermissionForSubcommand(sender, subcommand))
                        return null
                    return subcommand.tabComplete(sender, args)
                }
            }
        }

        return null
    }

    fun getSubcommand(name: String): SubCommand? {
        return subcommands.firstOrNull { it.name == name }
    }

    fun hasPermissionForSubcommand(sender: CommandSender, subcommand: SubCommand): Boolean {
        if (subcommand.permission == null) {
            return true
        }
        return sender.hasPermission(subcommand.permission!!)
    }
}
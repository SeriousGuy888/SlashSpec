package io.github.seriousguy888.slashspec.commands

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class SpecForceCommand : SubCommand() {
    override val name: String
        get() = "force"
    override val description: String
        get() = "Force another player into or out of spec."
    override val syntax: String
        get() = "/spec force <player>"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        Bukkit.getLogger().info("executed subcommand force")
    }
}
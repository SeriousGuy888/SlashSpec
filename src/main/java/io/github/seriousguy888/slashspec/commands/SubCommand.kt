package io.github.seriousguy888.slashspec.commands

import org.bukkit.command.CommandSender

abstract class SubCommand {
    abstract val name: String
    abstract val description: String
    abstract val syntax: String

    abstract fun execute(sender: CommandSender, args: Array<out String>)
}
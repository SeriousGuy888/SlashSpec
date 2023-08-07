package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import io.github.seriousguy888.slashspec.SpecToggleDirection
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class SpecForceCommand(private val plugin: SlashSpec) : SubCommand() {
    override val name: String
        get() = "force"
    override val description: String
        get() = "Force another player into or out of spec."
    override val syntax: String
        get() = "/spec force <player> [in/out]"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("slashspec.admin.force")) {
            sender.sendMessage(Component.text("Insufficient permissions.", NamedTextColor.RED))
            return
        }

        // if the <player> arg has not been specified
        if (args.size <= 1) {
            sender.sendMessage(syntax)
            return
        }

        val targetPlayer = Bukkit.getPlayer(args[1])
        if (targetPlayer == null) {
            sender.sendMessage(Component.text("Invalid player.", NamedTextColor.RED))
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

        val success = plugin.specPlayersManager.togglePlayer(targetPlayer, dir)

        if (success) {
            sender.sendMessage(Component.text(buildString {
                append("Forced ")
                append(targetPlayer.name)
                when (dir) {
                    SpecToggleDirection.INTO_SPEC -> append(" into spec.")
                    SpecToggleDirection.OUT_OF_SPEC -> append(" out of spec.")
                    SpecToggleDirection.TOGGLE -> append(" to toggle spec.")
                }
            }, NamedTextColor.AQUA))
        } else {
            sender.sendMessage(Component.text(
                    "Command did not change anything.",
                    NamedTextColor.RED))
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 2) { // if user is on the <player> arg
            return Bukkit.getOnlinePlayers()
                    .map { it.name }
                    .filter { it.startsWith(args[1]) }
        }
        if (args.size == 3) { // if user is on the [in/out] arg
            return listOf("in", "out")
                    .filter { it.startsWith(args[2]) }
        }

        // after all that, just don't provide any suggestions
        // overrides the default thing of suggesting playernames
        return listOf()
    }
}

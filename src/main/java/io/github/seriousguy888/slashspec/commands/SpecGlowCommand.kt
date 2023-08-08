package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpecGlowCommand(private val plugin: SlashSpec) : SubCommand() {
    override val name: String
        get() = "glow"
    override val description: String
        get() = "Make yourself visible to other players while you are in spec. " +
                "Useful when you are trying to show someone something."
    override val syntax: String
        get() = "/spec glow [player]"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (!sender.hasPermission("slashspec.glow"))
            sender.sendMessage(Component.text("Insufficient permissions.", NamedTextColor.RED))

        var player = sender as Player
        if (args.size >= 2) {
            if (!sender.hasPermission("slashspec.glow.others"))
                sender.sendMessage(Component.text("Insufficient permissions.", NamedTextColor.RED))

            val specifiedPlayer = Bukkit.getPlayer(args[1])
            if (specifiedPlayer == null) {
                sender.sendMessage(Component.text("Invalid player.", NamedTextColor.RED))
                return
            }

            player = specifiedPlayer
        }

        val isForcingOther = player != sender

        val result = plugin.playerManager.toggleGlow(player)

        if (result == null) {
            sender.sendMessage(Component.text(
                    if (isForcingOther) "Player is not currently in spec."
                    else "You are not currently in spec.",
                    NamedTextColor.RED))
            return
        }

        sender.sendMessage(Component.text(
                buildString {
                    append(
                            if (isForcingOther) "Player is "
                            else "You are ")
                    append(
                            if (result) "now "
                            else "no longer "
                    )
                    append("glowing in spec.")
                },
                NamedTextColor.AQUA
        ))
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }
}
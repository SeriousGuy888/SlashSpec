package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ComponentBuilder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SpecTeleportCommand(private val plugin: SlashSpec) : SubCommand() {
    override val name: String
        get() = "tp"
    override val syntax: String
        get() = "/spec tp <player>"
    override val permission: String
        get() = "slashspec.teleport"

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("The console may not teleport to a player.")
            return
        }

        if (!plugin.playerManager.isPlayerInSpec(sender)) {
            sender.spigot()
                .sendMessage(
                    *ComponentBuilder("You must be in spec to use this.")
                        .color(ChatColor.RED)
                        .create()
                )
            return
        }

        if (args.size < 2) {
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
        if (targetPlayer == sender) {
            sender.spigot().sendMessage(
                *ComponentBuilder("No need to teleport to yourself.")
                    .color(ChatColor.RED)
                    .create()
            )
            return
        }
        if (!plugin.playerPrefsManager.get(targetPlayer).isTeleportableTo) {
            sender.spigot().sendMessage(
                *ComponentBuilder("This player has spec tp disabled.")
                    .color(ChatColor.RED)
                    .create()
            )
            return
        }

        sender.teleport(targetPlayer)
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String>? {
        if (args.size == 2) {
            return plugin.tabCompletionUtil.getPlayerNames(args[1])
        }

        return null
    }
}
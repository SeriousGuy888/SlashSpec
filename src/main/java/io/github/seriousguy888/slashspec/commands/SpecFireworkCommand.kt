package io.github.seriousguy888.slashspec.commands

import io.github.seriousguy888.slashspec.SlashSpec
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.command.CommandSender
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.metadata.FixedMetadataValue
import kotlin.math.max
import kotlin.math.min

class SpecFireworkCommand(private val plugin: SlashSpec) : SubCommand() {
    override val name: String
        get() = "firework"
    override val syntax: String
        get() = "/spec firework [<colour>] [<flight duration>/instant]"
    override val permission: String
        get() = "slashspec.firework"

    data class FwCol(
            val name: String,
            val hexInt: Int
    )

    private val fwCols = listOf(
            FwCol("red", 0xff0000),
            FwCol("orange", 0xff8800),
            FwCol("yellow", 0xffff00),
            FwCol("green", 0x00ff00),
            FwCol("cyan", 0x00ffff),
            FwCol("blue", 0x0000ff),
            FwCol("purple", 0x9370db),
            FwCol("white", 0xffffff),
            FwCol("black", 0x000000),
    )
    private val maxFlightDuration = 5

    override fun execute(sender: CommandSender, args: Array<out String>) {
        if (sender !is Player) {
            sender.sendMessage("Only players can summon fireworks.")
            return
        }

        var colour = Color.RED
        if (args.size >= 2) {
            val fwCol = fwCols.firstOrNull { it.name.equals(args[1], true) }
            if (fwCol == null) {
                sender.sendMessage(Component
                        .text("Valid colours: " + fwCols.joinToString(", ") { it.name })
                        .color(NamedTextColor.RED))
                return
            }
            colour = Color.fromRGB(fwCol.hexInt)
        }

        var isInstant = true
        var flightDuration = 2
        if (args.size >= 3) {
            val durationArg = args[2]
            if (durationArg.equals("instant", true)) {
                isInstant = true
            } else {
                isInstant = false
                val dur = durationArg.toIntOrNull()
                if (dur == null) {
                    sender.sendMessage(syntax)
                    return
                }

                flightDuration = max(1, min(maxFlightDuration, dur))
            }
        }

        val firework = sender.world.spawn(sender.eyeLocation, Firework::class.java)
        val meta = firework.fireworkMeta
        meta.addEffect(FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL)
                .withColor(colour)
                .build())
        meta.power = flightDuration - 1
        firework.fireworkMeta = meta

        // metadata checked for in listeners to cancel damage
        firework.setMetadata("no_damage_firework", FixedMetadataValue(plugin, true))

        // immediately detonate firework
        if (isInstant) {
            firework.detonate()
        }
    }

    override fun tabComplete(sender: CommandSender, args: Array<out String>): List<String> {
        if (args.size == 2) {
            return plugin.tabCompletionUtil.getCompletions(args[1], fwCols.map { it.name })
        }

        if (args.size == 3) {
            val flightDurations = (1..maxFlightDuration).map { it.toString() }.toMutableList()
            flightDurations.add("instant")
            return plugin.tabCompletionUtil.getCompletions(args[2], flightDurations)
        }

        return listOf()
    }
}
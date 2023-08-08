package io.github.seriousguy888.slashspec.particles

import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.entity.Player

class ParticlePlayer(private val plugin: SlashSpec) {
    private val dustOptions = Particle.DustOptions(Color.WHITE, 3f)

    fun playSpecParticle(player: Player) {
        val world = player.world
        world.spawnParticle(Particle.REDSTONE, player.location, 25, dustOptions)
    }
}
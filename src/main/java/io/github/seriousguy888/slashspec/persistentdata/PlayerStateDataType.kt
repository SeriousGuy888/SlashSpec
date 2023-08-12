package io.github.seriousguy888.slashspec.persistentdata

import io.github.seriousguy888.slashspec.SlashSpec
import io.github.seriousguy888.slashspec.state.PlayerState
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

class PlayerStateDataType(private val plugin: SlashSpec) : PersistentDataType<String, PlayerState> {
    override fun getPrimitiveType(): Class<String> {
        return String::class.java
    }

    override fun getComplexType(): Class<PlayerState> {
        return PlayerState::class.java
    }

    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): PlayerState {
        return PlayerState.fromJson(primitive, plugin)
    }

    override fun toPrimitive(complex: PlayerState, context: PersistentDataAdapterContext): String {
        return complex.toJson()
    }
}
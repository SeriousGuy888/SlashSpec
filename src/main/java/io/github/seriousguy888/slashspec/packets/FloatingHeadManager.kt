package io.github.seriousguy888.slashspec.packets

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.FieldAccessException
import com.comphenix.protocol.wrappers.BukkitConverters
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import io.github.seriousguy888.slashspec.SlashSpec
import io.github.seriousguy888.slashspec.packets.wrappers.WrapperPlayServerEntityMetadata
import io.github.seriousguy888.slashspec.packets.wrappers.WrapperPlayServerEntityTeleport
import io.github.seriousguy888.slashspec.packets.wrappers.WrapperPlayServerSpawnEntity
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.InvocationTargetException
import java.util.*


class FloatingHeadManager(private val plugin: SlashSpec) {
    private val floatingHeadMap = HashMap<UUID, FloatingHead>()
    private val visibilityRange = 32.0

    private val isProtocolLibInstalled = plugin.isProtocolLibInstalled()
    private var isProtocolLibWorking = false

    // fallback particles if protocollib is not installed
    private val dustOptions = Particle.DustOptions(Color.WHITE, 1f)

    data class FloatingHead(
        val entityId: Int,
        val uuid: UUID,
        var visibleTo: HashSet<Player>
    )

    fun displayHead(player: Player) {
        // https://www.spigotmc.org/threads/371934/
        // https://github.com/dmulloy2/PacketWrapper/
        // https://wiki.vg/Protocol#Spawn_Entity

        val isInGhostMode = plugin.playerPrefsManager.get(player).isGhostMode
        if (isInGhostMode)
            return

        val alreadyExists = floatingHeadMap.containsKey(player.uniqueId)

        // If player is spectating from another entity's perspective
        if (player.spectatorTarget != null) {
            // Don't display floating head because it can be annoying, especially
            // for the player you are spectating if you are spectating a player.
            // They would be unable to see anything because your floating head would
            // be in the way.
            if (alreadyExists) {
                removeFloatingHead(player)
            }
            return
        }


        // Get all nearby players in range who are in spectator mode.
        // Does not include the spectator themselves.
        val nearbyPlayers = player
            .getNearbyEntities(visibilityRange, visibilityRange, visibilityRange)
            .filterIsInstance<Player>()
            .filter { it != player && it.gameMode != GameMode.SPECTATOR }


        // If ProtocolLib is not installed, don't bother with the floating head stuff, and
        // instead, just play some particle effects in its place.
        //
        // Or, if config.yml says not to use the floating head feature, then use the
        // particles and return.
        if (!isProtocolLibInstalled || !plugin.configReader.shouldUseFloatingHead || !isProtocolLibWorking) {
            // Spawn particles for everyone nearby except the spectator.
            displayParticlesInstead(player.eyeLocation, nearbyPlayers)
            return
        }

        val floatingHead =
            if (alreadyExists)
                floatingHeadMap[player.uniqueId]!!
            else
                FloatingHead(
                    entityId = (Math.random() * Integer.MAX_VALUE).toInt(),
                    uuid = UUID.randomUUID(),
                    visibleTo = hashSetOf()
                )


        try {
            val spawnPacket = createSpawnPacket(player, floatingHead)
            val teleportPacket = createTeleportPacket(player, floatingHead)
            val metadataPacket = createMetadataPacket(player, floatingHead)

            val protocolManager = ProtocolLibrary.getProtocolManager()
            nearbyPlayers.forEach {
                val playerIsNewViewer = !floatingHead.visibleTo.contains(it) && nearbyPlayers.contains(it)

                try {
                    if (playerIsNewViewer) {
                        spawnPacket.sendPacket(it)
                    }
                    teleportPacket.sendPacket(it)
                    metadataPacket.sendPacket(it)
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                }
            }

            val destroyPacket = createDestroyPacket(floatingHead)
            floatingHead.visibleTo
                .filter { !nearbyPlayers.contains(it) }
                .forEach {
                    floatingHead.visibleTo.remove(it)
                    try {
                        protocolManager.sendServerPacket(it, destroyPacket)
                    } catch (e: InvocationTargetException) {
                        e.printStackTrace()
                    }
                }

            floatingHead.visibleTo.addAll(nearbyPlayers)
            floatingHeadMap[player.uniqueId] = floatingHead
        } catch (e: FieldAccessException) {
            isProtocolLibWorking = false
            plugin.logger.severe(e.stackTraceToString())
            plugin.logger.warning(
                "ProtocolLib is not working properly. " +
                        "SlashSpec will display particles instead of floating heads."
            )
        }
    }

    private fun displayParticlesInstead(location: Location, seeingPlayers: List<Player>) {
        seeingPlayers.forEach {
            it.spawnParticle(
                Particle.DUST,
                location,
                25,
                dustOptions
            )
        }
    }

    fun removeFloatingHead(player: Player) {
        if (!isProtocolLibInstalled)
            return

        if (!floatingHeadMap.containsKey(player.uniqueId))
            return

        val floatingHead = floatingHeadMap[player.uniqueId] ?: return
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val destroyPacket = createDestroyPacket(floatingHead)

        floatingHead.visibleTo.forEach {
            try {
                protocolManager.sendServerPacket(it, destroyPacket)
//                Bukkit.getLogger().info("Sent destroy packet for " + player.name + " to " + it.name)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }

        floatingHeadMap.remove(player.uniqueId)
    }

    private fun createSpawnPacket(headOwner: Player, floatingHead: FloatingHead): WrapperPlayServerSpawnEntity {
        val packet = WrapperPlayServerSpawnEntity()

        packet.uuid = floatingHead.uuid
        packet.type = EntityType.ITEM_DISPLAY
        packet.id = floatingHead.entityId
        packet.x = headOwner.eyeLocation.x
        packet.y = headOwner.eyeLocation.y + 0.25
        packet.z = headOwner.eyeLocation.z

        return packet
    }

    private fun createTeleportPacket(headOwner: Player, floatingHead: FloatingHead): WrapperPlayServerEntityTeleport {
        val packet = WrapperPlayServerEntityTeleport()

        packet.xRot = (-headOwner.location.pitch * 256f / 360f).toInt().toByte()
        packet.yRot = (headOwner.location.yaw * 256f / 360f + 128).toInt().toByte()
//        packet.xRot = (-headOwner.location.pitch * 256f / 360f).toInt().toByte()
//        packet.yRot = (headOwner.location.yaw * 256f / 360f + 128).toInt().toByte()
        packet.id = floatingHead.entityId
        packet.x = headOwner.eyeLocation.x
        packet.y = headOwner.eyeLocation.y + 0.25
        packet.z = headOwner.eyeLocation.z

        return packet
    }

    private fun createMetadataPacket(headOwner: Player, floatingHead: FloatingHead): WrapperPlayServerEntityMetadata {
        // https://wiki.vg/Entity_metadata#Entity_Metadata_Format

        val packet = WrapperPlayServerEntityMetadata()

        val nameOpt = Optional.of(WrappedChatComponent.fromJson(headOwner.displayName).handle)

        val head = ItemStack(Material.PLAYER_HEAD)
        val headMeta = head.itemMeta as SkullMeta
        headMeta.owningPlayer = headOwner
        head.itemMeta = headMeta

        // Remember to check these index numbers. It seems they might change pretty often between versions.
        val invisFlag = WrappedDataValue(
            0,
            WrappedDataWatcher.Registry.get(java.lang.Byte::class.java),
            (0x20).toByte()
        ) // 0x20 - invis; 0x40 - glowing
        val customName = WrappedDataValue(
            2,
            WrappedDataWatcher.Registry.getChatComponentSerializer(true),
            nameOpt
        )
        val customNameVisible = WrappedDataValue(
            3,
            WrappedDataWatcher.Registry.get(java.lang.Boolean::class.java),
            true
        )
        val displayedItem = WrappedDataValue(
            23, // Changed from 22 to make it work with 1.20.2
            WrappedDataWatcher.Registry.getItemStackSerializer(false),
            BukkitConverters.getItemStackConverter().getGeneric(head)
        ) // displayed item
        val displayType = WrappedDataValue(
            24, // Changed from 23 to make it work with 1.20.2.
            WrappedDataWatcher.Registry.get(java.lang.Byte::class.java),
            5.toByte()
        ) // 5 - display type head

        val metadata = listOf(
            invisFlag,
            customName,
            customNameVisible,
            displayedItem,
            displayType,
        )

        packet.id = floatingHead.entityId
        packet.packedItems = metadata

        return packet
    }

    private fun createDestroyPacket(floatingHead: FloatingHead): PacketContainer {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY)
        val eIds = mutableListOf(floatingHead.entityId)
        packet.intLists.write(0, eIds)

        return packet
    }
}

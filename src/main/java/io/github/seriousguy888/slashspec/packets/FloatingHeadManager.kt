package io.github.seriousguy888.slashspec.packets

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.*
import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.InvocationTargetException
import java.util.*


class FloatingHeadManager(private val plugin: SlashSpec) {
    private val floatingHeadMap = HashMap<Player, FloatingHead>()
    private val visibilityRange = 32.0

    private val isProtocolLibInstalled = plugin.isProtocolLibInstalled()

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

        val alreadyExists = floatingHeadMap.containsKey(player)

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
            .filter { it.gameMode != GameMode.SPECTATOR }


        // If ProtocolLib is not installed, don't bother with the floating head stuff, and
        // instead, just play some particle effects in its place.
        //
        // Or, if config.yml says not to use the floating head feature, then use the
        // particles and return.
        if (!isProtocolLibInstalled || !plugin.configReader.shouldUseFloatingHead) {
            // Spawn particles for everyone nearby except the spectator.
            nearbyPlayers.forEach {
                it.spawnParticle(
                    Particle.REDSTONE,
                    player.eyeLocation,
                    25,
                    dustOptions
                )
            }

            return
        }

        val floatingHead =
            if (alreadyExists)
                floatingHeadMap[player]!!
            else
                FloatingHead(
                    entityId = (Math.random() * Integer.MAX_VALUE).toInt(),
                    uuid = UUID.randomUUID(),
                    visibleTo = hashSetOf()
                )

        val spawnPacket = createPositionPacket(true, player, floatingHead)
        val teleportPacket = createPositionPacket(false, player, floatingHead)
        val metadataPacket = createMetadataPacket(player, floatingHead)

        val protocolManager = ProtocolLibrary.getProtocolManager()
        nearbyPlayers.forEach {
            val playerIsNewViewer = !floatingHead.visibleTo.contains(it) && nearbyPlayers.contains(it)

            try {
                if (playerIsNewViewer) {
                    protocolManager.sendServerPacket(it, spawnPacket)
                }
                protocolManager.sendServerPacket(it, teleportPacket)
                protocolManager.sendServerPacket(it, metadataPacket)
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
        floatingHeadMap[player] = floatingHead
    }

    fun removeFloatingHead(player: Player) {
        if (!isProtocolLibInstalled)
            return

        if (!floatingHeadMap.containsKey(player))
            return

        val floatingHead = floatingHeadMap[player] ?: return
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val destroyPacket = createDestroyPacket(floatingHead)

        floatingHead.visibleTo.forEach {
            try {
                protocolManager.sendServerPacket(it, destroyPacket)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }

        floatingHeadMap.remove(player)
    }

    private fun createPositionPacket(
        isSpawnPacket: Boolean,
        headOwner: Player,
        floatingHead: FloatingHead
    ): PacketContainer {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val spawnOrTpPacket: PacketContainer
        if (isSpawnPacket) {
            spawnOrTpPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY)
            spawnOrTpPacket.uuiDs.write(0, floatingHead.uuid)
            spawnOrTpPacket.entityTypeModifier.write(0, EntityType.ITEM_DISPLAY)
        } else {
            spawnOrTpPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT)

            // https://www.spigotmc.org/threads/protocollib-named_entity_spawn-angle-field.280263/
            // https://www.desmos.com/calculator/3ge37avign
            spawnOrTpPacket.bytes.write(0, (headOwner.location.yaw * 256f / 360f + 128).toInt().toByte())
            spawnOrTpPacket.bytes.write(1, (-headOwner.location.pitch * 256f / 360f).toInt().toByte())
        }

        spawnOrTpPacket.integers.write(0, floatingHead.entityId)

        spawnOrTpPacket.doubles.write(0, headOwner.eyeLocation.x)
        spawnOrTpPacket.doubles.write(1, headOwner.eyeLocation.y + 0.25)
        spawnOrTpPacket.doubles.write(2, headOwner.eyeLocation.z)

        return spawnOrTpPacket
    }

    private fun createMetadataPacket(headOwner: Player, floatingHead: FloatingHead): PacketContainer? {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA)
        metadataPacket.integers.write(0, floatingHead.entityId)


        val nameOpt = try {
            // If using PaperMC (ie: the teamDisplayName method is available), use that for the floating head
            // name tag.
            Optional.of(AdventureComponentConverter.fromComponent(headOwner.teamDisplayName()).handle)
        } catch (e: NoSuchMethodError) {
            // If not, (ie: probably using Spigot), use the regular old displayName method.
            @Suppress("DEPRECATION") Optional.of(WrappedChatComponent.fromJson(headOwner.displayName).handle)
        }

        val head = ItemStack(Material.PLAYER_HEAD)
        val headMeta = head.itemMeta as SkullMeta
        headMeta.owningPlayer = headOwner
        head.itemMeta = headMeta

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
            22,
            WrappedDataWatcher.Registry.getItemStackSerializer(false),
            BukkitConverters.getItemStackConverter().getGeneric(head)
        ) // displayed item
        val displayType = WrappedDataValue(
            23,
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
        metadataPacket.dataValueCollectionModifier.write(0, metadata)

        return metadataPacket
    }

    private fun createDestroyPacket(floatingHead: FloatingHead): PacketContainer {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY)
        val eIds = mutableListOf(floatingHead.entityId)
        packet.intLists.write(0, eIds)

        return packet
    }
}

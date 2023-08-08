package io.github.seriousguy888.slashspec.packets

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.BukkitConverters
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.InvocationTargetException
import java.util.*


class FloatingHeadManager(private val plugin: SlashSpec) {
    private val floatingHeadMap = HashMap<Player, FloatingHead>()

    private val visibilityRange = 32.0

    data class FloatingHead(
            val entityId: Int,
            val uuid: UUID,
            var visibleTo: HashSet<Player>
    )

    fun displayHead(player: Player) {
        // https://www.spigotmc.org/threads/371934/
        // https://github.com/dmulloy2/PacketWrapper/
        // https://wiki.vg/Protocol#Spawn_Entity

        val protocolManager = ProtocolLibrary.getProtocolManager()

        val nearbyPlayers = player
                .getNearbyEntities(visibilityRange, visibilityRange, visibilityRange)
                .filterIsInstance<Player>()
                .filter { it.gameMode != GameMode.SPECTATOR }


        val alreadyExists = floatingHeadMap.containsKey(player)
        val floatingHead =
                if (alreadyExists)
                    floatingHeadMap[player]!!
                else
                    FloatingHead(
                            entityId = (Math.random() * Integer.MAX_VALUE).toInt(),
                            uuid = UUID.randomUUID(),
                            visibleTo = hashSetOf())

        val spawnPacket = createPositionPacket(true, player, floatingHead)
        val teleportPacket = createPositionPacket(false, player, floatingHead)
        val metadataPacket = createMetadataPacket(player, floatingHead)

        nearbyPlayers.forEach {
            val playerIsNewViewer = !floatingHead.visibleTo.contains(it) && nearbyPlayers.contains(it)

            try {
                protocolManager.sendServerPacket(it, if (playerIsNewViewer) spawnPacket else teleportPacket)
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

    private fun createPositionPacket(isSpawnPacket: Boolean,
                                     headOwner: Player,
                                     floatingHead: FloatingHead): PacketContainer {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val spawnOrTpPacket: PacketContainer
        if (!isSpawnPacket) {
            spawnOrTpPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT)
        } else {
            spawnOrTpPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY)
            spawnOrTpPacket.uuiDs.write(0, floatingHead.uuid)
            spawnOrTpPacket.entityTypeModifier.write(0, EntityType.ITEM_DISPLAY)
        }

        spawnOrTpPacket.integers.write(0, floatingHead.entityId)

        spawnOrTpPacket.doubles.write(0, headOwner.eyeLocation.x)
        spawnOrTpPacket.doubles.write(1, headOwner.eyeLocation.y + 0.25)
        spawnOrTpPacket.doubles.write(2, headOwner.eyeLocation.z)

        // https://www.spigotmc.org/threads/protocollib-named_entity_spawn-angle-field.280263/
        // https://www.desmos.com/calculator/3ge37avign
        spawnOrTpPacket.bytes.write(0, (headOwner.location.yaw * 256f / 360f + 128).toInt().toByte())
        spawnOrTpPacket.bytes.write(1, (-headOwner.location.pitch * 256f / 360f).toInt().toByte())

        return spawnOrTpPacket
    }

    private fun createMetadataPacket(headOwner: Player, floatingHead: FloatingHead): PacketContainer? {
        val protocolManager = ProtocolLibrary.getProtocolManager()

        val metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA)
        metadataPacket.integers.write(0, floatingHead.entityId)


        val nameOpt = Optional.of(WrappedChatComponent.fromChatMessage(headOwner.name)[0].handle)

        val head = ItemStack(Material.PLAYER_HEAD)
        val headMeta = head.itemMeta as SkullMeta
        headMeta.owningPlayer = headOwner
        head.itemMeta = headMeta

        val invisFlag = WrappedDataValue(0,
                WrappedDataWatcher.Registry.get(java.lang.Byte::class.java),
                (0x20).toByte()) // 0x20 - invis; 0x40 - glowing
        val customName = WrappedDataValue(2,
                WrappedDataWatcher.Registry.getChatComponentSerializer(true),
                nameOpt)
        val customNameVisible = WrappedDataValue(3,
                WrappedDataWatcher.Registry.get(java.lang.Boolean::class.java),
                true)
        val displayedItem = WrappedDataValue(22,
                WrappedDataWatcher.Registry.getItemStackSerializer(false),
                BukkitConverters.getItemStackConverter().getGeneric(head)) // displayed item
        val displayType = WrappedDataValue(23,
                WrappedDataWatcher.Registry.get(java.lang.Byte::class.java),
                5.toByte()) // 5 - display type head

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

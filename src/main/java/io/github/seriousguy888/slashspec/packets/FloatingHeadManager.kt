package io.github.seriousguy888.slashspec.packets

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.comphenix.protocol.wrappers.Pair
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import io.github.seriousguy888.slashspec.SlashSpec
import org.bukkit.Material
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.joml.Vector3f
import java.lang.reflect.InvocationTargetException
import java.util.*


class FloatingHeadManager(private val plugin: SlashSpec) {
    private val floatingHeadMap = HashMap<Player, FloatingHead>()

    data class FloatingHead(
            val entityId: Int,
            val uuid: UUID,
    )

    fun displayHead(player: Player) {
        // https://www.spigotmc.org/threads/371934/
        // https://github.com/dmulloy2/PacketWrapper/
        // https://wiki.vg/Protocol#Spawn_Entity

        val protocolManager = ProtocolLibrary.getProtocolManager()

        val alreadyExists = floatingHeadMap.containsKey(player)
        val floatingHead =
                if (alreadyExists)
                    floatingHeadMap[player]!!
                else
                    FloatingHead(
                            entityId = (Math.random() * Integer.MAX_VALUE).toInt(),
                            uuid = UUID.randomUUID())
        val entityId = floatingHead.entityId
        val uuid = floatingHead.uuid


        val spawnOrTpPacket: PacketContainer
        if (alreadyExists) {
            spawnOrTpPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT)
        } else {
            spawnOrTpPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY)
            spawnOrTpPacket.uuiDs.write(0, uuid)
            spawnOrTpPacket.entityTypeModifier.write(0, EntityType.ARMOR_STAND)
        }

        spawnOrTpPacket.integers.write(0, entityId)

        spawnOrTpPacket.doubles.write(0, player.location.x)
        spawnOrTpPacket.doubles.write(1, player.location.y)
        spawnOrTpPacket.doubles.write(2, player.location.z)

        // https://www.spigotmc.org/threads/protocollib-named_entity_spawn-angle-field.280263/
        spawnOrTpPacket.bytes.write(0, (player.location.yaw / 360.0 * 256).toInt().toByte())
        spawnOrTpPacket.bytes.write(1, (player.location.pitch / 360.0 * 256).toInt().toByte())


        val metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA)
        metadataPacket.integers.write(0, entityId)


        val nameOpt = Optional.of(WrappedChatComponent.fromChatMessage(player.name)[0].handle)

        val invisAndGlowing = WrappedDataValue(0,
                WrappedDataWatcher.Registry.get(java.lang.Byte::class.java),
                (0x20).toByte()) // 0x20 - invis; 0x40 - glowing
        val customName = WrappedDataValue(2,
                WrappedDataWatcher.Registry.getChatComponentSerializer(true),
                nameOpt)
        val customNameVisible = WrappedDataValue(3,
                WrappedDataWatcher.Registry.get(java.lang.Boolean::class.java),
                true)
        val headRotations = WrappedDataValue(16,
                WrappedDataWatcher.Registry.get(Vector3f::class.java),
                Vector3f(player.location.pitch, player.location.yaw, 0f))

        val metadata = listOf(invisAndGlowing, customName, customNameVisible, headRotations)
        metadataPacket.dataValueCollectionModifier.write(0, metadata)


        val equipmentPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT)
        equipmentPacket.integers.write(0, entityId)

        val head = ItemStack(Material.PLAYER_HEAD)
        val headMeta = head.itemMeta as SkullMeta
        headMeta.owningPlayer = player
        head.itemMeta = headMeta

        val equipmentData: List<Pair<ItemSlot, ItemStack>> = listOf(Pair(ItemSlot.HEAD, head))

        equipmentPacket.slotStackPairLists.write(0, equipmentData)


        val nearby = player.getNearbyEntities(32.0, 32.0, 32.0)
        nearby.add(player)
        nearby.forEach {
            if (it !is Player)
                return@forEach

            try {
                protocolManager.sendServerPacket(it, spawnOrTpPacket)
                protocolManager.sendServerPacket(it, metadataPacket)
                protocolManager.sendServerPacket(it, equipmentPacket)
            } catch (e: InvocationTargetException) {
                e.printStackTrace()
            }
        }

        if (!alreadyExists) {
            floatingHeadMap[player] = floatingHead
        }
    }
}

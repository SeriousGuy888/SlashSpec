package io.github.seriousguy888.slashspec.packets.wrappers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.util.Vector;

public class WrapperPlayServerEntityTeleport extends AbstractPacket {

    /**
     * The packet type that is wrapped by this wrapper.
     */
    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_TELEPORT;

    /**
     * Constructs a new wrapper and initialize it with a packet handle with default values
     */
    public WrapperPlayServerEntityTeleport() {
        super(TYPE);
    }

    public WrapperPlayServerEntityTeleport(PacketContainer packet) {
        super(packet, TYPE);
    }

    /**
     * Retrieves the value of field 'id'
     *
     * @return 'id'
     */
    public int getId() {
        return this.handle.getIntegers().read(0);
    }

    /**
     * Sets the value of field 'id'
     *
     * @param value New value for field 'id'
     */
    public void setId(int value) {
        this.handle.getIntegers().write(0, value);
    }

    public void setLocation(double x, double y, double z) {
        // Stopgap fix: https://github.com/dmulloy2/ProtocolLib/issues/3341#issuecomment-2580832502

        InternalStructure is = this.handle.getStructures().getValues().getFirst();
        is.getVectors()
                .write(0, new Vector(x, y, z)) // position
                .write(1, new Vector(0, 0, 0)); // velocity?
    }

    public void setRotation(float yaw, float pitch) {
        // Stopgap fix: https://github.com/dmulloy2/ProtocolLib/issues/3341#issuecomment-2580832502

        InternalStructure is = this.handle.getStructures().getValues().getFirst();
        is.getFloat()
                .write(0, yaw)
                .write(1, pitch);
    }
}

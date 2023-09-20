package me.binary.turretmod.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class UpdateFallingBlockMotionPacket {
    private final int pId;
    private final double motionX;
    private final double motionY;
    private final double motionZ;

    public UpdateFallingBlockMotionPacket(int pId, double motionX, double motionY, double motionZ) {
        this.pId = pId;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
    }

    public UpdateFallingBlockMotionPacket(FriendlyByteBuf buffer) {
        this.pId = buffer.readInt();
        this.motionX = buffer.readDouble();
        this.motionY = buffer.readDouble();
        this.motionZ = buffer.readDouble();
    }

    public void toBytes(FriendlyByteBuf buffer) {
        buffer.writeInt(pId);
        buffer.writeDouble(motionX);
        buffer.writeDouble(motionY);
        buffer.writeDouble(motionZ);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level.getEntity(pId) instanceof FallingBlockEntity fallingBlockEntity) {
                fallingBlockEntity.setDeltaMovement(motionX,motionY,motionZ);
            }
        });
        return true;
    }
}
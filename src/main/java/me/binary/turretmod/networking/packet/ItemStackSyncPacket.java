package me.binary.turretmod.networking.packet;

import me.binary.turretmod.block.entity.FireFactoryEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class ItemStackSyncPacket {
    private final ItemStackHandler itemHandler;
    private final BlockPos pos;

    public ItemStackSyncPacket(ItemStackHandler itemHandler, BlockPos pos) {
        this.itemHandler = itemHandler;
        this.pos = pos;
    }

    public ItemStackSyncPacket(FriendlyByteBuf buf) {
        List<ItemStack> list = buf.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
        itemHandler = new ItemStackHandler(list.size());
        for (int i = 0; i < list.size(); i++) {
            itemHandler.insertItem(i, list.get(i), false);
        }
        this.pos = buf.readBlockPos();
    }

    public void toBytes(FriendlyByteBuf buf) {
        Collection<ItemStack> collection = new ArrayList<>();
        for(int i = 0; i < itemHandler.getSlots(); i++) {
            collection.add(itemHandler.getStackInSlot(i));
        }

        buf.writeCollection(collection, FriendlyByteBuf::writeItem);
        buf.writeBlockPos(pos);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof FireFactoryEntity blockEntity) {
                blockEntity.setHandler(this.itemHandler);
            }
        });
        return true;
    }
}

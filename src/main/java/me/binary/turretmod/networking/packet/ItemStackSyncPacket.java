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
    private final ItemStackHandler itemStackHandler;
    private final BlockPos pos;
    private final int progress;
    private final int maxProgress;

    public ItemStackSyncPacket(ItemStackHandler itemStackHandler, BlockPos pos, int progress, int maxProgress) {
        this.itemStackHandler = itemStackHandler;
        this.pos = pos;
        this.progress = progress;
        this.maxProgress = maxProgress;
    }

    public ItemStackSyncPacket(FriendlyByteBuf buf) {
        List<ItemStack> collection = buf.readCollection(ArrayList::new, FriendlyByteBuf::readItem);
        itemStackHandler = new ItemStackHandler(collection.size());
        for (int i = 0; i < collection.size(); i++) {
            itemStackHandler.insertItem(i, collection.get(i), false);
        }

        this.pos = buf.readBlockPos();
        this.progress = buf.readInt();
        this.maxProgress = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        Collection<ItemStack> list = new ArrayList<>();
        for(int i = 0; i < itemStackHandler.getSlots(); i++) {
            list.add(itemStackHandler.getStackInSlot(i));
        }

        buf.writeCollection(list, FriendlyByteBuf::writeItem);
        buf.writeBlockPos(pos);
        buf.writeInt(progress);
        buf.writeInt(maxProgress);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if(Minecraft.getInstance().level.getBlockEntity(pos) instanceof FireFactoryEntity blockEntity) {
                blockEntity.setHandler(this.itemStackHandler);
                blockEntity.setProgress(this.progress);
                blockEntity.setMaxProgress(this.maxProgress);
            }
        });
        return true;
    }
}
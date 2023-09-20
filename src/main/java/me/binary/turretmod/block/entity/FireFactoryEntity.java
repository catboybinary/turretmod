package me.binary.turretmod.block.entity;

import me.binary.turretmod.networking.ModMessages;
import me.binary.turretmod.networking.packet.ItemStackSyncPacket;
import me.binary.turretmod.networking.packet.UpdateFallingBlockMotionPacket;
import me.binary.turretmod.screen.FireFactoryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FireFactoryEntity extends BlockEntity implements MenuProvider {
    public float degrees = 0;
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                ModMessages.sendToClients(new ItemStackSyncPacket(this, worldPosition, progress, maxProgress));
            }
        }
    };
    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 20;
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FireFactoryMenu(id, inventory, this, this.data);
    }
    //Saving and loading NBT to and from chunks
    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
        setChanged();
    }
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("Inventory", itemHandler.serializeNBT());
        nbt.putInt("progress", progress);
        super.saveAdditional(nbt);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("Inventory"));
        progress = nbt.getInt("progress");
    }
    public FireFactoryEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.FIRE_FACTORY.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> FireFactoryEntity.this.progress;
                    case 1 -> FireFactoryEntity.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> FireFactoryEntity.this.progress = pValue;
                    case 1 -> FireFactoryEntity.this.maxProgress = pValue;
                };
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    public void dropContents() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }
    @Override
    public Component getDisplayName() {
        return Component.literal("\uD83D\uDD25 Fire Factory \uD83D\uDD25");
    }
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FireFactoryEntity e) {
        if(level.isClientSide()) return;
        ModMessages.sendToClients(new ItemStackSyncPacket(e.itemHandler, blockPos, e.progress, e.maxProgress));
        if(!e.hasFuel()) {
            e.resetProgress();
            setChanged(level, blockPos, blockState);
            return;
        }
        ItemStack fuel = e.itemHandler.getStackInSlot(0);
        if(e.progress == -1) {
            e.maxProgress = ForgeHooks.getBurnTime(fuel,RecipeType.SMELTING);
            e.progress = 0;
        }
        if(e.progress < e.maxProgress && e.progress>=0) {
            System.out.println(e.progress);
            e.progress++;
            setChanged(level, blockPos, blockState);
        } else if (e.progress >= e.maxProgress) {
            //Spread out the fire
            //System.out.println("!!!FIRE!!!");
            fuel.setCount(fuel.getCount()-1);
            e.itemHandler.setStackInSlot(0,fuel);
            for(int i = 0;i<e.maxProgress/50;i++) {
                FallingBlockEntity fire = FallingBlockEntity.fall(level,blockPos.above(2), Blocks.FIRE.defaultBlockState());
                Vec3 motion = new Vec3(Math.random()-0.5,Math.random(),Math.random()-0.5);
                fire.setDeltaMovement(motion);
                ModMessages.sendToClients(new UpdateFallingBlockMotionPacket(
                        fire.getId(),motion.x, motion.y, motion.z));
                //level.addFreshEntity(fire);
            }
            e.progress = -1;
        }
    }

    private boolean hasFuel() {
        return ForgeHooks.getBurnTime(itemHandler.getStackInSlot(0),RecipeType.SMELTING) > 0;
    }
    private void resetProgress() {this.progress = -1;}
    public ItemStack getRenderStack() {return itemHandler.getStackInSlot(0);}

    public int getProgress() {return progress;}
    public int getMaxProgress() {return maxProgress;}
    public void setProgress(int value) {progress = value;}
    public void setMaxProgress(int value) {maxProgress = value;}
    public void setHandler(ItemStackHandler itemStackHandler) {
        for(int i = 0; i < itemStackHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i,itemStackHandler.getStackInSlot(i));
        }
    }
}

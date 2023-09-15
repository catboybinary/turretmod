package me.binary.turretmod.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class TurretEntity extends BlockEntity {

    private static final Random random = new Random();
    private int timer = 0;
    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            lazyItemHandler.cast();
        }
        return super.getCapability(cap);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("Inventory", itemHandler.serializeNBT());
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("Inventory"));
    }

    public TurretEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET.get(), pos, state);
    }

    public void dropContents() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    public void setItem(ItemStack item) {
        dropContents();
        itemHandler.setStackInSlot(0, item);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, TurretEntity e) {
        if (e.itemHandler.getStackInSlot(0).getCount() > 0 && e.itemHandler.getStackInSlot(0).is(Items.ARROW)) {
            if (level.isClientSide) return;
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,new AABB(blockPos.subtract(new Vec3i(5,5,5)),blockPos.subtract(new Vec3i(-5,-5,-5))));
            if (entities.isEmpty()) return;
            e.timer = (e.timer + 1) % 20;
            if (e.timer>0) return;

            LivingEntity target = entities.get(random.nextInt(entities.size()));
            Projectile projectile = EntityType.ARROW.create(level);
            projectile.setPos(Vec3.atCenterOf(blockPos.above()));
            Vec3 targetPosition = target.position().add(0,target.getEyeHeight()/2,0);
            Vec3 blockPosition = Vec3.atCenterOf(blockPos.above());
            projectile.setDeltaMovement(targetPosition.subtract(blockPosition).normalize()
                    .add(0,targetPosition.distanceTo(blockPosition)/20,0));
            level.addFreshEntity(projectile);

            ItemStack n = e.itemHandler.getStackInSlot(0);
            n.setCount(n.getCount()-1);
            e.itemHandler.setStackInSlot(0,n);
        }
    }
}

package me.binary.turretmod.block.entity;

import me.binary.turretmod.util.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
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

    //Create a single slot inventory for the turret
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

    //Saving and loading NBT to and from chunks
    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }
    @Override
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("Inventory", itemHandler.serializeNBT());
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
    }

    public TurretEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TURRET.get(), pos, state);
    }

    //Drops the contents of the turret
    public void dropContents() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    //Sets the item for the turret to use
    public void setItem(ItemStack item) {
        dropContents();
        itemHandler.setStackInSlot(0, item);
    }

    //Runs every tick
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, TurretEntity e) {
        if (level.isClientSide) return;
        ItemStack item = e.itemHandler.getStackInSlot(0);
        if (item.getCount() > 0 && Maps.PROJECTILES.containsKey(item.getItem())) {
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class,new AABB(blockPos.subtract(new Vec3i(5,5,5)),blockPos.subtract(new Vec3i(-5,-5,-5))));
            if (entities.isEmpty()) return;
            // Shoots every second, counts ticks only when an entity is nearby
            e.timer = (e.timer + 1) % 20;
            if (e.timer>0) return;

            LivingEntity target = entities.get(random.nextInt(entities.size()));

            //Projectile settings
            Projectile projectile = (Projectile) Maps.PROJECTILES.get(item.getItem()).create(level);
            if (projectile instanceof Arrow) {
                ((Arrow) projectile).setEffectsFromItem(item);
            }
            projectile.setPos(Vec3.atCenterOf(blockPos.above()));

            //AI
            Vec3 targetPosition = target.position().add(0,target.getEyeHeight()/2,0);
            Vec3 blockPosition = Vec3.atCenterOf(blockPos.above());
            if (projectile instanceof SmallFireball) {
                projectile.setDeltaMovement(targetPosition.subtract(blockPosition).normalize());
            } else {
                projectile.setDeltaMovement(targetPosition.subtract(blockPosition).normalize()
                        .add(0,targetPosition.distanceTo(blockPosition)/25,0));
            }

            level.addFreshEntity(projectile);

            //Subtracts the item count after a shot
            ItemStack n = e.itemHandler.getStackInSlot(0);
            n.setCount(n.getCount()-1);
            e.itemHandler.setStackInSlot(0,n);
        }
    }
}

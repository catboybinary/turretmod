package me.binary.turretmod.block.entity;

import me.binary.turretmod.TurretMod;
import me.binary.turretmod.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, TurretMod.MODID);

    public static final RegistryObject<BlockEntityType<TurretEntity>> TURRET =
            BLOCK_ENTITIES.register("turret",()->
                    BlockEntityType.Builder.of(TurretEntity::new,
                            ModBlocks.TURRET_BLOCK.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

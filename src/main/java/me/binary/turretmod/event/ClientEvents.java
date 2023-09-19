package me.binary.turretmod.event;

import me.binary.turretmod.TurretMod;
import me.binary.turretmod.block.entity.ModBlockEntities;
import me.binary.turretmod.block.entity.renderer.FireFactoryEntityRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {
    @Mod.EventBusSubscriber(modid = TurretMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.FIRE_FACTORY.get(),
                    FireFactoryEntityRenderer::new);
        }
    }
}
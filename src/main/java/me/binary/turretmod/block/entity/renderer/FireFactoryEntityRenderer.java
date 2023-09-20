package me.binary.turretmod.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import me.binary.turretmod.block.entity.FireFactoryEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;

public class FireFactoryEntityRenderer implements BlockEntityRenderer<FireFactoryEntity> {
    public FireFactoryEntityRenderer(BlockEntityRendererProvider.Context context) {

    }
    @Override
    public void render(FireFactoryEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        Font font = Minecraft.getInstance().font;
        
        ItemStack itemStack = pBlockEntity.getRenderStack();
        pPoseStack.pushPose();
        pPoseStack.translate(0.5f,1.5f,0.5f);
        pPoseStack.scale(1f,1f,1f);
        pPoseStack.mulPose(Vector3f.YP.rotationDegrees(pBlockEntity.degrees));

        itemRenderer.renderStatic(itemStack, ItemTransforms.TransformType.GUI, getLightLevel(pBlockEntity.getLevel(),
                        pBlockEntity.getBlockPos().above()),
                OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, 1);
        pPoseStack.popPose();
        pPoseStack.pushPose();
        pPoseStack.translate(0.25f,2f,0.5f);
        pPoseStack.scale(0.01f,-0.01f,0.01f);
        font.drawInBatch(String.valueOf(pBlockEntity.getProgress()),0,0,-1,
                true,pPoseStack.last().pose(), pBufferSource,
                false, 16777215,
                getLightLevel(pBlockEntity.getLevel(),pBlockEntity.getBlockPos().above()),false);
        pPoseStack.popPose();
        pBlockEntity.degrees+=Minecraft.getInstance().getDeltaFrameTime()*10;
        if (pBlockEntity.degrees>360) {
            pBlockEntity.degrees = 0;
        }
    }
    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}

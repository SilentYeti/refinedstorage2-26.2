package com.refinedmods.refinedstorage.fabric.support.render;

import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;

import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.MOD_ID;
import static com.refinedmods.refinedstorage.common.util.IdentifierUtil.createIdentifier;

public final class RenderTypes {
    // MATRICES_PROJECTION_SNIPPET was removed; MATRICES_FOG_SNIPPET is the closest replacement
    // (same GLOBALS + MATRICES_PROJECTION bind groups, plus an unused fog uniform). It's already
    // built on top of GLOBALS_SNIPPET (see vanilla RenderPipelines.java), so passing both here
    // registered the 'Globals' bind group twice and failed pipeline compilation at runtime.
    private static final RenderPipeline DISK_LEDS_PIPELINE = RenderPipeline
        .builder(RenderPipelines.MATRICES_FOG_SNIPPET)
        .withDepthStencilState(DepthStencilState.DEFAULT)
        .withLocation(createIdentifier("pipeline/disk_leds"))
        .withVertexShader("core/position_color")
        .withFragmentShader("core/position_color")
        .withVertexBinding(0, DefaultVertexFormat.POSITION_COLOR)
        .withPrimitiveTopology(PrimitiveTopology.QUADS)
        .build();

    // RenderSetupBuilder#bufferSize was removed; buffer sizing is handled internally now.
    public static final RenderType DISK_LEDS = RenderType.create(
        MOD_ID + "_disk_leds",
        RenderSetup.builder(DISK_LEDS_PIPELINE).createRenderSetup()
    );

    private RenderTypes() {
    }
}

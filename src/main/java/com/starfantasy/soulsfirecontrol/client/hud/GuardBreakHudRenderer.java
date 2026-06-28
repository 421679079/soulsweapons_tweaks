package com.starfantasy.soulsfirecontrol.client.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.starfantasy.soulsfirecontrol.StarFantasySoulsFireControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = StarFantasySoulsFireControl.MOD_ID, value = Dist.CLIENT)
public final class GuardBreakHudRenderer {
    private static final ResourceLocation BEADS_TEXTURE =
            StarFantasySoulsFireControl.id("textures/gui/guard_break/guard_break_beads.png");
    private static final int BOSS_BAR_WIDTH = 182;
    private static final int TEXTURE_BEAD_SIZE = 32;
    private static final int DISPLAY_BEAD_SIZE = 16;
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;
    private static final int FULL_U = 0;
    private static final int EMPTY_U = 32;
    private static final int BASE_GAP = 2;
    private static final int MAX_ROW_WIDTH = 160;
    private static final int Y_OFFSET = 10;

    private GuardBreakHudRenderer() {
    }

    @SubscribeEvent
    public static void onBossEventProgress(CustomizeGuiOverlayEvent.BossEventProgress event) {
        GuardBreakHudClientState.HudState state =
                GuardBreakHudClientState.get(event.getBossEvent().getId());
        if (state == null) {
            return;
        }
        drawBeads(event.getGuiGraphics(), event.getX(), event.getY(), state);
        int rowHeight = Math.max(DISPLAY_BEAD_SIZE, 10);
        event.setIncrement(event.getIncrement() + rowHeight + 2);
    }

    private static void drawBeads(GuiGraphics guiGraphics, int bossBarX, int bossBarY,
                                  GuardBreakHudClientState.HudState state) {
        int requiredGuards = Math.max(1, state.requiredGuards());
        int gap = requiredGuards > 16 ? 2 : BASE_GAP;
        int rowWidth = requiredGuards * DISPLAY_BEAD_SIZE + Math.max(0, requiredGuards - 1) * gap;
        float scale = rowWidth > MAX_ROW_WIDTH ? (float) MAX_ROW_WIDTH / rowWidth : 1.0F;
        int scaledWidth = Math.round(rowWidth * scale);
        int x = bossBarX + (BOSS_BAR_WIDTH - scaledWidth) / 2;
        int y = bossBarY + Y_OFFSET;
        float textureToDisplayScale = (float) DISPLAY_BEAD_SIZE / TEXTURE_BEAD_SIZE;
        float totalScale = textureToDisplayScale * scale;
        int rawGap = Math.max(1, Math.round((float) gap / textureToDisplayScale));

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0F);
        pose.scale(totalScale, totalScale, 1.0F);
        for (int i = 0; i < requiredGuards; ++i) {
            int u = selectSpriteU(i, state);
            int beadX = i * (TEXTURE_BEAD_SIZE + rawGap);
            guiGraphics.blit(BEADS_TEXTURE, beadX, 0, (float) u, 0.0F,
                    TEXTURE_BEAD_SIZE, TEXTURE_BEAD_SIZE, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
        pose.popPose();
    }

    private static int selectSpriteU(int index, GuardBreakHudClientState.HudState state) {
        return index < state.guardCount() ? EMPTY_U : FULL_U;
    }
}

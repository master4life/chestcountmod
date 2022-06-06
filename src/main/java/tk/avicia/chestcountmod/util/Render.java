package tk.avicia.chestcountmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import tk.avicia.chestcountmod.ChestCountMod;

import javax.vecmath.Vector3f;
import java.awt.*;

public class Render {
    public static void highlightBlock(BlockPos blockpos, Color c, float partialTicks, boolean depth) {
        Entity viewing_from = Minecraft.getMinecraft().getRenderViewEntity();

        double x_fix = viewing_from.lastTickPosX + ((viewing_from.posX - viewing_from.lastTickPosX) * partialTicks);
        double y_fix = viewing_from.lastTickPosY + ((viewing_from.posY - viewing_from.lastTickPosY) * partialTicks);
        double z_fix = viewing_from.lastTickPosZ + ((viewing_from.posZ - viewing_from.lastTickPosZ) * partialTicks);

        GlStateManager.pushMatrix();

        GlStateManager.translate(-x_fix, -y_fix, -z_fix);

        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableTexture2D();

        if (!depth) {
            GlStateManager.disableDepth();
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GlStateManager.depthMask(false);
        }
        GlStateManager.color(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f, c.getAlpha() / 255.0f);

        GlStateManager.translate(blockpos.getX(), blockpos.getY(), blockpos.getZ());

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(0, 1, 0); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(1, 0, 1);
        GL11.glVertex3d(1, 0, 0);
        GL11.glVertex3d(1, 1, 0);
        GL11.glVertex3d(1, 1, 1);

        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(1, 0, 1);
        GL11.glVertex3d(1, 1, 1); // TOP LEFT / BOTTOM LEFT / TOP RIGHT/ BOTTOM RIGHT

        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(0, 1, 0);
        GL11.glVertex3d(1, 1, 0);
        GL11.glVertex3d(1, 0, 0);

        GL11.glVertex3d(0, 1, 0);
        GL11.glVertex3d(0, 1, 1);
        GL11.glVertex3d(1, 1, 1);
        GL11.glVertex3d(1, 1, 0);

        GL11.glVertex3d(0, 0, 1);
        GL11.glVertex3d(0, 0, 0);
        GL11.glVertex3d(1, 0, 0);
        GL11.glVertex3d(1, 0, 1);

        GL11.glEnd();

        if (!depth) {
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
        }
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }
    public static void drawTextAtWorld(String text, float x, float y, float z, int color, float scale, boolean increase, boolean renderBlackBox, boolean depth, float partialTicks) {
        float lScale = scale;

        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;

        Vector3f renderPos = getRenderPos(x, y, z, partialTicks);

        if (increase) {
            double distance = Math.sqrt(renderPos.x * renderPos.x + renderPos.y * renderPos.y + renderPos.z * renderPos.z);
            double multiplier = distance / 120f; //mobs only render ~120 blocks away
            lScale *= 0.45f * multiplier;
        }

        //GlStateManager.color(1f, 1f, 1f, 0.5f);
        GlStateManager.pushMatrix();
        GlStateManager.translate(renderPos.x, renderPos.y, renderPos.z);
        GlStateManager.rotate(-renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-lScale, -lScale, lScale);
        GlStateManager.disableLighting();
        if (depth) {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GlStateManager.disableDepth();
        }
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int textWidth = fontRenderer.getStringWidth(text);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();
        if (renderBlackBox) {
            double j = textWidth / 2;
            GlStateManager.disableTexture2D();
            worldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
            worldRenderer.pos(-j - 1, -1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(-j - 1, 8, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(j + 1, 8, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            worldRenderer.pos(j + 1, -1, 0.0).color(0.0f, 0.0f, 0.0f, 0.25f).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D();
        }

        //GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableBlend();
        fontRenderer.drawString(text, -textWidth / 2, 0, color);
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableLighting();

        if (depth)
            GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    public static Vector3f getRenderPos(float x, float y, float z, float partialTicks) {
        EntityPlayerSP sp = Minecraft.getMinecraft().player;
        return new Vector3f(
                x - (float) (sp.lastTickPosX + (sp.posX - sp.lastTickPosX) * partialTicks),
                y - (float) (sp.lastTickPosY + (sp.posY - sp.lastTickPosY) * partialTicks),
                z - (float) (sp.lastTickPosZ + (sp.posZ - sp.lastTickPosZ) * partialTicks)
        );
    }

}

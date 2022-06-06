package tk.avicia.chestcountmod;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import tk.avicia.chestcountmod.util.PotentialMythic;
import tk.avicia.chestcountmod.util.Render;

import java.awt.*;
import java.util.ArrayList;

public class DisplayChest {
    public RenderManager renderManager = ChestCountMod.getMC().getRenderManager();
    public ArrayList<JsonObject> listChest = ChestCountMod.getMythicData().getChests();

    @SubscribeEvent
    public void renderTick(RenderWorldLastEvent event) {
        if (ChestCountMod.getMC().fontRenderer == null || ChestCountMod.getMC().player == null || renderManager == null || renderManager.options == null || ChestCountMod.getMythicData().getChests() == null)
            return;

        // Cancel rendering when listChest doesnt exist yet
        if (listChest.size() <= 0)
            return;

        Entity viewEntity = ChestCountMod.getMC().getRenderViewEntity();
        // Cancels when player hasnt initialised yet.
        if (viewEntity == null)
            return;

        // Foreach all values from "Chests"
        for (JsonObject obj : this.listChest) {
            BlockPos pos = new BlockPos((double) obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());
            float tick = event.getPartialTicks();

            if (ChestCountMod.getMC().world.getBlockState(pos).getBlock() instanceof BlockContainer)
                Render.highlightBlock(pos, new Color(0, 255, 0, 70), tick, true);
            else
                Render.highlightBlock(pos, new Color( 255, 0, 0, 70), tick, true);

            // Selecting from Gui your distance between player & chest location.
            String distance = ChestCountMod.CONFIG.getConfig("distanceChest");
            int distanceChest = 1;

            if (distance.equalsIgnoreCase("Close"))
                distanceChest = 3;
            else if (distance.equalsIgnoreCase("Nearby"))
                distanceChest = 15;
            else if (distance.equalsIgnoreCase("Normal"))
                distanceChest = 50;
            else if (distance.equalsIgnoreCase("Far"))
                distanceChest = 100;

            int lvl = obj.get("lvl").getAsInt();
            int tier1 = obj.get("tier").getAsInt();

            if (ChestCountMod.CONFIG.getConfigBoolean("displayMythicOnChest") && viewEntity.getDistance(pos.getX(), pos.getY(), pos.getZ()) < 5.0d)
            {
                Render.drawTextAtWorld(TextFormatting.AQUA + "^ Mythic possibilities ^", pos.getX() + 0.5f, pos.getY() + 2.3f, pos.getZ() + 0.5f, 0xFFFFFF, 0.03F, false, true, false, event.getPartialTicks());
                String[] mythics = PotentialMythic.mythicType(lvl-2, lvl+2, tier1);

                for(int i = 0; i< mythics.length; i++)
                    Render.drawTextAtWorld(TextFormatting.DARK_PURPLE + mythics[i].replace( "_", " "), pos.getX() + 0.5f, pos.getY() + (float) (2.6f + (i-(0.7*i))), pos.getZ() + 0.5f, 0xFFFFFF, 0.03F, false, true, false, event.getPartialTicks());
            }

            if (distance.equalsIgnoreCase("Everywhere") || viewEntity.getDistance(pos.getX(), pos.getY(), pos.getZ()) < (double) distanceChest) {
                // Converts numeric to latain numbers.
                String tier = "";
                switch (tier1) {
                    case 1: {
                        tier = "I";
                        break;
                    }
                    case 2: {
                        tier = "II";
                        break;
                    }
                    case 3: {
                        tier = "III";
                        break;
                    }
                    case 4: {
                        tier = "IV";
                        break;
                    }
                }

                // Renders a floating text in air
                Render.drawTextAtWorld(
                        TextFormatting.DARK_PURPLE + "Chest Tier: "
                                + TextFormatting.BOLD + TextFormatting.YELLOW + tier
                        , pos.getX() + 0.5f, pos.getY() + 2.0f, pos.getZ() + 0.5f, 0xFFFFFF, 0.03F, false, true, false, event.getPartialTicks());

                Render.drawTextAtWorld(TextFormatting.DARK_PURPLE + "Level: "
                                + TextFormatting.AQUA
                                + (lvl == 0 ? "UNKNOWN" : (lvl - 2) + "-" + (lvl + 2)),
                        pos.getX() + 0.5f, pos.getY() + 1.7f, pos.getZ() + 0.5f, 0xFFFFFF, 0.03F, false, true, false,event.getPartialTicks());
            }
        }
    }
}
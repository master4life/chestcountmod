package tk.avicia.chestcountmod;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import tk.avicia.chestcountmod.pathfinding.AStarCustomPathFinder;
import tk.avicia.chestcountmod.util.ChestUtils;
import tk.avicia.chestcountmod.util.MythicUtils;
import tk.avicia.chestcountmod.util.RenderUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class DisplayChest {
    public Executor executor = Executors.newSingleThreadExecutor();
    public List<BlockPos> path = new ArrayList<>();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void onTickEvent(TickEvent.ClientTickEvent event) {
        World world = ChestCountMod.getMC().world;
        if(ChestCountMod.CONFIG.getConfigBoolean("displayPathfinding") && world != null && world.getTotalWorldTime() % 5 == 0) { // 1/4 of an second it should trigger
            // Run CODE on a ThreadPool
            this.executor.execute(() -> {
                BlockPos playerPos = ChestCountMod.getMC().player.getPosition();
                List<BlockPos> allChestsPos = new ChestUtils().getAllChests();
                BlockPos closest = null;
                EntityPlayerSP player = ChestCountMod.getMC().player;

                // find the closest chest to generate a path
                for (BlockPos pos : allChestsPos) {
                    if (world.getBlockState(pos).getBlock().equals(Blocks.CHEST)) {
                        if (closest == null)
                            closest = pos;

                        if (player.getDistanceSq(pos) < player.getDistanceSq(closest))
                            closest = pos;
                    }
                }
                if (closest == null) return;

                AStarCustomPathFinder star = new AStarCustomPathFinder( new Vec3d(playerPos.getX(),playerPos.getY(), playerPos.getZ()), new Vec3d(closest.getX(), closest.getY(), closest.getZ()));
                // Calculating processing
                star.compute();
                List<BlockPos> nodes = new ArrayList<>();
                for(int i = 0; i < star.getPath().size(); i++)
                    if (i % 2 != 0) // make it less blocky by skipping each second node
                        nodes.add(new BlockPos(star.getPath().get(i).x, star.getPath().get(i).y, star.getPath().get(i).z));

                this.path = nodes;
            });
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority= EventPriority.NORMAL, receiveCanceled=true)
    public void renderTick(RenderWorldLastEvent event) {
        if (ChestCountMod.getMC().player == null)
            return;

        ChestUtils chests = new ChestUtils();
        List<JsonObject> listChest = chests.getAllJsonChests();

        if (listChest == null) return;

        // Cancel rendering when listChest doesnt exist yet
        if (listChest.size() <= 0) {
            return;
        }

        Entity viewEntity = ChestCountMod.getMC().getRenderViewEntity();
        // Cancels when player hasnt initialised yet.
        if (viewEntity == null) {
            return;
        }

        float tick = event.getPartialTicks();

        // Foreach all values from "ChestUtils"
        for (JsonObject obj : listChest) {
            BlockPos pos = new BlockPos((double) obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt());

            if (ChestCountMod.getMC().world.getBlockState(pos).getBlock() instanceof BlockContainer)
                RenderUtils.highlightBlock(pos, new Color(0, 255, 0, 70), tick, true);
            else
                RenderUtils.highlightBlock(pos, new Color(255, 0, 0, 70), tick, true);

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

            if (ChestCountMod.CONFIG.getConfigBoolean("displayMythicOnChest") && viewEntity.getDistance(pos.getX(), pos.getY(), pos.getZ()) < 5.0d) {
                RenderUtils.drawTextAtWorld(TextFormatting.AQUA + "^ Mythic possibilities ^", pos.getX() + 0.5f, pos.getY() + 2.3f, pos.getZ() + 0.5f, 0xFFFFFF, 0.03F, false, true, false, event.getPartialTicks());
                String[] mythics = MythicUtils.mythicType(lvl - 2, lvl + 2, tier1);

                for (int i = 0; i < mythics.length; i++)
                    RenderUtils.drawTextAtWorld(TextFormatting.DARK_PURPLE + mythics[i].replace("_", " "), pos.getX() + 0.5f, pos.getY() + (float) (2.6f + (i - (0.7 * i))), pos.getZ() + 0.5f, 0xFFFFFF, 0.03F, false, true, false, event.getPartialTicks());
            }

            if (distance.equalsIgnoreCase("Everywhere") || viewEntity.getDistance(pos.getX(), pos.getY(), pos.getZ()) < (double) distanceChest) {
                // Converts numeric to latain numbers.
                String tier = "";
                switch (tier1) {
                    case 1: { tier = "I"; break; }
                    case 2: { tier = "II"; break; }
                    case 3: { tier = "III";  break; }
                    case 4: { tier = "IV";  break; }
                }

                // Renders a floating text in air
                RenderUtils.drawTextAtWorld(
                        TextFormatting.DARK_PURPLE + "Chest Tier: "
                                + TextFormatting.BOLD + TextFormatting.YELLOW + tier
                        , pos.getX() + 0.5f, pos.getY() + 2.0f, pos.getZ() + 0.5f, 0xFFFFFF, 0.03F, false, true, false, tick);

                RenderUtils.drawTextAtWorld(TextFormatting.DARK_PURPLE + "Level: "
                                + TextFormatting.AQUA
                                + (lvl == 0 ? "UNKNOWN" : (lvl - 2) + "-" + (lvl + 2)),
                        pos.getX() + 0.5f, pos.getY() + 1.7f, pos.getZ() + 0.5f, 0xFFFFFF, 0.03F, false, true, false, tick);
            }
        }

        if (ChestCountMod.CONFIG.getConfigBoolean("displayPathfinding") && !this.path.isEmpty()) {
            int max = Math.min(this.path.size() - 1, 128);
            for (int i = 0; i < max; i++) {
                if (i+1 >= this.path.size())
                    break;

                BlockPos start = this.path.get(i);
                BlockPos end = this.path.get(i+1);
                RenderUtils.drawLine(start, end, new Color(0, 255, 255, 150), tick, false);
            }
        }
    }
}
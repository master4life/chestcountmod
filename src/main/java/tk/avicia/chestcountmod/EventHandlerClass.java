package tk.avicia.chestcountmod;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import tk.avicia.chestcountmod.configs.ConfigsGui;
import tk.avicia.chestcountmod.configs.locations.LocationsGui;
import tk.avicia.chestcountmod.configs.locations.MultipleElements;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class EventHandlerClass {
    private static final TextFormatting[] colors = {TextFormatting.DARK_GRAY, TextFormatting.BLACK, TextFormatting.RED,
            TextFormatting.LIGHT_PURPLE, TextFormatting.DARK_BLUE, TextFormatting.DARK_GREEN, TextFormatting.DARK_RED,
            TextFormatting.DARK_PURPLE, TextFormatting.BLUE};

    private boolean hasMythicBeenRegistered = false;
    private boolean hasChestBeenRegistered = false;
    private int chestsDry = 0;
    private BlockPos chestLocation = null;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void openChest(PlayerInteractEvent.RightClickBlock e) {
        if (e.isCanceled()) return;
        BlockPos pos = e.getPos();
        IBlockState state = e.getEntityPlayer().world.getBlockState(pos);
        if (!(state.getBlock() instanceof BlockContainer)) return;
        chestLocation = pos.toImmutable();
    }

    @SubscribeEvent
    public void onGuiOpen(GuiScreenEvent.InitGuiEvent event) {
        if (ChestCountMod.getMC().player == null || event.getGui() == null) {
            return;
        }
        if (!ChestCountMod.getChestCountData().hasBeenInitialized()) {
            // Keeps trying to get the chestcount data from the api until it gets it
            Thread thread = new Thread(() -> {
                ChestCountMod.PLAYER_UUID = ChestCountMod.getMC().player.getGameProfile().getId().toString();
                ChestCountMod.getChestCountData().updateChestCount();
                ChestCountMod.getMythicData().updateDry();
            });
            thread.start();
        }
        Container openContainer = ChestCountMod.getMC().player.openContainer;
        if (openContainer instanceof ContainerChest) {
            InventoryBasic lowerInventory = (InventoryBasic) ((ContainerChest) openContainer).getLowerChestInventory();
            String containerName = lowerInventory.getName();

            if (ChestCountMod.getMythicData().getChests() == null) {
                ChestCountMod.getMythicData().getChestData();
            }

            // It is a lootchest and it doesn't already have a new name
            if (containerName.contains("Loot Chest") && !containerName.contains("#")) {
                // All this code runs once when the loot chest has been opened
                ChestCountMod.getChestCountData().addToSessionChestCount();
                ChestCountMod.getMythicData().addToDry();
                this.chestsDry = ChestCountMod.getMythicData().getChestsDry();
                // Defaults to not having a mythic in the chest
                this.hasMythicBeenRegistered = false;
                this.hasChestBeenRegistered = true;
                lowerInventory.setCustomName((ChestCountMod.CONFIG.getConfigBoolean("enableColoredName") ? ChestCountMod.getRandom(colors) : "") + containerName + " #" +
                        ChestCountMod.getChestCountData().getSessionChestCount()
                        + " Tot: " + ChestCountMod.getChestCountData().getTotalChestCount());
            }
        }
    }


    @SubscribeEvent
    public void guiDraw(GuiScreenEvent.DrawScreenEvent.Pre event) {
        if (ChestCountMod.getMC().player == null || event.getGui() == null) {
            return;
        }
        Container openContainer = ChestCountMod.getMC().player.openContainer;
        if (openContainer instanceof ContainerChest) {
            InventoryBasic lowerInventory = (InventoryBasic) ((ContainerChest) openContainer).getLowerChestInventory();
            String containerName = lowerInventory.getName();
            if (containerName.contains("Loot Chest")) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(1f, 1f, 1f);
                int screenWidth = event.getGui().width;
                int screenHeight = event.getGui().height;
                ChestCountMod.drawString(chestsDry + " Dry", screenWidth / 2 - 20, screenHeight / 2 - 11, new Color(64, 64, 64));
                GlStateManager.popMatrix();
                int itemCount = 0;
                for (int i = 0; i < 27; i++) {
                    if (!lowerInventory.getStackInSlot(i).getDisplayName().equals("Air")) {
                        itemCount++;
                    }
                }
                if (itemCount == 0) {
                    return; // If there are no items on the chest (or the items haven't loaded) just try again basically
                }
                boolean isMythicInChest = false;

                int itemTotalLVL = 0;
                int itemLVLCounter = 0;

                for (int i = 0; i < 27; i++) {
                    ItemStack itemStack = lowerInventory.getStackInSlot(i);
                    if (!itemStack.getDisplayName().equals("Air")) {
                        List<String> lore = itemStack.getTooltip(ChestCountMod.getMC().player, ITooltipFlag.TooltipFlags.ADVANCED);
                        // Find whether the lore includes Tier: Mythic
                        Optional<String> mythicTier = lore.stream()
                                .filter(line -> Objects.requireNonNull(TextFormatting.getTextWithoutFormattingCodes(line)).contains("Tier: Mythic")).findFirst();
                        Optional<String> itemLevel = lore.stream()
                                .filter(line -> line.contains("Lv. ")).findFirst();

                        if (itemLevel.isPresent()) {
                            // Clears all colors &b,&c,$b etc. from items lore
                            String clearedLore = TextFormatting.getTextWithoutFormattingCodes(itemLevel.get());
                            // Checks if lore is valid(exists)
                            if (clearedLore == null)
                                return;
                            try {
                                Pattern iHateRegEx = Pattern.compile("(^[0-9]+$)", 2);
                                if (iHateRegEx.matcher(clearedLore.split(": ")[1]).find()) {
                                    // FIRST NUMERIC: Items that have a defined level range gets incremented on var itemTotalLVL
                                    // Like Potion, Ingridents
                                    itemTotalLVL += Integer.parseInt(clearedLore.split(": ")[1]);
                                } else if ((clearedLore.split(": ")[1].split("-")).length == 2) {
                                    // SECOND NUMERIC: Items with a Range of Levels
                                    // Like Unidentified Chests
                                    String[] range = clearedLore.split(": ")[1].split("-");

                                    itemTotalLVL += (Integer.parseInt(range[0]) +
                                            Integer.parseInt(range[1])) / 2;
                                }
                                // Counts how many items have been rolled through this process.
                                itemLVLCounter++;
                            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                                e.printStackTrace();
                            }
                        }

                        if (mythicTier.isPresent()) {
                            if (!hasMythicBeenRegistered) { // Makes sure you don't register the same mythic twice
                                if (itemLevel.isPresent()) {
                                    try {
                                        // A new mythic has been found!
                                        String mythicString = itemStack.getDisplayName() + " " + itemLevel.get();
                                        if (ChestCountMod.CONFIG.getConfigBoolean("displayMythicOnFind")) {
                                            if (ChestCountMod.CONFIG.getConfigBoolean("displayMythicTypeOnFind")) {
                                                ChestCountMod.getMC().player.sendMessage(new TextComponentString(mythicString + " : " + TextFormatting.RED + ChestCountMod.getMythicData().getChestsDry() + " dry"));
                                            } else {
                                                ChestCountMod.getMC().player.sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + "Mythic found : " + TextFormatting.RED + ChestCountMod.getMythicData().getChestsDry() + " dry"));
                                            }
                                        }
                                        EntityPlayerSP player = ChestCountMod.getMC().player;
                                        ChestCountMod.getMythicData().addMythic(ChestCountMod.getChestCountData().getTotalChestCount(), TextFormatting.getTextWithoutFormattingCodes(mythicString), this.chestsDry, chestLocation.getX(), chestLocation.getY(), chestLocation.getZ());
                                    } catch (Exception e) {
                                        // If a mythic is in the chest, just catch every exception (I don't want to risk a crash with a mythic in the chest)
                                        e.printStackTrace();
                                    }
                                }
                            }
                            isMythicInChest = true;
                        }
                    }
                }
                // After checking every item in the chest
                if (isMythicInChest) {
                    if (!this.hasMythicBeenRegistered) {
                        this.hasMythicBeenRegistered = true;
                    }
                }

                if (this.hasChestBeenRegistered) {
                    this.hasChestBeenRegistered = false;

                    int level = 0;
                    if (itemLVLCounter > 0)
                        level = itemTotalLVL / itemLVLCounter;

                    ArrayList<JsonObject> listChest = ChestCountMod.getMythicData().getChests();
                    ArrayList<BlockPos> chests = new ArrayList<>();
                    String[] lvl = containerName.split(" ");

                    // Converts latain numeric to ASCII numeric
                    int num = 0;
                    switch (lvl[2]) {
                        case "I": {
                            num = 1;
                            break;
                        }
                        case "II": {
                            num = 2;
                            break;
                        }
                        case "III": {
                            num = 3;
                            break;
                        }
                        case "IV": {
                            num = 4;
                            break;
                        }
                    }

                    if (listChest != null) {
                        for (JsonObject obj : listChest)
                            chests.add(new BlockPos(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt()));

                        // Checks if the current chest position is already added.
                        if (chests.contains(this.chestLocation))
                            return;

                        // If the chests contains no items w/ levels at all
                        if (level == 0) {
                            ChestCountMod.getMC().player.sendMessage(new TextComponentString(TextFormatting.RED + "Couldnt verify the chest level. Try again later when refilled."));
                            return;
                        }

                        // Too less items results to inaccurate values, which you have retry
                        if (itemLVLCounter < 4) {
                            ChestCountMod.getMC().player.sendMessage(new TextComponentString(TextFormatting.RED + "Too less items to estimate item level. Try again later when refilled."));
                            return;
                        }
                    }

                    // Chest BlockPos gets saved to the JSON.
                    ChestCountMod.getMythicData().addChest(level, num, this.chestLocation.getX(), this.chestLocation.getY(), this.chestLocation.getZ());
                    ChestCountMod.getMC().player.sendMessage(
                            new TextComponentString(
                                    TextFormatting.GREEN + "Chest "
                                            + TextFormatting.RED + "(Tier: "
                                            + num + " Level: " + (level - 2) + "-" + (level + 2) + ") "
                                            + TextFormatting.GREEN + "has been added to your collection!"));
                }

            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (ChestCountMod.CONFIG.shouldGuiConfigBeDrawn()) {
            ChestCountMod.getMC().displayGuiScreen(new ConfigsGui());
            ChestCountMod.CONFIG.setShouldGuiConfigBeDrawn(false);
        }
        // Clicking the edit button in the Info Location configs changes its value to Editing, so when that happens we
        // open up the locations editing gui
        if (ChestCountMod.CONFIG.getConfig("infoLocation").equals("Editing")) {
            ChestCountMod.getMC().displayGuiScreen(new LocationsGui());
            ChestCountMod.CONFIG.setConfig("infoLocation", "Edit");
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void renderOverlay(RenderGameOverlayEvent.Chat event) {
        // The Chat RenderGameOverlayEvent renders stuff normally, it disappears in f1, you can see it when your
        // inventory is open and you can make stuff transparent
        MultipleElements elements = InfoDisplay.getElementsToDraw();
        elements.draw();
    }

}

package tk.avicia.chestcountmod.util;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockContainer;
import net.minecraft.util.math.BlockPos;
import tk.avicia.chestcountmod.ChestCountMod;

import java.util.ArrayList;
import java.util.List;

public class ChestUtils {
    private final List<JsonObject> jsonFile;
    private final List<BlockPos> chests;
    private final List<Integer> tiers;
    private final List<Integer> levels;

    public ChestUtils() {
        this.jsonFile = ChestCountMod.getMythicData().getChests();
        this.chests = new ArrayList<>();
        this.tiers = new ArrayList<>();
        this.levels = new ArrayList<>();
    }

    public boolean IsChestPresent(BlockPos pos) {
        return (ChestCountMod.getMC().world.getBlockState(pos).getBlock() instanceof BlockContainer);
    }
    public boolean IsValid(BlockPos pos) {
        return chests.contains(pos);
    }

    public List<JsonObject> getAllJsonChests() {
        return jsonFile;
    }
    public List<BlockPos> getAllChests() {
        for (JsonObject obj : this.jsonFile )
            this.chests.add( new BlockPos(obj.get("x").getAsInt(), obj.get("y").getAsInt(), obj.get("z").getAsInt()));

        return this.chests;
    }

    public List<Integer> getTiers() {
        for (JsonObject obj : ChestCountMod.getMythicData().getChests())
            this.tiers.add(obj.get("tier").getAsInt());

        return this.tiers;
    }
    public List<Integer> getLevels() {
        for (JsonObject obj : ChestCountMod.getMythicData().getChests())
            this.levels.add(obj.get("level").getAsInt());

        return this.levels;
    }
}

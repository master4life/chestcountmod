package tk.avicia.chestcountmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.compress.archivers.ar.ArArchiveEntry;
import scala.util.Try;

import java.util.ArrayList;

public class MythicData {
    private int chestsDry = 0;

    public MythicData() {
    }

    public JsonObject getChestData() {
        CustomFile chestData = new CustomFile(ChestCountMod.getMC().mcDataDir, "chestcountmod/chestData.json");

        return chestData.readJson();
    }

    public JsonObject getMythicData() {
        CustomFile mythicData = new CustomFile(ChestCountMod.getMC().mcDataDir, "chestcountmod/mythicData.json");

        return mythicData.readJson();
    }

    public JsonObject getDryData() {
        CustomFile dryData = new CustomFile(ChestCountMod.getMC().mcDataDir, "chestcountmod/dryCount.json");

        return dryData.readJson();
    }

    public void addChest(int lvl, int tier, int x, int y, int z) {
        try {
            JsonObject currentData = getChestData();
            JsonObject newData = new JsonObject();
            newData.addProperty("lvl", lvl);
            newData.addProperty("tier", tier);
            newData.addProperty("x", x);
            newData.addProperty("y", y);
            newData.addProperty("z", z);
            if (!currentData.has("ChestUtils")) {
                currentData.add("ChestUtils", new JsonArray());
            }
            currentData.get("ChestUtils").getAsJsonArray().add(newData);
            CustomFile chestData = new CustomFile(ChestCountMod.getMC().mcDataDir, "chestcountmod/chestData.json");
            chestData.writeJson(currentData);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    public void addMythic(int chestCount, String mythic, int dry, int x, int y, int z) {
        try {
            JsonObject currentData = getMythicData();
            JsonObject newData = new JsonObject();
            newData.addProperty("chestCount", chestCount);
            newData.addProperty("mythic", mythic);
            newData.addProperty("dry", dry);
            newData.addProperty("x", x);
            newData.addProperty("y", y);
            newData.addProperty("z", z);
            if (!currentData.has(ChestCountMod.PLAYER_UUID)) {
                currentData.add(ChestCountMod.PLAYER_UUID, new JsonArray());
            }
            currentData.get(ChestCountMod.PLAYER_UUID).getAsJsonArray().add(newData);
            CustomFile mythicData = new CustomFile(ChestCountMod.getMC().mcDataDir, "chestcountmod/mythicData.json");
            mythicData.writeJson(currentData);
            updateDry();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void updateDry() {
        try {
            JsonObject lastMythic = getLastMythic();
            if (lastMythic != null) {
                chestsDry = ChestCountMod.getChestCountData().getTotalChestCount() - lastMythic.get("chestCount").getAsInt();
            } else {
                chestsDry = ChestCountMod.getChestCountData().getTotalChestCount();
            }
            saveDryToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToDry() {
        chestsDry++;
        try {
            saveDryToFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDryToFile() {
        JsonObject currentData = getDryData();
        currentData.addProperty(ChestCountMod.PLAYER_UUID, chestsDry);
        CustomFile mythicData = new CustomFile(ChestCountMod.getMC().mcDataDir, "chestcountmod/dryCount.json");
        mythicData.writeJson(currentData);
    }

    public int getChestsDry() {
        return chestsDry;
    }

    public int getLevelChest() {
        int level = 0;
        JsonArray chestData = getChestData().getAsJsonArray("ChestUtils");

        if (chestData == null)
            return 0;

        for (int i = 0; i < chestData.size(); i++)
        {
            level = chestData.get(i).getAsJsonObject().get("lvl").getAsInt();
        }

        return level;
    }

    public ArrayList<JsonObject> getChests() {
        ArrayList<JsonObject> chests = new ArrayList<>();
        JsonArray chestData = getChestData().getAsJsonArray("Chests");

        if (chestData == null)
            return null;

        for (int i = 0; i < chestData.size(); i++)
        {
            chests.add(chestData.get(i).getAsJsonObject());
        }

        if (chests.size() > 0)
            return chests;
        else
            return null;
    }

    public JsonObject getLastMythic() {
        JsonObject mythicData = getMythicData().getAsJsonObject();
        if (!mythicData.has(ChestCountMod.PLAYER_UUID)) {
            mythicData.add(ChestCountMod.PLAYER_UUID, new JsonArray());
        }
        JsonArray mythics = mythicData.get(ChestCountMod.PLAYER_UUID).getAsJsonArray();
        if (mythics.size() > 0) {
            return mythics.get(mythics.size() - 1).getAsJsonObject();
        } else {
            return null;
        }
    }
}

package tk.avicia.chestcountmod.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class MythicUtils {
    public static String[] mythicType(int from, int to, int tier) {
        StringBuilder mythic = new StringBuilder();
        for (int i : IntStream.range(from, to).toArray()) {
            switch (i) {
                case 104: { mythic.append(" Convergence"); break; }
                case 103: { mythic.append(" Quetzalcoatl"); break; }
                case 102: { mythic.append(" Epoch"); break; }
                case 101: { mythic.append(" Immolation Oblivion"); break; }
                case 99: { mythic.append(" Fatal Warp Singularity Revenant Stratiformis"); break; }
                case 98: { mythic.append(" Monster Spring Warchief"); break; }
                case 97: { mythic.append(" Absolution Gaia Collapse Inferno Nirvana Divzer Stardew"); break; }
                case 96: { mythic.append(" Fantasia Toxoplasmosis Lament Thrundacrack Cataclysm Weathered Grimtrap Dawnbreak"); break; }
                case 95: { mythic.append(" Nullification Sunstar Idol Grandmother Moontower Ignis"); break; }
                case 94: { mythic.append(" Hadal Slayer Alkatraz"); break; }
                case 93: { mythic.append(" Olympic Guardian Boreal Freedom"); break; }
                case 92: { mythic.append(" Galleon"); break; }
                case 91: { mythic.append(" Hero Resurgence"); break; }
                case 90: { mythic.append(" Crusade_Sabatons"); break; }
                case 89: { if (tier >= 3) mythic.append(" Discoverer"); break; }
                case 81: { mythic.append(" Apocalypse"); break; }
                case 77: { mythic.append(" Aftershock"); break; }
                case 74: { mythic.append(" Az"); break; }
                case 69: { mythic.append(" Archangel"); break; }
                case 65: {  mythic.append(" Pure"); break; }
                default: {
                }
            }
        }

        String regex = ".{1,30}[a-z]+";
        Matcher m = Pattern.compile(regex).matcher(mythic.toString());
        ArrayList<String> lines = new ArrayList<>();
        while (m.find())
            lines.add(m.group().replaceFirst( " ", ""));

        return lines.toArray(new String[0]);
    }
}

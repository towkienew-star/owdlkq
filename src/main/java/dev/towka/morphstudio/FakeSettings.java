package dev.towka.morphstudio;

import java.util.Properties;

public final class FakeSettings {
    public String skinName = "SELF";
    public boolean useLocalSkin = false;
    public boolean slimArms = false;
    public int armorPreset = 0;
    public String head = "minecraft:air";
    public String chest = "minecraft:air";
    public String legs = "minecraft:air";
    public String feet = "minecraft:air";
    public String mainHand = "minecraft:air";
    public String offHand = "minecraft:air";

    public String getSlot(int i) {
        return switch(i) {
            case 0 -> head; case 1 -> chest; case 2 -> legs;
            case 3 -> feet; case 4 -> mainHand; case 5 -> offHand;
            default -> "minecraft:air";
        };
    }
    public void setSlot(int i, String item) {
        switch(i) {
            case 0 -> head = item;
            case 1 -> chest = item;
            case 2 -> legs = item;
            case 3 -> feet = item;
            case 4 -> mainHand = item;
            case 5 -> offHand = item;
            default -> { }
        }
    }
    public void save(Properties p) {
        p.setProperty("skinName", skinName);
        p.setProperty("useLocalSkin", Boolean.toString(useLocalSkin));
        p.setProperty("slimArms", Boolean.toString(slimArms));
        p.setProperty("armorPreset", Integer.toString(armorPreset));
        for (int i=0; i<6; i++) p.setProperty("slot."+i, getSlot(i));
    }
    public void load(Properties p) {
        skinName=p.getProperty("skinName",skinName);
        useLocalSkin=Boolean.parseBoolean(p.getProperty("useLocalSkin", "false"));
        slimArms=Boolean.parseBoolean(p.getProperty("slimArms", "false"));
        try { armorPreset=Integer.parseInt(p.getProperty("armorPreset", "0")); }
        catch (NumberFormatException ignored) { }
        for (int i=0; i<6; i++) setSlot(i,p.getProperty("slot."+i,getSlot(i)));
    }
}

package dev.towka.morphstudio;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.UUID;

public final class FakeManager {
    public static final UUID FAKE_UUID = UUID.nameUUIDFromBytes("MorphStudio-LocalFake".getBytes(StandardCharsets.UTF_8));
    public static final String FAKE_NAME = "MorphFake";
    public static final FakeSettings SETTINGS = new FakeSettings();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("morphstudio-fake.properties");
    private static FakeActor actor;
    private static ClientWorld world;
    private static boolean skinLoadAttempted;
    private FakeManager() { }

    public static boolean isFakeName(String name) { return FAKE_NAME.equals(name); }
    public static boolean isFake(Entity entity) { return actor != null && entity == actor; }
    public static boolean exists() { return actor != null && world != null; }
    public static FakeActor get() { return actor; }

    public static void tick(MinecraftClient client) {
        if (client.world != world) { actor = null; world = client.world; skinLoadAttempted = false; }
        if (client.world != null && !skinLoadAttempted) {
            skinLoadAttempted = true;
            if (SETTINGS.useLocalSkin) FakeSkins.reloadPng();
        }
        if (actor != null) {
            if (client.world == null || client.world.getEntityById(actor.getId()) != actor) {
                actor = null;
                return;
            }
            actor.setVelocity(Vec3d.ZERO);
            actor.setSneaking(ProfileStore.edit(FAKE_UUID).pose == Pose.CROUCH);
        }
    }

    public static void spawn() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null || client.player == null) return;
        if (exists()) remove();
        world = client.world;
        actor = new FakeActor(world);
        moveInFront();
        actor.setCustomNameVisible(false);
        world.addEntity(actor);
        updateEquipment();
    }

    public static void moveInFront() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (actor == null || client.player == null) return;
        Vec3d look = client.player.getRotationVec(1.0F).multiply(2.6);
        actor.setPosition(client.player.getX()+look.x, client.player.getY(), client.player.getZ()+look.z);
        actor.setYaw(client.player.getYaw()+180f);
        actor.setHeadYaw(actor.getYaw());
        actor.setBodyYaw(actor.getYaw());
    }

    public static void remove() {
        if (actor != null && world != null && world.getEntityById(actor.getId()) == actor) {
            world.removeEntity(actor.getId(), Entity.RemovalReason.DISCARDED);
        }
        actor = null;
    }

    private static ItemStack item(String id) {
        Identifier key = Identifier.tryParse(id);
        if (key == null) return ItemStack.EMPTY;
        Item found = Registries.ITEM.getOrEmpty(key).orElse(Items.AIR);
        return found == Items.AIR ? ItemStack.EMPTY : new ItemStack(found);
    }

    public static boolean isValidItem(String id) {
        Identifier key = Identifier.tryParse(id);
        return key != null && Registries.ITEM.getOrEmpty(key).isPresent();
    }

    public static void updateEquipment() {
        if (actor == null) return;
        actor.equipStack(EquipmentSlot.HEAD, item(SETTINGS.head));
        actor.equipStack(EquipmentSlot.CHEST, item(SETTINGS.chest));
        actor.equipStack(EquipmentSlot.LEGS, item(SETTINGS.legs));
        actor.equipStack(EquipmentSlot.FEET, item(SETTINGS.feet));
        actor.equipStack(EquipmentSlot.MAINHAND, item(SETTINGS.mainHand));
        actor.equipStack(EquipmentSlot.OFFHAND, item(SETTINGS.offHand));
    }

    public static void cycleArmor() {
        SETTINGS.armorPreset = (SETTINGS.armorPreset+1)%6;
        String[] armor = {"air", "leather", "iron", "diamond", "netherite", "golden"};
        String a = armor[SETTINGS.armorPreset];
        SETTINGS.head = "minecraft:" + (a.equals("air") ? "air" : a+"_helmet");
        SETTINGS.chest = "minecraft:" + (a.equals("air") ? "air" : a+"_chestplate");
        SETTINGS.legs = "minecraft:" + (a.equals("air") ? "air" : a+"_leggings");
        SETTINGS.feet = "minecraft:" + (a.equals("air") ? "air" : a+"_boots");
        updateEquipment(); save();
    }

    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) { p.load(in); SETTINGS.load(p); }
        catch (Exception e) { System.err.println("[MorphStudio] fake settings: " + e); }
    }
    public static void save() {
        Properties p = new Properties(); SETTINGS.save(p);
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) { p.store(out, "Morph Studio fake actor"); }
        } catch (Exception e) { System.err.println("[MorphStudio] save fake settings: " + e); }
    }
}

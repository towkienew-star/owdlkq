package dev.towka.morphstudio;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public final class ProfileStore {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("morphstudio-players.properties");
    private static final Map<String, VisualProfile> PROFILES = new HashMap<>();
    private static final Map<String, UUID> ONLINE = new HashMap<>();
    private static String server = "singleplayer";
    private ProfileStore() { }

    public static void sync(MinecraftClient client) {
        ONLINE.clear();
        if (client.getNetworkHandler() == null) return;
        server = client.getCurrentServerEntry() == null ? "singleplayer" : client.getCurrentServerEntry().address;
        for (PlayerListEntry p : client.getNetworkHandler().getPlayerList()) {
            ONLINE.put(p.getProfile().getName(), p.getProfile().getId());
        }
    }
    private static String key(UUID uuid) { return server + "|" + uuid; }
    public static VisualProfile edit(UUID id) { return PROFILES.computeIfAbsent(key(id), k -> new VisualProfile()); }
    public static VisualProfile forName(String name) {
        if (FakeManager.isFakeName(name)) return edit(FakeManager.FAKE_UUID);
        UUID id = ONLINE.get(name);
        return id == null ? null : PROFILES.get(key(id));
    }
    public static void reset(UUID id) { PROFILES.remove(key(id)); save(); }
    public static void load() {
        if (!Files.isRegularFile(FILE)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            for (String compound : p.stringPropertyNames()) {
                int cut = compound.lastIndexOf('#');
                if (cut <= 0) continue;
                String id = compound.substring(0, cut);
                String field = compound.substring(cut+1);
                VisualProfile profile = PROFILES.computeIfAbsent(id, k -> new VisualProfile());
                if (field.equals("pose")) {
                    try { profile.pose = Pose.valueOf(p.getProperty(compound)); }
                    catch (IllegalArgumentException ignored) { }
                    continue;
                }
                for (int i=0; i<VisualProfile.KEYS.length; i++) {
                    if (VisualProfile.KEYS[i].equals(field)) {
                        try { profile.set(i, Float.parseFloat(p.getProperty(compound))); }
                        catch (NumberFormatException ignored) { }
                    }
                }
            }
        } catch (Exception e) { System.err.println("[MorphStudio] load profiles: " + e); }
    }
    public static void save() {
        Properties props = new Properties();
        PROFILES.forEach((key, profile) -> {
            for (int i=0; i<VisualProfile.KEYS.length; i++) {
                props.setProperty(key + "#" + VisualProfile.KEYS[i], Float.toString(profile.get(i)));
            }
            props.setProperty(key + "#pose", profile.pose.name());
        });
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out=Files.newOutputStream(FILE)) { props.store(out, "Morph Studio local appearances"); }
        } catch (Exception e) { System.err.println("[MorphStudio] save profiles: " + e); }
    }
}

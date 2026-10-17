package dev.towka.morphstudio;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FakeSkins {
    public static final Path PNG = FabricLoader.getInstance().getConfigDir().resolve("morphstudio").resolve("skin.png");
    private static Identifier customTexture;
    private FakeSkins() { }

    public static SkinTextures selected() {
        FakeSettings prefs = FakeManager.SETTINGS;
        MinecraftClient client = MinecraftClient.getInstance();
        if (prefs.useLocalSkin && customTexture != null) {
            return new SkinTextures(customTexture, null, null, null,
                prefs.slimArms ? SkinTextures.Model.SLIM : SkinTextures.Model.WIDE, true);
        }
        if (client.player == null) return null;
        if (prefs.skinName.equalsIgnoreCase("SELF")) return client.player.getSkinTextures();
        if (client.getNetworkHandler() != null) {
            for (PlayerListEntry p : client.getNetworkHandler().getPlayerList()) {
                if (p.getProfile().getName().equalsIgnoreCase(prefs.skinName)) return p.getSkinTextures();
            }
        }
        return client.player.getSkinTextures();
    }

    public static boolean reloadPng() {
        if (!Files.isRegularFile(PNG)) return false;
        try (InputStream in=Files.newInputStream(PNG)) {
            NativeImage image = NativeImage.read(in);
            if (image.getWidth() != 64 || image.getHeight() != 64) {
                image.close();
                return false;
            }
            MinecraftClient client = MinecraftClient.getInstance();
            if (customTexture != null) client.getTextureManager().destroyTexture(customTexture);
            Identifier id = Identifier.of("morphstudio", "user_skin");
            client.getTextureManager().registerTexture(id, new NativeImageBackedTexture(image));
            customTexture = id;
            return true;
        } catch (Exception e) {
            System.err.println("[MorphStudio] invalid skin PNG: " + e);
            return false;
        }
    }
}

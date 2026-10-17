package dev.towka.morphstudio;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.world.ClientWorld;

/** Never spawned by server; visual stand-in exists only in this client world. */
public final class FakeActor extends OtherClientPlayerEntity {
    public FakeActor(ClientWorld world) {
        super(world, new GameProfile(FakeManager.FAKE_UUID, FakeManager.FAKE_NAME));
        setNoGravity(true);
        setInvulnerable(true);
    }

    @Override
    public SkinTextures getSkinTextures() {
        SkinTextures skin = FakeSkins.selected();
        return skin != null ? skin : super.getSkinTextures();
    }

    @Override
    public void tick() {
        // Intentionally stationary: no local physics, player AI or movement packets.
    }
}

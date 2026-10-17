package dev.towka.morphstudio.mixin;

import dev.towka.morphstudio.ProfileStore;
import dev.towka.morphstudio.VisualProfile;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method="scale(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;)V",at=@At("HEAD"))
    private void morphstudio$scale(PlayerEntityRenderState state, MatrixStack matrices, CallbackInfo ci) {
        VisualProfile p=ProfileStore.forName(state.name);
        if (p != null) matrices.scale(p.overall*p.width,p.overall*p.height,p.overall*p.width);
    }
}

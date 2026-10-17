package dev.towka.morphstudio.mixin;

import dev.towka.morphstudio.FakeManager;
import dev.towka.morphstudio.MorphStudioClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Local fake-player clicks are handled before packets reach the server. */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Unique private boolean morphstudio$clickingFake() {
        MinecraftClient client=(MinecraftClient)(Object)this;
        return client.currentScreen == null && client.crosshairTarget instanceof EntityHitResult hit
            && FakeManager.isFake(hit.getEntity());
    }
    @Inject(method="doItemUse",at=@At("HEAD"),cancellable=true)
    private void morphstudio$rightClickFake(CallbackInfo ci) {
        if(morphstudio$clickingFake()) {
            MorphStudioClient.openFakeEditor();ci.cancel();
        }
    }
    @Inject(method="doAttack",at=@At("HEAD"),cancellable=true)
    private void morphstudio$leftClickFake(CallbackInfoReturnable<Boolean> cir) {
        if(morphstudio$clickingFake()) {
            MorphStudioClient.openFakeEditor();cir.setReturnValue(false);
        }
    }
}

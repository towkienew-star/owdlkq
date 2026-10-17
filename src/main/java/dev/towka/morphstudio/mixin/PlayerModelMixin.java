package dev.towka.morphstudio.mixin;

import dev.towka.morphstudio.Pose;
import dev.towka.morphstudio.ProfileStore;
import dev.towka.morphstudio.VisualProfile;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerModelMixin {
    @Inject(method="setAngles(Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;)V",at=@At("TAIL"))
    private void morphstudio$partsAndPose(PlayerEntityRenderState state, CallbackInfo ci) {
        PlayerEntityModel m=(PlayerEntityModel)(Object)this;
        VisualProfile p=ProfileStore.forName(state.name);
        float head=p == null?1f:p.head;
        float torso=p == null?1f:p.torso;
        float arms=p == null?1f:p.arms;
        float legs=p == null?1f:p.legs;
        scale(m.head,head); scale(m.hat,head);
        scale(m.body,torso); scale(m.jacket,torso);
        scale(m.leftArm,arms); scale(m.rightArm,arms);
        scale(m.leftSleeve,arms); scale(m.rightSleeve,arms);
        scale(m.leftLeg,legs); scale(m.rightLeg,legs);
        scale(m.leftPants,legs); scale(m.rightPants,legs);
        if(p!=null && p.pose!=Pose.DEFAULT) pose(m,p.pose,state.age);
    }
    private static void scale(ModelPart part,float v) {
        part.xScale=v;part.yScale=v;part.zScale=v;
    }
    private static void pose(PlayerEntityModel m,Pose pose,float age) {
        switch(pose) {
            case T_POSE -> {
                m.rightArm.pitch=0f;m.leftArm.pitch=0f;
                m.rightArm.roll=(float)Math.PI/2f;
                m.leftArm.roll=-(float)Math.PI/2f;
            }
            case ARMS_CROSSED -> {
                m.rightArm.pitch=-1.1f;m.leftArm.pitch=-1.1f;
                m.rightArm.yaw=-.80f;m.leftArm.yaw=.80f;
                m.rightArm.roll=.38f;m.leftArm.roll=-.38f;
            }
            case WAVE -> {
                m.rightArm.pitch=-2.8f;m.rightArm.roll=-.3f;
                m.leftArm.pitch=0f;
            }
            case DAB -> {
                m.rightArm.pitch=-1.5f;m.rightArm.yaw=.5f;m.rightArm.roll=.5f;
                m.leftArm.pitch=-.2f;m.leftArm.roll=-1.45f;
                m.head.roll=-.30f;
            }
            case POINT -> {
                m.rightArm.pitch=-1.4f;m.rightArm.yaw=0f;
                m.leftArm.pitch=0f;
            }
            case HANDS_UP -> {
                m.rightArm.pitch=-3f;m.leftArm.pitch=-3f;
            }
            case SIT -> {
                m.rightLeg.pitch=-1.4f;m.leftLeg.pitch=-1.4f;
                m.rightArm.pitch=-.3f;m.leftArm.pitch=-.3f;
            }
            case CROUCH -> {
                m.body.pitch=.5f;
                m.rightLeg.pitch=-.7f;m.leftLeg.pitch=-.7f;
            }
            case LOOK_DOWN -> m.head.pitch=.8f;
            case DANCE -> {
                m.rightArm.pitch=-1.4f+(float)Math.sin(age*.25f)*.6f;
                m.leftArm.pitch=-1.4f-(float)Math.sin(age*.25f)*.6f;
                m.rightLeg.pitch=(float)Math.sin(age*.25f)*.35f;
                m.leftLeg.pitch=-(float)Math.sin(age*.25f)*.35f;
            }
            default -> { }
        }
        // Mirror the arm/leg transforms onto the outer skin layers.
        copy(m.rightArm,m.rightSleeve);copy(m.leftArm,m.leftSleeve);
        copy(m.rightLeg,m.rightPants);copy(m.leftLeg,m.leftPants);
        copy(m.body,m.jacket);copy(m.head,m.hat);
    }
    private static void copy(ModelPart from,ModelPart to) {
        to.pitch=from.pitch;to.yaw=from.yaw;to.roll=from.roll;
    }
}

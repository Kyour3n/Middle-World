package com.example.middleworld.client;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

/**
 * Keyframe animations. These are ADDITIVE: they are layered on top of the
 * procedural walk cycle / head look set in {@link RaceModel#setupAnim}.
 */
public class MobAnimations {

    private static final AnimationChannel.Interpolation SMOOTH = AnimationChannel.Interpolations.CATMULLROM;
    private static final AnimationChannel.Interpolation LINEAR = AnimationChannel.Interpolations.LINEAR;

    /** 4 second loop: breathing, sneaky head tilt, twitching ears and nose. */
    public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(4.0F).looping()
            // Breathing: chest swells slightly
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), SMOOTH),
                    new Keyframe(1.0F, KeyframeAnimations.scaleVec(1.03F, 1.04F, 1.03F), SMOOTH),
                    new Keyframe(2.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), SMOOTH),
                    new Keyframe(3.0F, KeyframeAnimations.scaleVec(1.03F, 1.04F, 1.03F), SMOOTH),
                    new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), SMOOTH)))
            // Shifty head tilt / glance
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, 4.0F), SMOOTH),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH),
                    new Keyframe(3.0F, KeyframeAnimations.degreeVec(2.0F, 0.0F, -4.0F), SMOOTH),
                    new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            // Ear twitches (right ear quick flick, left ear flick offset in time)
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(1.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -18.0F), LINEAR),
                    new Keyframe(1.15F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 6.0F), LINEAR),
                    new Keyframe(1.3F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.8F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(2.9F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 18.0F), LINEAR),
                    new Keyframe(3.05F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -6.0F), LINEAR),
                    new Keyframe(3.2F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR)))
            // Sniffling nose
            .addAnimation("nose", new AnimationChannel(AnimationChannel.Targets.SCALE,
                    new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), LINEAR),
                    new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.2F, 1.2F, 1.2F), LINEAR),
                    new Keyframe(0.7F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), LINEAR),
                    new Keyframe(2.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), LINEAR),
                    new Keyframe(2.7F, KeyframeAnimations.scaleVec(1.25F, 1.25F, 1.25F), LINEAR),
                    new Keyframe(2.9F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), LINEAR),
                    new Keyframe(4.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), LINEAR)))
            // Free hand fidgets
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH),
                    new Keyframe(2.0F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, -4.0F), SMOOTH),
                    new Keyframe(4.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .build();

    /** 0.7 second dagger lunge: wind up, thrust, recover. */
    public static final AnimationDefinition ATTACK = AnimationDefinition.Builder.withLength(0.7F)
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-75.0F, 0.0F, -10.0F), SMOOTH),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(35.0F, 0.0F, 6.0F), LINEAR),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(25.0F, 0.0F, 10.0F), SMOOTH),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            // Body twists back, then whips forward and lunges
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-8.0F, -20.0F, 0.0F), SMOOTH),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(18.0F, 15.0F, 0.0F), LINEAR),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.POSITION,
                    new Keyframe(0.0F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.posVec(0.0F, 0.0F, 1.0F), SMOOTH),
                    new Keyframe(0.35F, KeyframeAnimations.posVec(0.0F, 0.0F, -2.0F), LINEAR),
                    new Keyframe(0.7F, KeyframeAnimations.posVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            // Head follows the lunge and snaps back
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.2F, KeyframeAnimations.degreeVec(-12.0F, 0.0F, 0.0F), SMOOTH),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(20.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            // Ears flatten back during the lunge
            .addAnimation("right_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 25.0F), SMOOTH),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .addAnimation("left_ear", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.35F, KeyframeAnimations.degreeVec(0.0F, 0.0F, -25.0F), SMOOTH),
                    new Keyframe(0.7F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .build();

    /** 0.5 second cast / shoot / heal gesture: both arms thrust forward, then relax. */
    public static final AnimationDefinition CAST = AnimationDefinition.Builder.withLength(0.5F)
            .addAnimation("right_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.15F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, -8.0F), SMOOTH),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), SMOOTH),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .addAnimation("left_arm", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.15F, KeyframeAnimations.degreeVec(-35.0F, 0.0F, 8.0F), SMOOTH),
                    new Keyframe(0.3F, KeyframeAnimations.degreeVec(-20.0F, 0.0F, 0.0F), SMOOTH),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .addAnimation("body", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.15F, KeyframeAnimations.degreeVec(-8.0F, 0.0F, 0.0F), SMOOTH),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.ROTATION,
                    new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), LINEAR),
                    new Keyframe(0.15F, KeyframeAnimations.degreeVec(-10.0F, 0.0F, 0.0F), SMOOTH),
                    new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), SMOOTH)))
            .build();
}

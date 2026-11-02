package com.example.middleworld.entity.ai;

import com.example.middleworld.entity.FactionMob;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.goal.Goal;

/** Healers run to wounded allies of their own faction and patch them up. */
public class HealAlliesGoal extends Goal {
    private final FactionMob healer;
    private FactionMob patient;
    private int cooldown;

    public HealAlliesGoal(FactionMob healer) {
        this.healer = healer;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        List<FactionMob> wounded = this.healer.level().getEntitiesOfClass(FactionMob.class,
                this.healer.getBoundingBox().inflate(14.0D),
                e -> e != this.healer && e.isAlive() && e.isAlly(this.healer)
                        && e.getHealth() < e.getMaxHealth() * 0.85F);
        FactionMob best = null;
        double bestDistance = Double.MAX_VALUE;
        for (FactionMob candidate : wounded) {
            double distance = this.healer.distanceToSqr(candidate);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }
        this.patient = best;
        return best != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.patient != null && this.patient.isAlive() && this.patient.getHealth() < this.patient.getMaxHealth();
    }

    @Override
    public void tick() {
        if (this.patient == null) {
            return;
        }
        this.healer.getLookControl().setLookAt(this.patient, 30.0F, 30.0F);
        if (this.healer.distanceToSqr(this.patient) > 9.0D) {
            this.healer.getNavigation().moveTo(this.patient, 1.2D);
            return;
        }
        this.healer.getNavigation().stop();
        this.patient.heal(6.0F);
        if (this.healer.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.HEART, this.patient.getX(), this.patient.getY() + this.patient.getBbHeight(),
                    this.patient.getZ(), 5, 0.3D, 0.3D, 0.3D, 0.0D);
        }
        this.healer.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.4F);
        this.healer.broadcastAttackEvent();
        this.cooldown = 60;
        this.patient = null;
    }

    @Override
    public void stop() {
        this.patient = null;
        this.healer.getNavigation().stop();
    }
}

package com.example.middleworld.entity.ai;

import com.example.middleworld.entity.FactionMob;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

/** Hit and run: after a strike the scout backs off for about a second and a half. */
public class RetreatGoal extends Goal {
    private final FactionMob mob;

    public RetreatGoal(FactionMob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return this.mob.retreatTicks > 0 && this.mob.getTarget() != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.retreatTicks > 0 && !this.mob.getNavigation().isDone();
    }

    @Override
    public void start() {
        Vec3 away = DefaultRandomPos.getPosAway(this.mob, 8, 4, this.mob.getTarget().position());
        if (away != null) {
            this.mob.getNavigation().moveTo(away.x, away.y, away.z, 1.4D);
        }
    }

    @Override
    public void stop() {
        this.mob.retreatTicks = 0;
    }
}

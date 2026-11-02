package com.example.middleworld.entity;

import com.example.middleworld.MiddleWorld;
import com.example.middleworld.entity.ai.HealAlliesGoal;
import com.example.middleworld.entity.ai.RetreatGoal;
import com.example.middleworld.reputation.Faction;
import com.example.middleworld.reputation.Factions;
import com.example.middleworld.reputation.ManagerAccess;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * One class for all 30 mobs (3 races x 10 roles). The race decides stats, looks, voice and
 * attitude; the role decides weapon, AI goals and special abilities.
 */
public class FactionMob extends PathfinderMob implements RangedAttackMob {
    /** Looping idle animation. Client side only. */
    public final AnimationState idleAnimationState = new AnimationState();
    /** One-shot attack / cast / heal animation, started by entity event 4. Client side only. */
    public final AnimationState attackAnimationState = new AnimationState();

    private static final byte EVENT_ATTACK = 4;
    private static final ResourceLocation RAGE_ID = ResourceLocation.fromNamespaceAndPath(MiddleWorld.MODID, "rage");
    private static final ResourceLocation OPENER_ID = ResourceLocation.fromNamespaceAndPath(MiddleWorld.MODID, "opener");

    private final Race race;
    private final Role role;

    private boolean enraged;
    private Entity lastStruck;
    /** Scouts run away for a moment after each hit. Read by {@link RetreatGoal}. */
    public int retreatTicks;

    public FactionMob(EntityType<? extends FactionMob> type, Level level, Race race, Role role) {
        super(type, level);
        this.race = race;
        this.role = role;
        this.xpReward = 5 + (int) (role.health / 8.0D);
        // Goals are set up here and not in registerGoals(): the Mob constructor calls
        // registerGoals() before our fields exist.
        this.setupGoals();
        this.equip();
    }

    public Race race() {
        return this.race;
    }

    public Role role() {
        return this.role;
    }

    // ------------------------------------------------------------------
    // Stats and equipment
    // ------------------------------------------------------------------

    public static AttributeSupplier.Builder createAttributes(Race race, Role role) {
        double damage = Math.max(0.5D, role.damage * race.damageMult - weaponBonus(mainHandItem(race, role)));
        double dwarfBonus = race == Race.DWARF ? 1.0D : 0.0D;
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, Math.round(role.health * race.healthMult))
                .add(Attributes.MOVEMENT_SPEED, 0.25D + race.speedBonus + role.speedBonus)
                .add(Attributes.ATTACK_DAMAGE, damage)
                .add(Attributes.FOLLOW_RANGE, role.ranged ? 28.0D : 20.0D)
                .add(Attributes.ARMOR, role.armor + 2.0D * dwarfBonus)
                .add(Attributes.KNOCKBACK_RESISTANCE, Math.min(1.0D, role.knockbackResistance + 0.2D * dwarfBonus));
    }

    /** What each race/role carries in its main hand. */
    public static Item mainHandItem(Race race, Role role) {
        switch (role) {
            case WARRIOR:
                return race == Race.GOBLIN ? Items.STONE_SWORD : race == Race.ELF ? Items.IRON_SWORD : Items.IRON_PICKAXE;
            case ARCHER:
                return Items.BOW;
            case KNIGHT:
                return race == Race.GOBLIN ? Items.STONE_SWORD : race == Race.ELF ? Items.IRON_SWORD : Items.IRON_AXE;
            case BERSERKER:
                return race == Race.GOBLIN ? Items.STONE_AXE : Items.IRON_AXE;
            case SCOUT:
                return race == Race.GOBLIN ? Items.WOODEN_SWORD : race == Race.ELF ? Items.STONE_SWORD : Items.STONE_SWORD;
            case MAGE:
                return race == Race.GOBLIN ? Items.BONE : race == Race.ELF ? Items.END_ROD : Items.BLAZE_ROD;
            case HEALER:
                return race == Race.GOBLIN ? Items.STICK : race == Race.ELF ? Items.OAK_SAPLING : Items.GOLDEN_SHOVEL;
            case ASSASSIN:
                return race == Race.ELF ? Items.GOLDEN_SWORD : Items.IRON_SWORD;
            case CAPTAIN:
                return race == Race.ELF ? Items.IRON_SWORD : Items.IRON_AXE;
            case BRUTE:
            default:
                return Items.MACE;
        }
    }

    /** Rough damage the weapon adds on top of the attribute, so totals match the Role table. */
    private static double weaponBonus(Item item) {
        if (item == Items.WOODEN_SWORD || item == Items.GOLDEN_SWORD) return 3.0D;
        if (item == Items.STONE_SWORD) return 4.0D;
        if (item == Items.IRON_SWORD) return 5.0D;
        if (item == Items.STONE_AXE || item == Items.IRON_AXE) return 8.0D;
        if (item == Items.IRON_PICKAXE) return 3.0D;
        if (item == Items.MACE) return 5.0D;
        return 0.0D;
    }

    private void equip() {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(mainHandItem(this.race, this.role)));
        if (this.role == Role.KNIGHT) {
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        }
    }

    // ------------------------------------------------------------------
    // AI
    // ------------------------------------------------------------------

    @Override
    protected void registerGoals() {
        // Intentionally empty, see the constructor.
    }

    private void setupGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        if (this.role == Role.SCOUT) {
            this.goalSelector.addGoal(1, new RetreatGoal(this));
        }
        if (this.role == Role.HEALER) {
            this.goalSelector.addGoal(1, new HealAlliesGoal(this));
        }
        if (this.role.ranged) {
            int interval = this.role == Role.ARCHER ? 30 : 50;
            float range = this.role == Role.ARCHER ? 16.0F : 13.0F;
            this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, interval, range));
        } else {
            this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        }
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        // Fight back (and call same-race mobs nearby, since getClass() is per race subclass).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        // Players: attack anyone below this race's calm rank.
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                target -> target instanceof Player player
                        && !player.isCreative()
                        && !player.isSpectator()
                        && !ManagerAccess.isCalmAround(player, this.race)));
        // Rival factions fight each other.
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                this::isRival));
    }

    private boolean isRival(LivingEntity other) {
        Faction faction = Factions.of(other);
        return faction != null && faction != this.race.faction && this.race.faction.rivals().contains(faction);
    }

    public boolean isAlly(LivingEntity other) {
        return other instanceof FactionMob mob && mob.race.faction == this.race.faction;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        this.setAggressive(this.getTarget() != null);

        if (this.retreatTicks > 0) {
            this.retreatTicks--;
        }
        if (this.role == Role.BERSERKER && !this.enraged && this.getHealth() <= this.getMaxHealth() * 0.5F) {
            this.enrage();
        }
        if (this.role == Role.CAPTAIN && this.tickCount % 60 == 0 && this.getTarget() != null) {
            this.rallyAllies();
        }
    }

    private void enrage() {
        this.enraged = true;
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (damage != null) {
            damage.addTransientModifier(new AttributeModifier(RAGE_ID, 0.5D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (speed != null) {
            speed.addTransientModifier(new AttributeModifier(RAGE_ID, 0.3D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        this.playSound(SoundEvents.VINDICATOR_CELEBRATE, 1.0F, this.getVoicePitch() * 0.8F);
        if (this.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.ANGRY_VILLAGER, this.getX(), this.getY() + this.getBbHeight(), this.getZ(),
                    6, 0.3D, 0.2D, 0.3D, 0.0D);
        }
    }

    private void rallyAllies() {
        List<LivingEntity> allies = this.level().getEntitiesOfClass(LivingEntity.class,
                this.getBoundingBox().inflate(12.0D), e -> e != this && e.isAlive() && this.isAlly(e));
        for (LivingEntity ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 0, true, true));
            if (this.level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.CRIT, ally.getX(), ally.getY() + ally.getBbHeight() + 0.2D, ally.getZ(),
                        4, 0.25D, 0.1D, 0.25D, 0.1D);
            }
        }
        if (!allies.isEmpty()) {
            this.playSound(SoundEvents.RAVAGER_ROAR, 0.6F, 1.5F);
            this.broadcastAttackEvent();
        }
    }

    // ------------------------------------------------------------------
    // Combat
    // ------------------------------------------------------------------

    public void broadcastAttackEvent() {
        this.level().broadcastEntityEvent(this, EVENT_ATTACK);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        this.broadcastAttackEvent();

        // Assassins hit twice as hard on the first strike against a new target.
        boolean opener = this.role == Role.ASSASSIN && target != this.lastStruck;
        AttributeInstance damage = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (opener && damage != null) {
            damage.addTransientModifier(new AttributeModifier(OPENER_ID, 1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        boolean hit;
        try {
            hit = super.doHurtTarget(target);
        } finally {
            if (opener && damage != null) {
                damage.removeModifier(OPENER_ID);
            }
        }
        if (!hit) {
            return false;
        }
        this.lastStruck = target;

        if (this.level() instanceof ServerLevel server) {
            if (opener) {
                server.sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + target.getBbHeight() * 0.6D,
                        target.getZ(), 10, 0.3D, 0.3D, 0.3D, 0.2D);
            }
            if (this.role == Role.BRUTE) {
                this.slam(server, target);
            }
        }
        if (this.role == Role.SCOUT) {
            this.retreatTicks = 30;
        }
        return true;
    }

    /** Brutes smash the ground: everyone near the victim takes half damage and gets thrown back. */
    private void slam(ServerLevel server, Entity victim) {
        float splash = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 0.5F;
        List<LivingEntity> nearby = server.getEntitiesOfClass(LivingEntity.class,
                victim.getBoundingBox().inflate(2.5D),
                e -> e != this && e != victim && e.isAlive() && !this.isAlly(e));
        for (LivingEntity e : nearby) {
            e.hurt(this.damageSources().mobAttack(this), splash);
            e.knockback(0.7D, this.getX() - e.getX(), this.getZ() - e.getZ());
        }
        server.sendParticles(ParticleTypes.POOF, victim.getX(), victim.getY() + 0.1D, victim.getZ(),
                12, 0.8D, 0.05D, 0.8D, 0.05D);
        this.playSound(SoundEvents.ANVIL_LAND, 0.6F, 0.7F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Knights block about a third of the attacks that come from the front with their shield.
        if (this.role == Role.KNIGHT && !this.level().isClientSide()
                && !source.is(DamageTypeTags.BYPASSES_SHIELD)
                && source.getDirectEntity() != null
                && this.getRandom().nextFloat() < 0.35F) {
            Vec3 look = this.getViewVector(1.0F);
            Vec3 toAttacker = source.getDirectEntity().position().subtract(this.position()).normalize();
            if (look.x * toAttacker.x + look.z * toAttacker.z > 0.3D) {
                this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 0.8F + this.getRandom().nextFloat() * 0.4F);
                return false;
            }
        }
        return super.hurt(source, amount);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.role == Role.ARCHER) {
            this.shootArrow(target);
        } else {
            this.castSpell(target);
        }
        this.broadcastAttackEvent();
    }

    private void shootArrow(LivingEntity target) {
        Arrow arrow = EntityType.ARROW.create(this.level());
        if (arrow == null) {
            return;
        }
        arrow.setOwner(this);
        arrow.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());
        double dx = target.getX() - this.getX();
        double dy = target.getY(0.3333333333333333D) - arrow.getY();
        double dz = target.getZ() - this.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        int difficulty = this.level().getDifficulty().getId();
        arrow.shoot(dx, dy + horizontal * 0.2D, dz, 1.6F, 14 - difficulty * 4);
        arrow.setBaseDamage(1.4D + this.race.damageMult);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(arrow);
    }

    /** Instant magic bolt. Goblins curse, elves poison, dwarves burn. */
    private void castSpell(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel server) || !this.hasLineOfSight(target)) {
            return;
        }
        ParticleOptions trail = switch (this.race) {
            case GOBLIN -> ParticleTypes.WITCH;
            case ELF -> ParticleTypes.HAPPY_VILLAGER;
            case DWARF -> ParticleTypes.FLAME;
        };
        Vec3 from = this.getEyePosition();
        Vec3 to = target.position().add(0.0D, target.getBbHeight() * 0.6D, 0.0D);
        int steps = Math.max(2, (int) (from.distanceTo(to) * 3.0D));
        for (int i = 0; i <= steps; i++) {
            Vec3 point = from.lerp(to, i / (double) steps);
            server.sendParticles(trail, point.x, point.y, point.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        this.playSound(SoundEvents.EVOKER_CAST_SPELL, 1.0F, this.getVoicePitch());

        target.hurt(this.damageSources().indirectMagic(this, this), (float) (4.0D * this.race.damageMult));
        switch (this.race) {
            case GOBLIN -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0), this);
            case ELF -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), this);
            case DWARF -> target.setRemainingFireTicks(80);
        }
    }

    // ------------------------------------------------------------------
    // Animation events, persistence, sounds
    // ------------------------------------------------------------------

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_ATTACK) {
            this.attackAnimationState.start(this.tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    /** Goblins despawn like other monsters; elves and dwarves stay where they were generated. */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return this.race.isHostileByNature();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.race == Race.ELF ? SoundEvents.EVOKER_AMBIENT : SoundEvents.VINDICATOR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.race == Race.ELF ? SoundEvents.EVOKER_HURT : SoundEvents.VINDICATOR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.race == Race.ELF ? SoundEvents.EVOKER_DEATH : SoundEvents.VINDICATOR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, this.race == Race.DWARF ? 0.8F : 1.4F);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * this.race.voicePitch;
    }
}

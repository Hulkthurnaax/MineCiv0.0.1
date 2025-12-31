package net.andrew.mineciv.block.entity.custom;
import net.andrew.mineciv.block.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CannonballEntity extends Projectile {

    private static final float EXPLOSION_POWER = 8.0f;

    public CannonballEntity(EntityType<? extends CannonballEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false);
    }

    public CannonballEntity(Level level, double x, double y, double z) {
        this(ModEntities.CANNONBALL.get(), level);
        this.setPos(x, y, z);
        this.setNoGravity(false);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synched data needed
    }

    @Override
    public void tick() {
        super.tick();

        // Spawn trail particles
        if (this.level().isClientSide) {
            // Smoke trail
            for (int i = 0; i < 3; i++) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX() + (random.nextDouble() - 0.5) * 0.2,
                        this.getY() + (random.nextDouble() - 0.5) * 0.2,
                        this.getZ() + (random.nextDouble() - 0.5) * 0.2,
                        0, -0.05, 0);
            }
            // Flame trail
            this.level().addParticle(ParticleTypes.FLAME,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    0, 0, 0);
        }

        if (!this.level().isClientSide) {
            // Server-side physics
            Vec3 movement = this.getDeltaMovement();

            // Apply gravity
            movement = movement.add(0, -0.05, 0);

            // Apply slight air resistance
            movement = movement.scale(0.99);

            // Check for collision BEFORE moving
            Vec3 currentPos = this.position();
            Vec3 nextPos = currentPos.add(movement);

            HitResult hitResult = this.level().clip(new net.minecraft.world.level.ClipContext(
                    currentPos,
                    nextPos,
                    net.minecraft.world.level.ClipContext.Block.COLLIDER,
                    net.minecraft.world.level.ClipContext.Fluid.NONE,
                    this
            ));

            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onHit(hitResult);
                return;
            }

            // Check entity collisions
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    this.level(), this, currentPos, nextPos,
                    this.getBoundingBox().expandTowards(movement).inflate(1.0),
                    this::canHitEntity
            );

            if (entityHit != null) {
                this.onHit(entityHit);
                return;
            }

            // Update position and movement
            this.setDeltaMovement(movement);
            this.setPos(nextPos.x, nextPos.y, nextPos.z);
        }

        // Remove after 200 ticks (10 seconds)
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            explode();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide) {
            explode();
        }
    }

    private void explode() {
        if (!this.level().isClientSide) {
            this.level().explode(
                    this,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    EXPLOSION_POWER,
                    Level.ExplosionInteraction.TNT
            );

            this.discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // No additional data to read
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // No additional data to save
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 16384.0; // Render up to 128 blocks away
    }
}
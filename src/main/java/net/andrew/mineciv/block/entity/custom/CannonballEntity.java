package net.andrew.mineciv.block.entity.custom;
import net.andrew.mineciv.block.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CannonballEntity extends Projectile {

    private static final float EXPLOSION_POWER = 8.0f; // 2x TNT (TNT = 4.0f)

    public CannonballEntity(EntityType<? extends CannonballEntity> entityType, Level level) {
        super(entityType, level);
    }

    public CannonballEntity(Level level, double x, double y, double z) {
        this(ModEntities.CANNONBALL.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // No synched data needed
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Apply gravity
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.05, 0));

            // Check for collision
            HitResult hitResult = this.getHitResult();
            if (hitResult.getType() != HitResult.Type.MISS) {
                this.onHit(hitResult);
            }

            // Move
            this.setPos(this.getX() + this.getDeltaMovement().x,
                    this.getY() + this.getDeltaMovement().y,
                    this.getZ() + this.getDeltaMovement().z);
        } else {
            // Spawn smoke particles on client
            for (int i = 0; i < 2; i++) {
                this.level().addParticle(ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(),
                        0, 0, 0);
            }
        }

        // Remove after 200 ticks (10 seconds)
        if (this.tickCount > 200) {
            this.discard();
        }
    }

    private HitResult getHitResult() {
        Vec3 start = this.position();
        Vec3 end = start.add(this.getDeltaMovement());
        return this.level().clip(new net.minecraft.world.level.ClipContext(
                start, end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                this
        ));
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
            // Create explosion (2x TNT power)
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
}
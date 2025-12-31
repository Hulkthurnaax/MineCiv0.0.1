package net.andrew.mineciv.item.custom;

import net.minecraft.world.item.Tier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

import java.util.List;

public class Ironclaymoreitem extends SwordItem {

    private static final float BASE_DAMAGE = 6.0f; // Iron sword is 6, so 6 * 1.6 = 9.6 for chop
    private static final float SWEEP_DAMAGE_MULTIPLIER = 1.2f;
    private static final float CHOP_DAMAGE_MULTIPLIER = 1.4f;
    private static final double EXTENDED_REACH = 1.8; // 1.8x normal reach
    private static final float SWEEP_ANGLE = 85.0f; // 75 degree cone
    private static final int SWEEP_COOLDOWN = 40; // 2 seconds (40 ticks)

    public Ironclaymoreitem(Properties properties) {
        super(
                Tiers.IRON,
                properties.attributes(SwordItem.createAttributes(
                        Tiers.IRON,
                        (int)(BASE_DAMAGE * CHOP_DAMAGE_MULTIPLIER) - 1,// Subtract 1 because base adds 1
                        -2.8f // Attack speed: Iron sword is -2.4, this is 75% speed (-2.8)
                ))
        );
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        // Left-click chop attack with 1.6x damage and extended reach
        if (attacker instanceof Player player) {
            // Extended reach is handled by the attack range attribute modifier
            double reach = player.entityInteractionRange() * EXTENDED_REACH;
            double distance = player.distanceTo(target);

            if (distance <= reach) {
                // Apply bonus damage
                float bonusDamage = BASE_DAMAGE * (CHOP_DAMAGE_MULTIPLIER - 1.0f);
                target.hurt(target.damageSources().playerAttack(player), bonusDamage);

                // Play heavy hit sound
                player.level().playSound(null, player.blockPosition(),
                        SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.PLAYERS,
                        1.0f, 0.8f);

                // Spawn impact particles
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            target.getX(), target.getY(0.5), target.getZ(),
                            5, 0.5, 0.5, 0.5, 0.0);
                }
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Right-click sweep attack
        if (!level.isClientSide) {
            if (player.getCooldowns().isOnCooldown(this)) {
                player.displayClientMessage(
                        Component.literal("Sweep attack on cooldown!"),
                        true
                );
                return InteractionResultHolder.fail(stack);
            }

            // Perform sweep attack
            performSweepAttack(player, level);

            // Set cooldown
            player.getCooldowns().addCooldown(this, SWEEP_COOLDOWN);

            // Damage the weapon
            stack.hurtAndBreak(2, player, EquipmentSlot.MAINHAND);
        }

        return InteractionResultHolder.success(stack);
    }

    private void performSweepAttack(Player player, Level level) {
        Vec3 playerPos = player.position();
        Vec3 lookVec = player.getLookAngle();

        // Calculate sweep range
        double sweepRange = player.entityInteractionRange() * EXTENDED_REACH;

        // Create a cone-shaped hitbox in front of player
        AABB searchBox = new AABB(
                playerPos.x - sweepRange, playerPos.y - 1, playerPos.z - sweepRange,
                playerPos.x + sweepRange, playerPos.y + 3, playerPos.z + sweepRange
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(
                LivingEntity.class,
                searchBox,
                entity -> entity != player && entity.isAlive()
        );

        int hitCount = 0;
        float sweepDamage = BASE_DAMAGE * SWEEP_DAMAGE_MULTIPLIER;

        for (LivingEntity entity : entities) {
            Vec3 toEntity = entity.position().subtract(playerPos).normalize();

            // Check if entity is within the sweep cone angle
            double dotProduct = lookVec.dot(toEntity);
            double angle = Math.toDegrees(Math.acos(dotProduct));

            if (angle <= SWEEP_ANGLE / 2.0 && player.distanceTo(entity) <= sweepRange) {
                // Apply sweep damage
                entity.hurt(level.damageSources().playerAttack(player), sweepDamage);

                // Knockback effect
                Vec3 knockback = toEntity.multiply(0.5, 0.3, 0.5);
                entity.setDeltaMovement(entity.getDeltaMovement().add(knockback));

                hitCount++;

                // Spawn hit particles
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK,
                            entity.getX(), entity.getY(0.5), entity.getZ(),
                            3, 0.3, 0.3, 0.3, 0.0);
                }
            }
        }

        // Play sweep sound
        level.playSound(null, player.blockPosition(),
                SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
                1.0f, 1.0f);

        // Visual effect - create particle arc
        if (level instanceof ServerLevel serverLevel) {
            createSweepParticles(serverLevel, player, lookVec, sweepRange);
        }

        // Display feedback
        if (hitCount > 0) {
            player.displayClientMessage(
                    Component.literal("Sweep hit " + hitCount + " enemies!"),
                    true
            );
        }
    }

    private void createSweepParticles(ServerLevel level, Player player, Vec3 lookVec, double range) {
        Vec3 playerPos = player.position().add(0, 1, 0);

        // Create a wide arc of particles
        for (int i = -20; i <= 20; i++) {
            double angle = Math.toRadians(i * (SWEEP_ANGLE / 40.0));

            // Rotate the look vector
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            Vec3 rotated = new Vec3(
                    lookVec.x * cos - lookVec.z * sin,
                    lookVec.y,
                    lookVec.x * sin + lookVec.z * cos
            );

            for (double dist = 0.5; dist <= range; dist += 0.3) {
                Vec3 particlePos = playerPos.add(rotated.scale(dist));

                level.sendParticles(ParticleTypes.SWEEP_ATTACK,
                        particlePos.x, particlePos.y, particlePos.z,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        // Add dramatic crit particles
        level.sendParticles(ParticleTypes.CRIT,
                playerPos.x + lookVec.x * 2,
                playerPos.y,
                playerPos.z + lookVec.z * 2,
                15, 1.0, 0.5, 1.0, 0.1);


    }
    @Override
    public int getEnchantmentValue() {
        return 14; // Same as iron tools
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return repairCandidate.is(net.minecraft.world.item.Items.IRON_INGOT) || super.isValidRepairItem(stack, repairCandidate);
    }
}

package net.andrew.mineciv.block.entity.custom;
import net.andrew.mineciv.block.custom.CannonMenu;
import net.andrew.mineciv.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.network.IContainerFactory;

public class CannonBlockEntity extends BlockEntity implements MenuProvider {

    private static final int TNT_SLOT = 0;
    private static final int REDSTONE_SLOT = 1;
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(this.worldPosition);
    }


    private final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == TNT_SLOT) {
                return stack.is(Items.TNT);
            } else if (slot == REDSTONE_SLOT) {
                return stack.is(Items.REDSTONE);
            }
            return false;
        }
    };

    private float pitch = 45.0f; // Elevation angle (0-90 degrees)
    private float power = 1.0f;  // Power multiplier (0.5-2.0)

    public CannonBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CANNON_BE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public float getPitch() {
        return pitch;
    }

    public void setPitch(float pitch) {
        this.pitch = Math.max(0, Math.min(90, pitch));
        setChanged();
    }

    public float getPower() {
        return power;
    }

    public void setPower(float power) {
        this.power = Math.max(0.5f, Math.min(2.0f, power));
        setChanged();
    }

    public boolean canFire() {
        ItemStack tnt = inventory.getStackInSlot(TNT_SLOT);
        ItemStack redstone = inventory.getStackInSlot(REDSTONE_SLOT);
        return !tnt.isEmpty() && !redstone.isEmpty();
    }

    public void fire(Player player) {
        if (!canFire() || level == null || level.isClientSide) {
            return;
        }

        // Consume ammunition
        inventory.extractItem(TNT_SLOT, 1, false);
        inventory.extractItem(REDSTONE_SLOT, 1, false);

        // Get cannon direction
        Direction facing = getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);

        // Calculate spawn position (in front of cannon)
        Vec3 cannonPos = Vec3.atCenterOf(worldPosition);
        Vec3 offset = Vec3.atLowerCornerOf(facing.getNormal()).scale(2.0);
        Vec3 spawnPos = cannonPos.add(offset).add(0, 0.5, 0);

        // Calculate velocity based on pitch and power
        double radianPitch = Math.toRadians(pitch);
        double horizontalSpeed = Math.cos(radianPitch) * power * 2.0;
        double verticalSpeed = Math.sin(radianPitch) * power * 2.0;

        Vec3 velocity = Vec3.atLowerCornerOf(facing.getNormal())
                .scale(horizontalSpeed)
                .add(0, verticalSpeed, 0);

        // Spawn cannonball entity
        CannonballEntity cannonball = new CannonballEntity(level, spawnPos.x, spawnPos.y, spawnPos.z);
        cannonball.setDeltaMovement(velocity);
        cannonball.setOwner(player);

        level.addFreshEntity(cannonball);

        // Play sound
        level.playSound(null, worldPosition,
                net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE.value(),
                net.minecraft.sounds.SoundSource.BLOCKS,
                2.0f, 0.8f);

        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putFloat("Pitch", pitch);
        tag.putFloat("Power", power);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        pitch = tag.getFloat("Pitch");
        power = tag.getFloat("Power");
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Cannon");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new CannonMenu(containerId, playerInventory, this);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }
}
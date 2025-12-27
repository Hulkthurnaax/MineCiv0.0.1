package net.andrew.mineciv.block.custom;
import net.andrew.mineciv.ModMenuTypes;
import net.andrew.mineciv.block.entity.custom.CannonBlockEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.UnknownNullability;

public class CannonMenu extends AbstractContainerMenu {

    private final CannonBlockEntity cannonEntity;

    public CannonMenu(int containerId, Inventory playerInventory, @UnknownNullability CannonBlockEntity cannonEntity) {
        super(ModMenuTypes.CANNON_MENU.get(), containerId);
        this.cannonEntity = cannonEntity;


        // Cannon inventory slots
        this.addSlot(new SlotItemHandler(cannonEntity.getInventory(), 0, 44, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.TNT);
            }
        }); // TNT slot

        this.addSlot(new SlotItemHandler(cannonEntity.getInventory(), 1, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.REDSTONE);
            }
        }); // Redstone slot

        // Player inventory
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        // Player hotbar
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public CannonBlockEntity getCannonEntity() {
        return cannonEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();

            if (index < 2) {
                // Moving from cannon to player inventory
                if (!this.moveItemStackTo(stack, 2, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Moving from player inventory to cannon
                if (stack.is(Items.TNT)) {
                    if (!this.moveItemStackTo(stack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (stack.is(Items.REDSTONE)) {
                    if (!this.moveItemStackTo(stack, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return cannonEntity.getLevel() != null &&
                player.distanceToSqr(cannonEntity.getBlockPos().getX() + 0.5,
                        cannonEntity.getBlockPos().getY() + 0.5,
                        cannonEntity.getBlockPos().getZ() + 0.5) <= 64;
    }
}
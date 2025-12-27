package net.andrew.mineciv.block.entity;

import net.andrew.mineciv.MineCiv;
import net.andrew.mineciv.block.ModBlocks;
import net.andrew.mineciv.block.entity.custom.CannonBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static  final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MineCiv.MOD_ID);

    public static final RegistryObject<BlockEntityType<CannonBlockEntity>> CANNON_BE =
            BLOCK_ENTITIES.register("cannon_be",() -> BlockEntityType.Builder.of(
                    CannonBlockEntity::new, ModBlocks.CANNON.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

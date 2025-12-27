package net.andrew.mineciv.block;

import net.andrew.mineciv.MineCiv;
import net.andrew.mineciv.block.custom.CannonBlock;
import net.andrew.mineciv.item.Moditems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

import static com.ibm.icu.lang.UCharacter.GraphemeClusterBreak.T;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MineCiv.MOD_ID);

    public static final RegistryObject<Block> MONUMENT_BLOCK = registerBlock("monument_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .explosionResistance(10).sound(SoundType.ANVIL)));


    public static final RegistryObject<Block> CANNON = registerBlock("cannon",
            () -> new CannonBlock(BlockBehaviour.Properties.of()
                    .strength(5.0f, 6.0f)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
            )
    );


    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }


    private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block){
      Moditems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}

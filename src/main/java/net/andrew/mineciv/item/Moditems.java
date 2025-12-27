package net.andrew.mineciv.item;

import net.andrew.mineciv.MineCiv;
import net.andrew.mineciv.block.ModBlocks;
import net.andrew.mineciv.item.custom.WoodwandItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class Moditems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MineCiv.MOD_ID);

    public static final RegistryObject<Item> MONUMENT = ITEMS.register("monument",
            () -> new Item(new Item.Properties()));

   public static final RegistryObject<Item> WOODWAND = ITEMS.register("woodwand",
           () -> new WoodwandItem(new Item.Properties().durability(16)));

    public static final RegistryObject<Item> CANNON = ITEMS.register("cannon",
            () -> new BlockItem(ModBlocks.CANNON.get(), new Item.Properties())
    );

   public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

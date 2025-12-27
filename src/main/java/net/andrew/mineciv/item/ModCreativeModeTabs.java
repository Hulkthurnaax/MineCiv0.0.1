package net.andrew.mineciv.item;

import com.mojang.brigadier.LiteralMessage;
import net.andrew.mineciv.MineCiv;
import net.andrew.mineciv.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.objectweb.asm.tree.FieldInsnNode;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MineCiv.MOD_ID);

public static final RegistryObject<CreativeModeTab> MONUMENT_ITEMS_TAB = CREATIVE_MODE_TABS.register("monument_items_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(Moditems.MONUMENT.get()))
                .title(Component.translatable("creativetab.mineciv.monument_items"))
                .displayItems(new CreativeModeTab.DisplayItemsGenerator() {
                                  @Override
                                  public void accept(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
                                      pOutput.accept(Moditems.MONUMENT.get());

                                      pOutput.accept(Moditems.WOODWAND.get());

                                      pOutput.accept(ModBlocks.CANNON.get());


                                  }
                              })
                .build());

public static final RegistryObject<CreativeModeTab> MONUMENT_BLOCKS_TAB = CREATIVE_MODE_TABS.register("monument_blocks_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.MONUMENT_BLOCK.get()))
                .title(Component.translatable("creativetab.mineciv.monument_items"))
                .displayItems(new CreativeModeTab.DisplayItemsGenerator() {
                                  @Override
                                  public void accept(CreativeModeTab.ItemDisplayParameters pParameters, CreativeModeTab.Output pOutput) {
                                      pOutput.accept(ModBlocks.MONUMENT_BLOCK.get());
                                  }
                              })
                .build());



    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}

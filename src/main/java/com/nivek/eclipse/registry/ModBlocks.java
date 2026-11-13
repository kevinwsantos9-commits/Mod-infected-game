package com.nivek.eclipse.registry;

import com.nivek.eclipse.NivekEclipse;
import com.nivek.eclipse.block.AlienCoreBlock;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static final Block ALIEN_CORE = registerBlock("alien_core", AlienCoreBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PURPLE)
                    .strength(-1.0F, 3600000.0F)
                    .sound(SoundType.AMETHYST)
                    .lightLevel(state -> 10));

    public static final Item ALIEN_CORE_ITEM = registerBlockItem("alien_core", ALIEN_CORE);

    private ModBlocks() {}

    private static Block registerBlock(String name, java.util.function.Function<BlockBehaviour.Properties, Block> factory,
                                       BlockBehaviour.Properties props) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK,
                ResourceLocation.fromNamespaceAndPath(NivekEclipse.MOD_ID, name));
        Block block = factory.apply(props.setId(key));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    private static Item registerBlockItem(String name, Block block) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(NivekEclipse.MOD_ID, name));
        BlockItem item = new BlockItem(block, new Item.Properties().setId(key).useBlockDescriptionPrefix());
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static void init() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(ALIEN_CORE_ITEM));
    }
}

package example;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(ExampleForgeMod.MOD_ID)

public class ExampleForgeMod {
    public static final String MOD_ID = "ladder-example-mod";

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> event) {
            event.getRegistry().register(ExampleBlocks.EXAMPLE_BLOCK.setRegistryName("example_block"));
        }

        @SubscribeEvent
        public static void onItemsRegistry(final RegistryEvent.Register<Item> event) {
            Item.Properties properties = new Item.Properties().group(ItemGroup.FOOD);
            event.getRegistry().register(new BlockItem(ExampleBlocks.EXAMPLE_BLOCK,properties).setRegistryName("example_block"));
        }
    }

}


package example;


import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ExampleFabricMod implements ModInitializer {

    public static final String MOD_ID = "iridis-example-mod";

    @Override
    public void onInitialize() {
//        ExampleBlock.operation(Blocks.ACACIA_BUTTON);
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "example_block"), (Block) (Object) ExampleBlocks.EXAMPLE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "example_block"),
                new BlockItem((Block) (Object) ExampleBlocks.EXAMPLE_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
    }

}


package example;


import net.fabricmc.api.ModInitializer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class ExampleFabricMod implements ModInitializer {

    public static final String MOD_ID = "ladder-example-mod";

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "example_block"), ExampleBlocks.EXAMPLE_BLOCK);
        Registry.register(Registry.ITEM, new Identifier(MOD_ID, "example_block"),
                new BlockItem(ExampleBlocks.EXAMPLE_BLOCK, new Item.Settings().group(ItemGroup.MISC)));
    }

}

package example;

import v1_16_1.net.minecraft.block.BaseBlock;
import v1_16_1.net.minecraft.block.IBlock;

public class ExampleBlock extends BaseBlock {
    public ExampleBlock(Settings settings) {
        super(settings);
    }

    public static void operation(IBlock foo){
//        foo.
    }

    @Override
    public String getTranslationKey() {
        return "example.translation.key";
    }
}

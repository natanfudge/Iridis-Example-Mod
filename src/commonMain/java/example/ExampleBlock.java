package example;

import v1_16_1.net.minecraft.block.BaseBlock;
import v1_16_1.net.minecraft.block.IBlock;
import v1_16_1.net.minecraft.entity.IEntity;
import v1_16_1.net.minecraft.util.math.IBlockPos;
import v1_16_1.net.minecraft.util.registry.IRegistry;
import v1_16_1.net.minecraft.world.IWorld;

public class ExampleBlock extends BaseBlock {
    public ExampleBlock(Settings settings) {
        super(settings);
    }

//    public static void operation(IBlock foo, IEntity bar, IRegistry baz){
//        IBlock.create()
//    }

    @Override
    public String getTranslationKey() {
        return "example.translation.key";
    }

    @Override
    protected void dropExperience(IWorld world, IBlockPos pos, int size) {
        super.dropExperience(world, pos, size);
    }
}

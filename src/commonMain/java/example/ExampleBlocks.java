package example;


import v1_16_1.net.minecraft.block.IBlock;
import v1_16_1.net.minecraft.block.IMaterial;

public class ExampleBlocks {

    IMaterial mat = IMaterial.getMETAL();
    public static final ExampleBlock EXAMPLE_BLOCK = new ExampleBlock(IBlock.Settings.of(IMaterial.getMETAL()));

}

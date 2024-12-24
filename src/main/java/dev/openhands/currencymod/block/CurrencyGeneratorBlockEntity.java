package dev.openhands.currencymod.block;

import dev.openhands.currencymod.CurrencyMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CurrencyGeneratorBlockEntity extends BlockEntity {
    private static final float GENERATION_RATE = 0.005f; // Currency per tick
    private float storedCurrency = 0.0f;
    private static BlockEntityType<CurrencyGeneratorBlockEntity> TYPE;
    
    public CurrencyGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(TYPE, pos, state);
    }
    
    public static void setType(BlockEntityType<CurrencyGeneratorBlockEntity> type) {
        TYPE = type;
    }
    
    public static void tick(World world, BlockPos pos, BlockState state, CurrencyGeneratorBlockEntity be) {
        if (world.isClient) return;
        
        be.storedCurrency += GENERATION_RATE;
        be.markDirty();
    }
    
    public Text getStatusMessage() {
        return Text.literal(String.format("Stored Currency: %.2f", storedCurrency));
    }
    
    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putFloat("stored_currency", storedCurrency);
    }
    
    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        storedCurrency = nbt.getFloat("stored_currency");
    }
}
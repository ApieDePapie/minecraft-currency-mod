package dev.openhands.currencymod.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.openhands.currencymod.CurrencyMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CurrencyGeneratorBlockEntity extends BlockEntity {
    private static final float GENERATION_RATE = 0.005f; // Currency per tick
    private float storedCurrency = 0.0f;
    
    public static final Codec<CurrencyGeneratorBlockEntity> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.FLOAT.fieldOf("stored_currency").forGetter(be -> be.storedCurrency)
        ).apply(instance, storedCurrency -> {
            CurrencyGeneratorBlockEntity be = new CurrencyGeneratorBlockEntity(BlockPos.ORIGIN, null);
            be.storedCurrency = storedCurrency;
            return be;
        })
    );
    
    public CurrencyGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(CurrencyMod.CURRENCY_GENERATOR_ENTITY, pos, state);
    }
    
    public static void tick(World world, BlockPos pos, BlockState state, CurrencyGeneratorBlockEntity be) {
        if (world.isClient) return;
        
        be.storedCurrency += GENERATION_RATE;
        be.markDirty();
        world.updateListeners(pos, state, state, 3);
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
    
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
    
    @Override
    public NbtCompound toInitialChunkDataNbt() {
        return createNbt();
    }
}
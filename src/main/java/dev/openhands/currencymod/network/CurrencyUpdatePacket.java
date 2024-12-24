package dev.openhands.currencymod.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import dev.openhands.currencymod.CurrencyMod;

public class CurrencyUpdatePacket {
    public static final Identifier ID = new Identifier(CurrencyMod.MOD_ID, "currency_update");
    private final float currency;

    public CurrencyUpdatePacket(float currency) {
        this.currency = currency;
    }

    public static void write(PacketByteBuf buf, float currency) {
        buf.writeFloat(currency);
    }

    public static float read(PacketByteBuf buf) {
        return buf.readFloat();
    }

    public float getCurrency() {
        return currency;
    }
}
package dev.openhands.currencymod.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;

public class CurrencyUpdateS2CPacket {
    private static float lastKnownCurrency = 0.0f;

    public static void receive(PacketByteBuf buf, PacketSender responseSender) {
        float currency = buf.readFloat();
        lastKnownCurrency = currency;
    }

    public static float getLastKnownCurrency() {
        return lastKnownCurrency;
    }
}
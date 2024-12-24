package dev.openhands.currencymod.network;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class CurrencyUpdateS2CPacket {
    private static float lastKnownCurrency = 0.0f;

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(CurrencyUpdatePacket.ID, (client, handler, buf, responseSender) -> {
            float currency = CurrencyUpdatePacket.read(buf);
            client.execute(() -> lastKnownCurrency = currency);
        });
    }

    public static float getLastKnownCurrency() {
        return lastKnownCurrency;
    }
}
package dev.openhands.currencymod.network;

import dev.openhands.currencymod.CurrencyMod;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public class CurrencyUpdatePacket {
    public static void send(ServerPlayerEntity player, float currency) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeFloat(currency);
        ServerPlayNetworking.send(player, CurrencyMod.CURRENCY_UPDATE, buf);
    }
}
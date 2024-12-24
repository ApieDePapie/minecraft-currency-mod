package dev.openhands.currencymod.client;

import dev.openhands.currencymod.CurrencyMod;
import dev.openhands.currencymod.network.CurrencyUpdateS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class CurrencyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register currency update packet receiver
        ClientPlayNetworking.registerGlobalReceiver(CurrencyMod.CURRENCY_UPDATE, CurrencyUpdateS2CPacket::receive);

        // Register HUD renderer
        HudRenderCallback.EVENT.register((matrixStack, tickDelta) -> {
            renderCurrencyHud(matrixStack);
        });
    }

    private void renderCurrencyHud(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        TextRenderer textRenderer = client.textRenderer;
        String currencyText = String.format("Currency: %.2f", CurrencyUpdateS2CPacket.getLastKnownCurrency());
        
        int x = 5;
        int y = 5;
        
        // Draw shadow first
        textRenderer.draw(matrices, Text.literal(currencyText), x + 1, y + 1, 0x000000);
        // Draw text
        textRenderer.draw(matrices, Text.literal(currencyText), x, y, 0xFFFF00);
    }
}
package dev.openhands.currencymod.client;

import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import dev.openhands.currencymod.CurrencyMod;
import dev.openhands.currencymod.network.CurrencyUpdateS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class CurrencyModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register currency update packet receiver
        CurrencyUpdateS2CPacket.register();

        // Register HUD renderer
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            renderCurrencyHud(drawContext);
        });
    }

    private void renderCurrencyHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        String currencyText = String.format("Currency: %.2f", CurrencyUpdateS2CPacket.getLastKnownCurrency());
        
        int x = 5;
        int y = 5;
        
        // Draw shadow first
        context.drawText(client.textRenderer, currencyText, x + 1, y + 1, 0x000000, false);
        // Draw text
        context.drawText(client.textRenderer, currencyText, x, y, 0xFFFF00, false);
    }
}
package dev.openhands.currencymod.client;

import dev.openhands.currencymod.CurrencyMod;
import dev.openhands.currencymod.network.CurrencyUpdateS2CPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CurrencyModClient implements ClientModInitializer {
    private static final Identifier CURRENCY_ICON = new Identifier(CurrencyMod.MOD_ID, "textures/gui/currency_icon.png");
    private static final int ICON_SIZE = 16;
    private static final int PADDING = 4;
    
    private static KeyBinding upgradeScreenKey;
    
    @Override
    public void onInitializeClient() {
        // Register HUD renderer
        HudRenderCallback.EVENT.register(this::renderCurrencyHud);
        
        // Register keybinding for upgrade screen
        upgradeScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.currencymod.open_upgrades",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_U,
            "category.currencymod.general"
        ));
        
        // Register tick event for handling keybinding
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (upgradeScreenKey.wasPressed()) {
                if (client.player != null) {
                    client.setScreen(new UpgradeScreen());
                }
            }
        });
        
        // Register network handler for currency updates
        ClientPlayNetworking.registerGlobalReceiver(CurrencyMod.CURRENCY_UPDATE, CurrencyUpdateS2CPacket::receive);
    }
    
    private void renderCurrencyHud(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        float currency = CurrencyUpdateS2CPacket.getLastKnownCurrency();
        String currencyText = String.format("%.2f", currency);
        
        int screenWidth = client.getWindow().getScaledWidth();
        int x = screenWidth - PADDING - ICON_SIZE;
        int y = PADDING;
        
        // Draw currency icon (placeholder until image is provided)
        context.drawTexture(CURRENCY_ICON, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        
        // Draw currency amount
        context.drawText(
            client.textRenderer,
            Text.literal(currencyText),
            x - client.textRenderer.getWidth(currencyText) - PADDING,
            y + (ICON_SIZE - client.textRenderer.fontHeight) / 2,
            0xFFFFFF,
            true
        );
    }
}
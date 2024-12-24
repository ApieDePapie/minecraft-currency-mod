package dev.openhands.currencymod.client;

import dev.openhands.currencymod.CurrencyMod;
import dev.openhands.currencymod.upgrade.Upgrade;
import dev.openhands.currencymod.upgrade.UpgradeManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class UpgradeScreen extends Screen {
    private final List<ButtonWidget> upgradeButtons = new ArrayList<>();
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int PADDING = 10;
    
    protected UpgradeScreen() {
        super(Text.literal("Upgrades"));
    }
    
    @Override
    protected void init() {
        super.init();
        upgradeButtons.clear();
        
        int startY = 50;
        int x = (width - BUTTON_WIDTH) / 2;
        
        for (Upgrade upgrade : UpgradeManager.getUpgrades().values()) {
            int level = UpgradeManager.getUpgradeLevel(client.player, upgrade.getId());
            float cost = upgrade.getCostForLevel(level);
            
            ButtonWidget button = ButtonWidget.builder(
                Text.literal(String.format("%s (Level %d) - %.2f", upgrade.getName(), level, cost)),
                b -> {
                    if (UpgradeManager.purchaseUpgrade(client.player, upgrade.getId())) {
                        init(); // Refresh the screen
                    }
                })
                .dimensions(x, startY, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
            
            addDrawableChild(button);
            upgradeButtons.add(button);
            startY += BUTTON_HEIGHT + PADDING;
        }
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        
        // Draw title
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 10, 0xFFFFFF);
        
        // Draw current currency
        float currency = CurrencyMod.getPlayerCurrency(client.player);
        String currencyText = String.format("Current Currency: %.2f", currency);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(currencyText), width / 2, 30, 0xFFFFFF);
        
        super.render(context, mouseX, mouseY, delta);
        
        // Draw upgrade descriptions on hover
        for (ButtonWidget button : upgradeButtons) {
            if (button.isHovered()) {
                int index = upgradeButtons.indexOf(button);
                Upgrade upgrade = new ArrayList<>(UpgradeManager.getUpgrades().values()).get(index);
                List<Text> tooltip = List.of(
                    Text.literal(upgrade.getDescription()),
                    Text.literal(String.format("Current Level: %d", UpgradeManager.getUpgradeLevel(client.player, upgrade.getId())))
                );
                context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
            }
        }
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
}
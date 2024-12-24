package dev.openhands.currencymod.upgrade;

import dev.openhands.currencymod.CurrencyMod;
import dev.openhands.currencymod.data.PlayerCurrencyData;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class UpgradeManager {
    private static final Map<String, Upgrade> UPGRADES = new HashMap<>();
    
    static {
        // Register default upgrades
        registerUpgrade(new Upgrade(
            "income_multiplier",
            "Income Multiplier",
            "Increases your passive income",
            100.0f,  // Base cost
            1.5f,    // Cost multiplier per level
            0.5f,    // Base effect (50% increase)
            1.2f     // Effect multiplier per level
        ));
        
        registerUpgrade(new Upgrade(
            "offline_earnings",
            "Offline Earnings",
            "Earn currency while offline",
            500.0f,  // Base cost
            2.0f,    // Cost multiplier per level
            0.1f,    // Base effect (10% of normal rate)
            1.5f     // Effect multiplier per level
        ));
    }
    
    public static void registerUpgrade(Upgrade upgrade) {
        UPGRADES.put(upgrade.getId(), upgrade);
    }
    
    public static Upgrade getUpgrade(String id) {
        return UPGRADES.get(id);
    }
    
    public static int getUpgradeLevel(PlayerEntity player, String upgradeId) {
        return PlayerCurrencyData.getServerState(player.getWorld()).getUpgradeLevel(player, upgradeId);
    }
    
    public static boolean purchaseUpgrade(PlayerEntity player, String upgradeId) {
        Upgrade upgrade = getUpgrade(upgradeId);
        if (upgrade == null) return false;
        
        int currentLevel = getUpgradeLevel(player, upgradeId);
        float cost = upgrade.getCostForLevel(currentLevel);
        
        if (CurrencyMod.spendPlayerCurrency(player, cost)) {
            PlayerCurrencyData.getServerState(player.getWorld())
                .setUpgradeLevel(player, upgradeId, currentLevel + 1);
            return true;
        }
        
        return false;
    }
    
    public static float getTotalIncomeMultiplier(PlayerEntity player) {
        Upgrade incomeUpgrade = getUpgrade("income_multiplier");
        int level = getUpgradeLevel(player, "income_multiplier");
        return 1.0f + incomeUpgrade.getEffectForLevel(level);
    }
    
    public static float getOfflineEarningsRate(PlayerEntity player) {
        Upgrade offlineUpgrade = getUpgrade("offline_earnings");
        int level = getUpgradeLevel(player, "offline_earnings");
        return offlineUpgrade.getEffectForLevel(level);
    }
    
    public static Map<String, Upgrade> getUpgrades() {
        return new HashMap<>(UPGRADES);
    }
}
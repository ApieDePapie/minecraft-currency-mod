package dev.openhands.currencymod;

import dev.openhands.currencymod.block.CurrencyGeneratorBlock;
import dev.openhands.currencymod.block.CurrencyGeneratorBlockEntity;
import dev.openhands.currencymod.upgrade.UpgradeManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrencyMod implements ModInitializer {
    public static final String MOD_ID = "currencymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static final String CURRENCY_KEY = "currency";
    private static final String LAST_TICK_KEY = "lastTick";
    private static final float BASE_CURRENCY_PER_TICK = 0.001f; // Base currency earned per tick
    
    public static final Block CURRENCY_GENERATOR = new CurrencyGeneratorBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK));
    public static BlockEntityType<CurrencyGeneratorBlockEntity> CURRENCY_GENERATOR_ENTITY;
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Currency Mod");
        
        // Register block and block entity
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "currency_generator"), CURRENCY_GENERATOR);
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "currency_generator"),
            new BlockItem(CURRENCY_GENERATOR, new FabricItemSettings()));
        
        CURRENCY_GENERATOR_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(MOD_ID, "currency_generator"),
            FabricBlockEntityTypeBuilder.create(CurrencyGeneratorBlockEntity::new, CURRENCY_GENERATOR).build()
        );
        CurrencyGeneratorBlockEntity.setType(CURRENCY_GENERATOR_ENTITY);
        
        // Register server tick event for passive currency earning
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updatePlayerCurrency(player);
            }
        });
    }
    
    private void updatePlayerCurrency(PlayerEntity player) {
        NbtCompound persistentData = player.getPersistentData();
        float currentCurrency = persistentData.getFloat(CURRENCY_KEY);
        long lastTick = persistentData.getLong(LAST_TICK_KEY);
        long currentTick = player.getWorld().getTime();
        
        // First time initialization
        if (lastTick == 0) {
            lastTick = currentTick;
            persistentData.putLong(LAST_TICK_KEY, lastTick);
            persistentData.putFloat(CURRENCY_KEY, 0.0f);
            return;
        }
        
        // Calculate ticks passed and update currency
        long ticksPassed = currentTick - lastTick;
        if (ticksPassed > 0) {
            float multiplier = UpgradeManager.getTotalIncomeMultiplier(player);
            float earned = ticksPassed * BASE_CURRENCY_PER_TICK * multiplier;
            currentCurrency += earned;
            persistentData.putFloat(CURRENCY_KEY, currentCurrency);
            persistentData.putLong(LAST_TICK_KEY, currentTick);
        }
    }
    
    public static float getPlayerCurrency(PlayerEntity player) {
        return player.getPersistentData().getFloat(CURRENCY_KEY);
    }
    
    public static void setPlayerCurrency(PlayerEntity player, float amount) {
        player.getPersistentData().putFloat(CURRENCY_KEY, amount);
    }
    
    public static boolean spendPlayerCurrency(PlayerEntity player, float amount) {
        float current = getPlayerCurrency(player);
        if (current >= amount) {
            setPlayerCurrency(player, current - amount);
            return true;
        }
        return false;
    }
}
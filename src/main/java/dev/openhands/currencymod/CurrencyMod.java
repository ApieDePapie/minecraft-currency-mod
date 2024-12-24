package dev.openhands.currencymod;

import dev.openhands.currencymod.block.CurrencyGeneratorBlock;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import dev.openhands.currencymod.block.CurrencyGeneratorBlockEntity;
import dev.openhands.currencymod.data.PlayerCurrencyData;
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
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurrencyMod implements ModInitializer {
    public static final String MOD_ID = "currencymod";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier CURRENCY_UPDATE = new Identifier(MOD_ID, "currency_update");
    
    private static final float BASE_CURRENCY_PER_TICK = 0.001f; // Base currency earned per tick
    
    public static final Block CURRENCY_GENERATOR = Registry.register(
        Registries.BLOCK,
        new Identifier(MOD_ID, "currency_generator"),
        new CurrencyGeneratorBlock(FabricBlockSettings.copyOf(Blocks.IRON_BLOCK))
    );
    
    public static final BlockEntityType<CurrencyGeneratorBlockEntity> CURRENCY_GENERATOR_ENTITY = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        new Identifier(MOD_ID, "currency_generator"),
        FabricBlockEntityTypeBuilder.create(CurrencyGeneratorBlockEntity::new, CURRENCY_GENERATOR).build()
    );
    
    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Currency Mod");
        
        // Register block item
        Registry.register(
            Registries.ITEM,
            new Identifier(MOD_ID, "currency_generator"),
            new BlockItem(CURRENCY_GENERATOR, new FabricItemSettings())
        );
        
        // Register server tick event for passive currency earning
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                updatePlayerCurrency(player);
            }
        });
    }
    
    private void updatePlayerCurrency(PlayerEntity player) {
        PlayerCurrencyData data = PlayerCurrencyData.getServerState(player.getWorld());
        float currentCurrency = data.getCurrency(player);
        long lastTick = data.getLastTick(player);
        long currentTick = player.getWorld().getTime();
        
        // First time initialization
        if (lastTick == 0) {
            lastTick = currentTick;
            data.setLastTick(player, lastTick);
            data.setCurrency(player, 0.0f);
            return;
        }
        
        // Calculate ticks passed and update currency
        long ticksPassed = currentTick - lastTick;
        if (ticksPassed > 0) {
            float multiplier = UpgradeManager.getTotalIncomeMultiplier(player);
            float earned = ticksPassed * BASE_CURRENCY_PER_TICK * multiplier;
            currentCurrency += earned;
            data.setCurrency(player, currentCurrency);
            data.setLastTick(player, currentTick);
            
            // Send currency update to client
            if (player instanceof ServerPlayerEntity serverPlayer) {
                var buf = PacketByteBufs.create();
                buf.writeFloat(currentCurrency);
                ServerPlayNetworking.send(serverPlayer, CURRENCY_UPDATE, buf);
            }
        }
    }
    
    public static float getPlayerCurrency(PlayerEntity player) {
        if (player.getWorld().isClient()) {
            return 0.0f; // Return default value on client side
        }
        PlayerCurrencyData data = PlayerCurrencyData.getServerState(player.getWorld());
        return data != null ? data.getCurrency(player) : 0.0f;
    }
    
    public static void setPlayerCurrency(PlayerEntity player, float amount) {
        if (player.getWorld().isClient()) {
            return; // Do nothing on client side
        }
        PlayerCurrencyData data = PlayerCurrencyData.getServerState(player.getWorld());
        if (data != null) {
            data.setCurrency(player, amount);
            
            // Send currency update to client
            if (player instanceof ServerPlayerEntity serverPlayer) {
                var buf = PacketByteBufs.create();
                buf.writeFloat(amount);
                ServerPlayNetworking.send(serverPlayer, CURRENCY_UPDATE, buf);
            }
        }
    }
    
    public static boolean spendPlayerCurrency(PlayerEntity player, float amount) {
        if (player.getWorld().isClient()) {
            return false; // Cannot spend currency on client side
        }
        PlayerCurrencyData data = PlayerCurrencyData.getServerState(player.getWorld());
        if (data == null) {
            return false;
        }
        float current = data.getCurrency(player);
        if (current >= amount) {
            data.setCurrency(player, current - amount);
            return true;
        }
        return false;
    }
}
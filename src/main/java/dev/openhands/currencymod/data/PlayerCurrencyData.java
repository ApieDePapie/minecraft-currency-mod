package dev.openhands.currencymod.data;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.PersistentState.Type;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerCurrencyData extends PersistentState {
    private final Map<UUID, Float> currencyMap = new HashMap<>();
    private final Map<UUID, Long> lastTickMap = new HashMap<>();
    private final Map<UUID, Map<String, Integer>> upgradesMap = new HashMap<>();

    public static PlayerCurrencyData createFromNbt(NbtCompound tag) {
        PlayerCurrencyData data = new PlayerCurrencyData();
        NbtCompound currencyTag = tag.getCompound("currency");
        NbtCompound lastTickTag = tag.getCompound("lastTick");
        NbtCompound upgradesTag = tag.getCompound("upgrades");

        currencyTag.getKeys().forEach(key -> {
            data.currencyMap.put(UUID.fromString(key), currencyTag.getFloat(key));
        });

        lastTickTag.getKeys().forEach(key -> {
            data.lastTickMap.put(UUID.fromString(key), lastTickTag.getLong(key));
        });

        upgradesTag.getKeys().forEach(playerKey -> {
            UUID playerId = UUID.fromString(playerKey);
            NbtCompound playerUpgrades = upgradesTag.getCompound(playerKey);
            Map<String, Integer> upgrades = new HashMap<>();
            playerUpgrades.getKeys().forEach(upgradeKey -> {
                upgrades.put(upgradeKey, playerUpgrades.getInt(upgradeKey));
            });
            data.upgradesMap.put(playerId, upgrades);
        });

        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        NbtCompound currencyTag = new NbtCompound();
        NbtCompound lastTickTag = new NbtCompound();
        NbtCompound upgradesTag = new NbtCompound();

        currencyMap.forEach((uuid, amount) -> {
            currencyTag.putFloat(uuid.toString(), amount);
        });

        lastTickMap.forEach((uuid, tick) -> {
            lastTickTag.putLong(uuid.toString(), tick);
        });

        upgradesMap.forEach((uuid, upgrades) -> {
            NbtCompound playerUpgrades = new NbtCompound();
            upgrades.forEach((upgradeId, level) -> {
                playerUpgrades.putInt(upgradeId, level);
            });
            upgradesTag.put(uuid.toString(), playerUpgrades);
        });

        tag.put("currency", currencyTag);
        tag.put("lastTick", lastTickTag);
        tag.put("upgrades", upgradesTag);

        return tag;
    }

    public float getCurrency(PlayerEntity player) {
        return currencyMap.getOrDefault(player.getUuid(), 0.0f);
    }

    public void setCurrency(PlayerEntity player, float amount) {
        currencyMap.put(player.getUuid(), amount);
        markDirty();
    }

    public long getLastTick(PlayerEntity player) {
        return lastTickMap.getOrDefault(player.getUuid(), 0L);
    }

    public void setLastTick(PlayerEntity player, long tick) {
        lastTickMap.put(player.getUuid(), tick);
        markDirty();
    }

    public int getUpgradeLevel(PlayerEntity player, String upgradeId) {
        return upgradesMap
            .computeIfAbsent(player.getUuid(), k -> new HashMap<>())
            .getOrDefault(upgradeId, 0);
    }

    public void setUpgradeLevel(PlayerEntity player, String upgradeId, int level) {
        upgradesMap
            .computeIfAbsent(player.getUuid(), k -> new HashMap<>())
            .put(upgradeId, level);
        markDirty();
    }

    public static PlayerCurrencyData getServerState(World world) {
        return world.getServer().getOverworld().getPersistentStateManager()
            .getOrCreate(
                Type.create(PlayerCurrencyData::createFromNbt, PlayerCurrencyData::new),
                "currency_data"
            );
    }
}
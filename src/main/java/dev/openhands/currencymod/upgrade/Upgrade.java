package dev.openhands.currencymod.upgrade;

public class Upgrade {
    private final String id;
    private final String name;
    private final String description;
    private final float baseCost;
    private final float costMultiplier;
    private final float baseEffect;
    private final float effectMultiplier;
    
    public Upgrade(String id, String name, String description, float baseCost, float costMultiplier, float baseEffect, float effectMultiplier) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.baseCost = baseCost;
        this.costMultiplier = costMultiplier;
        this.baseEffect = baseEffect;
        this.effectMultiplier = effectMultiplier;
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public float getCostForLevel(int level) {
        return baseCost * (float)Math.pow(costMultiplier, level);
    }
    
    public float getEffectForLevel(int level) {
        return baseEffect * (float)Math.pow(effectMultiplier, level);
    }
}
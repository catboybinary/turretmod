package me.binary.turretmod.util;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;

public class Maps {
    public static HashMap<Item, EntityType> PROJECTILES = new HashMap<>();
    public static void register() {
        PROJECTILES.put(Items.ARROW, EntityType.ARROW);
        PROJECTILES.put(Items.SNOWBALL, EntityType.SNOWBALL);
        PROJECTILES.put(Items.SPECTRAL_ARROW, EntityType.SPECTRAL_ARROW);
        PROJECTILES.put(Items.TIPPED_ARROW, EntityType.ARROW);
        PROJECTILES.put(Items.FIRE_CHARGE, EntityType.SMALL_FIREBALL);
        PROJECTILES.put(Items.EGG, EntityType.EGG);
        PROJECTILES.put(Items.ENDER_PEARL, EntityType.ENDER_PEARL);
        PROJECTILES.put(Items.TRIDENT, EntityType.TRIDENT);
        PROJECTILES.put(Items.EXPERIENCE_BOTTLE, EntityType.EXPERIENCE_BOTTLE);
    }
}

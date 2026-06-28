package com.starfantasy.soulsfirecontrol.combat.guard;

import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.registries.ForgeRegistries;

public final class SlashBladeGuardCompat {
    private SlashBladeGuardCompat() {
    }

    public static boolean isHoldingSlashBlade(LivingEntity entity) {
        return isSlashBlade(entity.getMainHandItem()) || isSlashBlade(entity.getOffhandItem());
    }

    public static boolean isUsableMainhandSlashBlade(LivingEntity entity) {
        ItemStack stack = entity.getMainHandItem();
        if (!isSlashBlade(stack)) {
            return false;
        }
        LazyOptional<ISlashBladeState> slashBlade = stack.getCapability(CapabilitySlashBlade.BLADESTATE);
        return slashBlade.isPresent() && slashBlade.filter(ISlashBladeState::isBroken).isEmpty();
    }

    private static boolean isSlashBlade(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof ItemSlashBlade) {
            return true;
        }
        if (stack.getCapability(CapabilitySlashBlade.BLADESTATE).isPresent()
                || stack.getCapability(ItemSlashBlade.BLADESTATE).isPresent()) {
            return true;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key != null && "slashblade".equals(key.getNamespace())) {
            return true;
        }
        return item.getClass().getName().toLowerCase(java.util.Locale.ROOT).contains("slashblade");
    }
}

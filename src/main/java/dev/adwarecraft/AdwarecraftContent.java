package dev.adwarecraft;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

/** Common content and authoritative reward handling; install on both client and server for multiplayer. */
public final class AdwarecraftContent implements ModInitializer {
    public static final Identifier CLAIM_DIRT_PLUS = new Identifier("adwarecraft", "claim_dirt_plus");
    public static final Item DIRT_PLUS = register("dirt_plus", new Item(new Item.Settings().maxCount(16)));
    public static final Item DIRT_HELMET = register("dirt_helmet",
            new ArmorItem(ArmorMaterials.LEATHER, ArmorItem.Type.HELMET, new Item.Settings()));
    public static final Item DIRT_AXE = register("dirt_axe",
            new AxeItem(DirtToolMaterial.INSTANCE, 5.0F, -3.0F, new Item.Settings()));

    private static Item register(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier("adwarecraft", name), item);
    }

    @Override
    public void onInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(DIRT_PLUS));
        ServerPlayNetworking.registerGlobalReceiver(CLAIM_DIRT_PLUS, (server, player, handler, buffer, responseSender) ->
                server.execute(() -> giveDirtPlus(player)));
    }

    private static void giveDirtPlus(ServerPlayerEntity player) {
        ItemStack reward = new ItemStack(DIRT_PLUS);
        if (!player.getInventory().insertStack(reward)) {
            player.dropItem(reward, false);
        }
        player.currentScreenHandler.sendContentUpdates();
    }

    private enum DirtToolMaterial implements ToolMaterial {
        INSTANCE;

        @Override public int getDurability() { return 64; }
        @Override public float getMiningSpeedMultiplier() { return 2.5F; }
        @Override public float getAttackDamage() { return 1.0F; }
        @Override public int getMiningLevel() { return 0; }
        @Override public int getEnchantability() { return 8; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.ofItems(DIRT_PLUS); }
    }
}

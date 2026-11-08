package dev.adwarecraft;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.text.Text;

/** Items use readable fallback names even in locales that have not translated these additions yet. */
final class DirtPlusItemNames {
    private DirtPlusItemNames() { }

    static Text name(String key, String fallback) {
        return Text.literal(fallback);
    }

    static final class IngredientItem extends Item {
        IngredientItem(Settings settings) { super(settings); }
        @Override public Text getName(ItemStack stack) { return name("item.adwarecraft.dirt_plus", "Dirt Plus"); }
    }

    static final class HelmetItem extends ArmorItem {
        HelmetItem(ArmorItem.Type type, Settings settings) { super(net.minecraft.item.ArmorMaterials.LEATHER, type, settings); }
        @Override public Text getName(ItemStack stack) { return name("item.adwarecraft.dirt_helmet", "Dirt Helmet"); }
    }

    static final class Axe extends net.minecraft.item.AxeItem {
        Axe(net.minecraft.item.ToolMaterial material, float damage, float speed, Settings settings) {
            super(material, damage, speed, settings);
        }
        @Override public Text getName(ItemStack stack) { return name("item.adwarecraft.dirt_axe", "Dirt Axe"); }
    }
}

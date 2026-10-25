package net.silentchaos512.gear.loot.modifier;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.silentchaos512.gear.api.traits.TraitActionContext;
import net.silentchaos512.gear.util.GearHelper;
import net.silentchaos512.gear.util.TraitHelper;


public class BonusDropsTraitLootModifier {
    // Port note: upstream extends NeoForge's LootModifier and is registered as a global loot modifier with a
    // codec. Fabric has no such registry; SgLootModifiers registers doApply with Silent Lib's GlobalLootModifiers and
    // checks the conditions that the upstream JSON file declared.

    public ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ObjectArrayList<ItemStack> ret = new ObjectArrayList<>(generatedLoot);
        var tool = context.getOptionalParameter(LootContextParams.TOOL);

        if (tool != null && GearHelper.isGear(tool) && tool instanceof ItemStack gearStack) {
            //noinspection OverlyLongLambda
            TraitHelper.activateTraits(gearStack, 0, (trait, value) -> {
                generatedLoot.forEach(lootStack -> {
                    ItemStack stack = trait.getTrait().addLootDrops(new TraitActionContext(null, trait, gearStack), lootStack);
                    if (!stack.isEmpty()) {
                        ret.add(stack);
                    }
                });
                return 0;
            });
        }

        return ret;
    }

}

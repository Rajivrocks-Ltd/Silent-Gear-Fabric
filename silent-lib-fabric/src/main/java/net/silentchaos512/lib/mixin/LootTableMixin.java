package net.silentchaos512.lib.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.silentchaos512.lib.port.loot.GlobalLootModifiers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Applies {@link GlobalLootModifiers} to loot table results, at the point where NeoForge applies its global
 * loot modifiers.
 *
 * <p>Fabric port only. Every way of rolling a table on its own ends in
 * {@code getRandomItems(LootContext, Consumer)}:
 * <ul>
 *     <li>{@code getRandomItems(LootContext, Consumer)}: collects the split stacks, applies the modifiers
 *     once, then passes the result on.</li>
 *     <li>The two {@code getRandomItems(LootParams, ..., Consumer)} variants call {@code getRandomItemsRaw}
 *     directly in vanilla. They are sent through the method above instead, building the same context
 *     vanilla builds. NeoForge's patch makes the same change.</li>
 *     <li>The {@code ObjectArrayList} variants and {@code fill} already go through it.</li>
 * </ul>
 * Inner tables are rolled with {@code getRandomItemsRaw} and are not modified, as in NeoForge.
 */
@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @Shadow
    @Final
    private Optional<Identifier> randomSequence;

    @Shadow
    public abstract void getRandomItems(LootContext context, Consumer<ItemStack> output);

    @WrapMethod(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;Ljava/util/function/Consumer;)V")
    private void silentlib$modifyLoot(LootContext context, Consumer<ItemStack> output, Operation<Void> original) {
        ObjectArrayList<ItemStack> loot = new ObjectArrayList<>();
        original.call(context, (Consumer<ItemStack>) loot::add);
        GlobalLootModifiers.modifyLoot(loot, context).forEach(output);
    }

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;JLjava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void silentlib$seededThroughContext(LootParams params, long optionalLootTableSeed, Consumer<ItemStack> output, CallbackInfo ci) {
        this.getRandomItems(new LootContext.Builder(params).withOptionalRandomSeed(optionalLootTableSeed).create(this.randomSequence), output);
        ci.cancel();
    }

    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;Ljava/util/function/Consumer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void silentlib$paramsThroughContext(LootParams params, Consumer<ItemStack> output, CallbackInfo ci) {
        this.getRandomItems(new LootContext.Builder(params).create(this.randomSequence), output);
        ci.cancel();
    }
}

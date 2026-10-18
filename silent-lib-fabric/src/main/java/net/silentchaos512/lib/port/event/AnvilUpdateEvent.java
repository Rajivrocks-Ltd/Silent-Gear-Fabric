package net.silentchaos512.lib.port.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Fired when the items in an anvil change. A listener that sets a non-empty output replaces the
 * vanilla result. Canceling it leaves the anvil with no result.
 *
 * <p>Fabric: not fired yet. Needs a mixin in {@code AnvilMenu.createResult} (milestone M8).
 */
public class AnvilUpdateEvent extends CancellableEvent {
    private final ItemStack left;
    private final ItemStack right;
    private final @Nullable String name;
    private final Player player;
    private final VanillaResult vanillaResult;
    private ItemStack output;
    private int xpCost;
    private int materialCost;

    public AnvilUpdateEvent(ItemStack left, ItemStack right, @Nullable String name, ItemStack result, int xpCost, int materialCost, Player player) {
        this.left = left;
        this.right = right;
        this.name = name;
        this.player = player;
        this.vanillaResult = new VanillaResult(result.copy(), xpCost, materialCost);
        this.output = result;
        this.xpCost = xpCost;
        this.materialCost = materialCost;
    }

    public ItemStack getLeft() {
        return left;
    }

    public ItemStack getRight() {
        return right;
    }

    /**
     * Gets the name typed into the anvil.
     *
     * @return the name, or null if none was typed
     */
    public @Nullable String getName() {
        return name;
    }

    public VanillaResult getVanillaResult() {
        return vanillaResult;
    }

    public ItemStack getOutput() {
        return output;
    }

    public void setOutput(ItemStack output) {
        this.output = output;
    }

    public int getXpCost() {
        return xpCost;
    }

    public void setXpCost(int xpCost) {
        this.xpCost = xpCost;
    }

    public int getMaterialCost() {
        return materialCost;
    }

    public void setMaterialCost(int materialCost) {
        this.materialCost = materialCost;
    }

    public Player getPlayer() {
        return player;
    }

    /**
     * What vanilla would have produced.
     *
     * @param output       the vanilla output
     * @param xpCost       the vanilla level cost
     * @param materialCost the vanilla number of right-side items used
     */
    public record VanillaResult(ItemStack output, int xpCost, int materialCost) {
    }
}

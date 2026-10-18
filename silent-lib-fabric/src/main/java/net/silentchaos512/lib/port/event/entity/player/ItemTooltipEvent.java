package net.silentchaos512.lib.port.event.entity.player;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.silentchaos512.lib.port.event.Event;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Fired on the client after an item's tooltip lines are built. Listeners can add, change or
 * remove lines.
 *
 * <p>Fabric: fired from {@code ItemTooltipCallback} (client only). Unlike NeoForge this is not a
 * player event, because the player can be null while tooltips are built during startup.
 */
public class ItemTooltipEvent extends Event {
    private final ItemStack itemStack;
    private final @Nullable Player player;
    private final List<Component> toolTip;
    private final TooltipFlag flags;
    private final Item.TooltipContext context;

    public ItemTooltipEvent(ItemStack itemStack, @Nullable Player player, List<Component> toolTip, TooltipFlag flags, Item.TooltipContext context) {
        this.itemStack = itemStack;
        this.player = player;
        this.toolTip = toolTip;
        this.flags = flags;
        this.context = context;
    }

    public TooltipFlag getFlags() {
        return flags;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public List<Component> getToolTip() {
        return toolTip;
    }

    public @Nullable Player getEntity() {
        return player;
    }

    public Item.TooltipContext getContext() {
        return context;
    }
}

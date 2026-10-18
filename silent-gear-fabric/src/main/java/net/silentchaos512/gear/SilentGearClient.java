package net.silentchaos512.gear;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.silentchaos512.gear.client.ClientRecipeHelper;
import net.silentchaos512.gear.client.KeyTracker;
import net.silentchaos512.gear.client.event.TooltipHandler;
import net.silentchaos512.gear.client.setup.SgItemTintSources;
import net.silentchaos512.gear.client.tooltips.GearItemTooltips;
import net.silentchaos512.gear.event.ClientEvents;
import net.silentchaos512.gear.network.SgNetworkClient;
import net.silentchaos512.gear.setup.SgBlockEntities;
import net.silentchaos512.gear.setup.SgDataComponents;
import net.silentchaos512.gear.setup.SgEntities;
import net.silentchaos512.gear.setup.SgMenuTypes;
import net.silentchaos512.lib.port.client.event.EntityRenderersEvent;
import net.silentchaos512.lib.port.client.event.InputEvent;
import net.silentchaos512.lib.port.client.event.ModelEvent;
import net.silentchaos512.lib.port.client.event.RegisterColorHandlersEvent;
import net.silentchaos512.lib.port.client.event.RegisterMenuScreensEvent;
import net.silentchaos512.lib.port.client.extensions.common.RegisterClientExtensionsEvent;
import net.silentchaos512.lib.port.event.NeoForge;
import net.silentchaos512.lib.port.event.entity.player.ItemTooltipEvent;

/**
 * Client-only entry point for Silent Gear on Fabric.
 *
 * <p>Port notes: upstream does client setup from {@code SideProxy.Client} and from classes marked
 * {@code @EventBusSubscriber(value = Dist.CLIENT)}, which NeoForge calls through mod bus events.
 * Fabric calls {@link #onInitializeClient()} once on the physical client, after common init, so all
 * of that setup is done here, in the same groups as upstream. The NeoForge registration events are
 * Silent Lib stand-ins that forward to Fabric or vanilla registration calls.
 */
public final class SilentGearClient implements ClientModInitializer {
    /**
     * Called by Fabric Loader on the physical client, after {@link SilentGear#onInitialize()}.
     */
    @Override
    public void onInitializeClient() {
        // Former mod bus registration events
        SgItemTintSources.onItemTintSources(new RegisterColorHandlersEvent.ItemTintSources());
        SgMenuTypes.ClientEvents.registerScreens(new RegisterMenuScreensEvent());
        SgEntities.Events.registerRenderers(new EntityRenderersEvent.RegisterRenderers());
        SgEntities.Events.registerClientExtensions(new RegisterClientExtensionsEvent());
        SgEntities.Events.registerAdditional(new ModelEvent.RegisterStandalone());
        SgBlockEntities.ClientEvents.registerRenderers(new EntityRenderersEvent.RegisterRenderers());
        KeyTracker.register();

        // Former game bus listeners (client side)
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, TooltipHandler.INSTANCE::onTooltip);
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, GearItemTooltips::onTooltip);
        NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, SgDataComponents.TooltipHandler::onTooltip);
        NeoForge.EVENT_BUS.addListener(InputEvent.InteractionKeyMappingTriggered.class, ClientEvents::onClick);
        ClientRecipeSynchronizedEvent.EVENT.register((client, recipes) -> ClientRecipeHelper.onRecipesReceived(recipes));

        SgNetworkClient.registerClientReceivers();

        // Config screen (upstream registers it as a NeoForge mod extension point)
        ConfigScreenFactoryRegistry.INSTANCE.register(SilentGear.MOD_ID, ConfigurationScreen::new);

        SilentGear.LOGGER.info("Silent Gear (Fabric) client initialized");
    }
}

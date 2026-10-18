/**
 * Game events with NeoForge's names, for the Fabric port.
 *
 * <p>Sub-packages mirror {@code net.neoforged.neoforge.event}, so a ported import only changes its
 * package prefix. Each event class documents what fires it on Fabric: a Fabric API callback
 * (wired in {@link net.silentchaos512.lib.port.event.FabricEventBridge}) or a Silent Lib mixin.
 * Events with neither are not fired yet; their javadoc says so.
 */
@NullMarked
package net.silentchaos512.lib.port.event;

import org.jspecify.annotations.NullMarked;

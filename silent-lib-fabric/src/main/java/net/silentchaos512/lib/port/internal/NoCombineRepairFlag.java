package net.silentchaos512.lib.port.internal;

/**
 * Internal: read access to the flag set by
 * {@link net.silentchaos512.lib.port.IItemPropertiesExtension#setNoCombineRepair()}.
 *
 * <p>Implemented on {@code Item.Properties} by Silent Lib's mixin. Kept outside the mixin package,
 * because classes in a mixin package cannot be loaded directly.
 */
public interface NoCombineRepairFlag {
    /**
     * Checks whether the properties were marked with {@code setNoCombineRepair()}.
     *
     * @return true if marked
     */
    boolean silentlib$isNoCombineRepair();
}

package net.silentchaos512.lib.port.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

/**
 * Stream codec helpers that NeoForge provides and Silent Gear uses.
 *
 * <p>Fabric port stand-in for NeoForge's class of the same name.
 */
public final class NeoForgeStreamCodecs {
    private NeoForgeStreamCodecs() {}

    /**
     * Creates a codec that sends an enum constant as its ordinal.
     *
     * @param enumClass the enum class
     * @param <T>       the enum type
     * @return the codec
     */
    public static <T extends Enum<T>> StreamCodec<FriendlyByteBuf, T> enumCodec(Class<T> enumClass) {
        return StreamCodec.of(FriendlyByteBuf::writeEnum, buf -> buf.readEnum(enumClass));
    }
}

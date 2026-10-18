package net.silentchaos512.lib.port;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A named action a tool can perform, such as stripping logs or tilling dirt.
 *
 * <p>Fabric port stand-in for NeoForge's class of the same name. Fabric has no equivalent, so
 * this keeps the same names and methods. Each name maps to exactly one instance, so instances
 * can be compared with {@code ==}. What an action actually does is coded in the item using it.
 */
public final class ItemAbility {
    private static final Map<String, ItemAbility> ABILITIES = new ConcurrentHashMap<>();

    /** Encodes an ability as its name. */
    public static final Codec<ItemAbility> CODEC = Codec.STRING.xmap(ItemAbility::get, ItemAbility::name);
    /** Sends an ability over the network as its name. */
    public static final StreamCodec<ByteBuf, ItemAbility> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ItemAbility::get, ItemAbility::name);

    private final String name;

    private ItemAbility(String name) {
        this.name = name;
    }

    /**
     * Gets every ability created so far.
     *
     * @return an unmodifiable view of all abilities
     */
    public static Collection<ItemAbility> getActions() {
        return Collections.unmodifiableCollection(ABILITIES.values());
    }

    /**
     * Gets the ability with the given name, creating it if needed.
     *
     * @param name the ability name, for example {@code "axe_strip"}
     * @return the one instance for that name
     */
    public static ItemAbility get(String name) {
        return ABILITIES.computeIfAbsent(name, ItemAbility::new);
    }

    /**
     * Gets the ability name.
     *
     * @return the name
     */
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "ItemAbility[" + name + "]";
    }
}

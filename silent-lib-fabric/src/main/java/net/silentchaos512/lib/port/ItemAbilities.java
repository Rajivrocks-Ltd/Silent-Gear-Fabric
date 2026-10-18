package net.silentchaos512.lib.port;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.Set;

/**
 * The tool actions known to vanilla, and the sets of actions vanilla tools have by default.
 *
 * <p>Fabric port stand-in for NeoForge's class of the same name. The names and set contents match
 * NeoForge, because code and data written for NeoForge refer to them.
 */
public final class ItemAbilities {
    private ItemAbilities() {}

    /** Shears mining blocks such as wool and leaves. */
    public static final ItemAbility SHEARS_DIG = ItemAbility.get("shears_dig");
    /** Axe stripping a log. */
    public static final ItemAbility AXE_STRIP = ItemAbility.get("axe_strip");
    /** Axe scraping oxidation off copper. */
    public static final ItemAbility AXE_SCRAPE = ItemAbility.get("axe_scrape");
    /** Axe removing wax from copper. */
    public static final ItemAbility AXE_WAX_OFF = ItemAbility.get("axe_wax_off");
    /** Shovel turning grass into a path. */
    public static final ItemAbility SHOVEL_FLATTEN = ItemAbility.get("shovel_flatten");
    /** Shovel putting out a campfire. */
    public static final ItemAbility SHOVEL_DOUSE = ItemAbility.get("shovel_douse");
    /** Sword sweep attack. */
    public static final ItemAbility SWORD_SWEEP = ItemAbility.get("sword_sweep");
    /** Shears harvesting a block, such as honeycomb from a beehive. */
    public static final ItemAbility SHEARS_HARVEST = ItemAbility.get("shears_harvest");
    /** Shears removing armor from a wolf. */
    public static final ItemAbility SHEARS_REMOVE_ARMOR = ItemAbility.get("shears_remove_armor");
    /** Shears carving a pumpkin. */
    public static final ItemAbility SHEARS_CARVE = ItemAbility.get("shears_carve");
    /** Shears disarming a tripwire. */
    public static final ItemAbility SHEARS_DISARM = ItemAbility.get("shears_disarm");
    /** Shears trimming plants. */
    public static final ItemAbility SHEARS_TRIM = ItemAbility.get("shears_trim");
    /** Hoe tilling dirt. The name is "till", as in NeoForge. */
    public static final ItemAbility HOE_TILL = ItemAbility.get("till");
    /** Fishing rod casting. */
    public static final ItemAbility FISHING_ROD_CAST = ItemAbility.get("fishing_rod_cast");
    /** Trident throwing. */
    public static final ItemAbility TRIDENT_THROW = ItemAbility.get("trident_throw");
    /** Brush brushing suspicious blocks. */
    public static final ItemAbility BRUSH_BRUSH = ItemAbility.get("brush_brush");
    /** Flint and steel or fire charge lighting fires. */
    public static final ItemAbility FIRESTARTER_LIGHT = ItemAbility.get("firestarter_light");
    /** Spyglass zooming. */
    public static final ItemAbility SPYGLASS_SCOPE = ItemAbility.get("spyglass_scope");

    /** Default axe actions. */
    public static final Set<ItemAbility> DEFAULT_AXE_ACTIONS = of(AXE_STRIP, AXE_SCRAPE, AXE_WAX_OFF);
    /** Default hoe actions. */
    public static final Set<ItemAbility> DEFAULT_HOE_ACTIONS = of(HOE_TILL);
    /** Default shovel actions. */
    public static final Set<ItemAbility> DEFAULT_SHOVEL_ACTIONS = of(SHOVEL_FLATTEN, SHOVEL_DOUSE);
    /** Default shears actions. */
    public static final Set<ItemAbility> DEFAULT_SHEARS_ACTIONS = of(SHEARS_DIG, SHEARS_HARVEST, SHEARS_REMOVE_ARMOR, SHEARS_CARVE, SHEARS_DISARM, SHEARS_TRIM);
    /** Default fishing rod actions. */
    public static final Set<ItemAbility> DEFAULT_FISHING_ROD_ACTIONS = of(FISHING_ROD_CAST);
    /** Default trident actions. */
    public static final Set<ItemAbility> DEFAULT_TRIDENT_ACTIONS = of(TRIDENT_THROW);
    /** Default brush actions. */
    public static final Set<ItemAbility> DEFAULT_BRUSH_ACTIONS = of(BRUSH_BRUSH);
    /** Default flint and steel actions. */
    public static final Set<ItemAbility> DEFAULT_FLINT_ACTIONS = of(FIRESTARTER_LIGHT);
    /** Default fire charge actions. */
    public static final Set<ItemAbility> DEFAULT_FIRECHARGE_ACTIONS = of(FIRESTARTER_LIGHT);
    /** Default spyglass actions. */
    public static final Set<ItemAbility> DEFAULT_SPYGLASS_ACTIONS = of(SPYGLASS_SCOPE);

    /**
     * Makes an unmodifiable identity set, since each ability has exactly one instance.
     *
     * @param actions the abilities
     * @return the set
     */
    private static Set<ItemAbility> of(ItemAbility... actions) {
        Set<ItemAbility> set = Sets.newIdentityHashSet();
        Collections.addAll(set, actions);
        return Collections.unmodifiableSet(set);
    }
}

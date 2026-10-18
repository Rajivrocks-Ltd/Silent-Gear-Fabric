package net.silentchaos512.gear.api.property;

public class HarvestTierPropertyValue extends GearPropertyValue<HarvestTier> {
    public HarvestTierPropertyValue(HarvestTier value) {
        super(value);
    }

    @Override
    public String toString() {
        return this.value.name();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof HarvestTierPropertyValue other)) return false;

        return this.value.equals(other.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
}

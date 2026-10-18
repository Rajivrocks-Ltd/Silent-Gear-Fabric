package net.silentchaos512.gear.api.property;

import net.silentchaos512.lib.util.MathUtils;

public final class NumberPropertyValue extends GearPropertyValue<Float> {
    private final NumberProperty.Operation operation;

    public NumberPropertyValue(float value, NumberProperty.Operation operation) {
        super(value);
        this.operation = operation;
    }

    public NumberProperty.Operation operation() {
        return this.operation;
    }

    public static NumberPropertyValue average(float value) {
        return new NumberPropertyValue(value, NumberProperty.Operation.AVERAGE);
    }

    @Override
    public String toString() {
        return String.format("%s %.1f", this.operation.name(), this.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (!(obj instanceof NumberPropertyValue other)) return false;

        return MathUtils.floatsEqual(this.value, other.value) && this.operation == other.operation;
    }

    @Override
    public int hashCode() {
        return (this.value.hashCode() << 1) | this.operation.ordinal();
    }
}

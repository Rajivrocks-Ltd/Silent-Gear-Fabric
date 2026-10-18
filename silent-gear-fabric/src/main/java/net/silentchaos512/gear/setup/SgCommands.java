package net.silentchaos512.gear.setup;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.silentchaos512.gear.command.*;

public final class SgCommands {
    private SgCommands() {throw new IllegalAccessError("Utility class");}

    /**
     * Registers all Silent Gear commands.
     *
     * <p>Port note: upstream receives NeoForge's {@code RegisterCommandsEvent}. Fabric's
     * {@code CommandRegistrationCallback} provides the dispatcher directly.
     *
     * @param dispatcher the command dispatcher
     */
    public static void registerAll(CommandDispatcher<CommandSourceStack> dispatcher) {
        SGearCommand.register(dispatcher);
        GradeCommand.register(dispatcher);
        StarchargeCommand.register(dispatcher);
        SetDamageCommand.register(dispatcher);
        MaterialsCommand.register(dispatcher);
        PartsCommand.register(dispatcher);
        PropertiesCommand.register(dispatcher);
        TraitsCommand.register(dispatcher);
        RandomGearCommand.register(dispatcher);
    }
}

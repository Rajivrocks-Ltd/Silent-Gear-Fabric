package net.silentchaos512.gear.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundPlayerLoadedPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * A fake server player for gametests.
 *
 * <p>Vanilla's {@link GameTestHelper#makeMockServerPlayerInLevel()} is always in creative mode, where tools do
 * not lose durability. This player reports the game mode it was made with, stands inside the test area, and
 * can fully charge its attack (vanilla only charges it while the player ticks).
 */
public class TestPlayer extends ServerPlayer {
    private final GameType gameType;

    private TestPlayer(GameTestHelper helper, CommonListenerCookie cookie, GameType gameType) {
        super(helper.getLevel().getServer(), helper.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        this.gameType = gameType;
    }

    /**
     * Creates a player, adds it to the test level and moves it to a spot in the test area.
     *
     * @param helper      the test helper
     * @param gameType    the game mode
     * @param relativePos the block the player stands in, relative to the test area
     * @return the player
     */
    public static TestPlayer create(GameTestHelper helper, GameType gameType, BlockPos relativePos) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), "test-player"), false);
        TestPlayer player = new TestPlayer(helper, cookie, gameType);
        net.minecraft.network.Connection connection = new net.minecraft.network.Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        helper.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        // ServerPlayer is immune to all damage until its client says it has loaded; a real client sends this.
        player.connection.handleAcceptPlayerLoad(new ServerboundPlayerLoadedPacket());
        player.gameMode.changeGameModeForPlayer(gameType);
        // Changing to the mode the player already has does nothing, and the abilities placeNewPlayer gave may
        // be the gametest server's creative ones (no durability loss, no fall damage). Set them directly.
        gameType.updatePlayerAbilities(player.getAbilities());
        player.onUpdateAbilities();
        Vec3 pos = helper.absoluteVec(Vec3.atBottomCenterOf(relativePos));
        player.snapTo(pos.x, pos.y, pos.z, 0f, 0f);
        player.setOnGround(true);
        return player;
    }

    /**
     * Creates a survival player standing at relative position (1, 1, 1).
     *
     * @param helper the test helper
     * @return the player
     */
    public static TestPlayer survival(GameTestHelper helper) {
        return create(helper, GameType.SURVIVAL, new BlockPos(1, 1, 1));
    }

    /**
     * Describes the player's game mode and abilities, for failure messages.
     *
     * @return a short description
     */
    public String describe() {
        var a = this.getAbilities();
        return "mode=" + this.gameMode.getGameModeForPlayer() + " instabuild=" + a.instabuild + " invulnerable=" + a.invulnerable
                + " mayfly=" + a.mayfly + " infinite=" + this.hasInfiniteMaterials();
    }

    @Override
    public GameType gameMode() {
        return this.gameType;
    }

    /**
     * Makes the next attack a full strength attack, as if the player had waited for the cooldown.
     */
    public void fullyChargeAttack() {
        this.attackStrengthTicker = 1000;
    }
}

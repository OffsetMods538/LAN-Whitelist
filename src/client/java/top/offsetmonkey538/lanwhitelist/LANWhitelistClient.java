package top.offsetmonkey538.lanwhitelist;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import top.offsetmonkey538.lanwhitelist.config.WhitelistEnabled;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import static com.mojang.text2speech.Narrator.LOGGER;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class LANWhitelistClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		// Hopefully early enough so enabling whitelist here will apply to clients that join like almost instantly.
		//  Though I guess the host would seed to actually turn on LAN before anyone could try joining? There are probably mods that force lan from the beginning though...
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (!(server instanceof IntegratedServer integratedServer)) {
                LANWhitelist.logServer();
                return;
            }

			final boolean enabled = WhitelistEnabled.isWhitelistEnabled(integratedServer);
			server.setUseAllowlist(enabled);
			server.setEnforceWhitelist(enabled);

			try {
				server.getPlayerManager().getWhitelist().load();
			} catch (IOException e) {
				LOGGER.warn("Failed to load white-list: ", e);
			}

            // Always add host player to whitelist. Wouldn't want to ban them from their singleplayer world now, would I?
            server.getPlayerManager().getWhitelist().add(new WhitelistEntry(new PlayerConfigEntry(integratedServer.getHostProfile())));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) -> dispatcher.register(
                literal("whitelist")
                        .requires(source -> {
                            // Only allow host to manage whitelist.
                            //  No console on singleplayer so no need to worry about that.
                            //  Command blocks (and other non-player executors) definitely shouldn't cause another player could (when lan is opened with cheats) place down a command block and run the whitelist command
                            if (!(source.getServer() instanceof IntegratedServer server)) {
                                LANWhitelist.logServer();
                                return false;
                            }

                            return source.getEntity() instanceof PlayerEntity player &&  server.isHost(player.getPlayerConfigEntry());
                        })
                        .then(literal("on").executes(context -> {
                            if (!(context.getSource().getServer() instanceof IntegratedServer server)) {
                                LANWhitelist.logServer();
                                return 0;
                            }
                            // Always add host player to whitelist. Wouldn't want to ban them from their singleplayer world now, would I?
                            server.getPlayerManager().getWhitelist().add(new WhitelistEntry(new PlayerConfigEntry(server.getHostProfile())));

                            return WhitelistCommand.executeOn(context.getSource());
                        }))
                        .then(literal("off").executes(context -> WhitelistCommand.executeOff(context.getSource())))
                        .then(literal("list").executes(context -> WhitelistCommand.executeList(context.getSource())))
                        .then(
                                literal("add")
                                        .then(
                                                argument("targets", GameProfileArgumentType.gameProfile())
                                                        .suggests(
                                                                (context, builder) -> {
                                                                    PlayerManager playerManager = context.getSource().getServer().getPlayerManager();
                                                                    return CommandSource.suggestMatching(
                                                                            playerManager.getPlayerList()
                                                                                    .stream()
                                                                                    .filter(player -> !playerManager.getWhitelist().isAllowed(player.getPlayerConfigEntry()))
                                                                                    .map(player -> player.getGameProfile().name()),
                                                                            builder
                                                                    );
                                                                }
                                                        )
                                                        .executes(context -> WhitelistCommand.executeAdd(context.getSource(), GameProfileArgumentType.getProfileArgument(context, "targets")))
                                        )
                        )
                        .then(
                                literal("remove")
                                        .then(
                                                argument("targets", GameProfileArgumentType.gameProfile())
                                                        .suggests((context, builder) -> CommandSource.suggestMatching(context.getSource().getServer().getPlayerManager().getWhitelistedNames(), builder))
                                                        .executes(context -> {
                                                            final ServerCommandSource source = context.getSource();
                                                            final Collection<PlayerConfigEntry> targets = GameProfileArgumentType.getProfileArgument(context, "targets");
                                                            if (!(source.getServer() instanceof IntegratedServer server)) return LANWhitelist.logServer(-1);

                                                            // Write to new list so iteration isn't messed with
                                                            final LinkedList<PlayerConfigEntry> newTargets = new LinkedList<>();
                                                            for (final PlayerConfigEntry profile : targets) {
                                                                if (!server.isHost(profile)) {
                                                                    newTargets.add(profile);
                                                                    continue;
                                                                }

                                                                source.sendFeedback(() -> Text.translatable("commands.lan_whitelist.whitelist.remove.host", profile.name()).formatted(Formatting.RED), true);
                                                            }

                                                            if (newTargets.isEmpty()) return 1;
                                                            return WhitelistCommand.executeRemove(source, newTargets);
                                                        })
                                        )
                        )
                        .then(literal("reload").executes(context -> {
                            if (!(context.getSource().getServer() instanceof IntegratedServer server)) {
                                LANWhitelist.logServer();
                                return 0;
                            }

                            // Always add host player to whitelist. Wouldn't want to ban them from their singleplayer world now, would I?
                            server.getPlayerManager().getWhitelist().add(new WhitelistEntry(new PlayerConfigEntry(server.getHostProfile())));

                            // Reload isEnabled and send chat message
                            final boolean enabled = WhitelistEnabled.isWhitelistEnabled(server);
                            server.setUseAllowlist(enabled);
                            server.setEnforceWhitelist(enabled);

                            sendEnabledMessageToHost(server, enabled);

                            return WhitelistCommand.executeReload(context.getSource());
                        }))
        ));
	}

    public static void sendEnabledMessageToHost(final IntegratedServer server, final boolean enabled) {
        final GameProfile hostProfile = server.getHostProfile();
        if (hostProfile == null) throw new IllegalStateException("Host player profile not set!");

        final PlayerEntity hostPlayer = server.getPlayerManager().getPlayer(hostProfile.id());
        if (hostPlayer == null) throw new IllegalStateException("Host player not in server???");

        hostPlayer.sendMessage(Text.translatable(enabled ? "lan-whitelist.enabled" : "lan-whitelist.disabled").formatted(Formatting.YELLOW), false);
    }
}

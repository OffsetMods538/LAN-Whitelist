package top.offsetmonkey538.lanwhitelist;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.command.WhitelistCommand;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import top.offsetmonkey538.lanwhitelist.persistent.WhitelistPersistentState;

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
			if (!(server instanceof IntegratedServer integratedServer)) return;

			final boolean enabled = WhitelistPersistentState.getServerState(server).enabled;
			server.getPlayerManager().setWhitelistEnabled(enabled);
			server.setEnforceWhitelist(enabled);

			try {
				server.getPlayerManager().getWhitelist().load();
			} catch (IOException e) {
				LOGGER.warn("Failed to load white-list: ", e);
			}

			// Always add host player to whitelist. Wouldn't want to ban them from their singleplayer world now, would I?
			server.getPlayerManager().getWhitelist().add(new WhitelistEntry(integratedServer.getHostProfile()));
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, commandRegistryAccess, registrationEnvironment) -> dispatcher.register(
                literal("whitelist")
                        .requires(source -> {
                            // Only allow host to manage whitelist.
                            //  No console on singleplayer so no need to worry about that.
                            //  Command blocks (and other non-player executors) definitely shouldn't cause another player could (when lan is opened with cheats) place down a command block and run the whitelist command

                            return source.getServer() instanceof IntegratedServer server && source.getEntity() instanceof PlayerEntity player &&  server.isHost(player.getGameProfile());
                        })
                        .then(literal("on").executes(context -> WhitelistCommand.executeOn(context.getSource())))
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
                                                                                    .filter(player -> !playerManager.getWhitelist().isAllowed(player.getGameProfile()))
                                                                                    .map(player -> player.getGameProfile().getName()),
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
                                                            final Collection<GameProfile> targets = GameProfileArgumentType.getProfileArgument(context, "targets");
                                                            if (!(source.getServer() instanceof IntegratedServer server)) return -1;

                                                            // Write to new list so iteration isn't messed with
                                                            final LinkedList<GameProfile> newTargets = new LinkedList<>();
                                                            for (final GameProfile profile : targets) {
                                                                if (!server.isHost(profile)) {
                                                                    newTargets.add(profile);
                                                                    continue;
                                                                }

                                                                source.sendFeedback(() -> Text.translatable("commands.lan_whitelist.whitelist.remove.host", profile.getName()).formatted(Formatting.RED), true);
                                                            }

                                                            if (newTargets.isEmpty()) return 1;
                                                            return WhitelistCommand.executeRemove(source, newTargets);
                                                        })
                                        )
                        )
                        .then(literal("reload").executes(context -> WhitelistCommand.executeReload(context.getSource())))
        ));
	}
}

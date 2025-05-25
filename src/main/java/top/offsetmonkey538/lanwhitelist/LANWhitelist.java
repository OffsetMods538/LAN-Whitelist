package top.offsetmonkey538.lanwhitelist;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class LANWhitelist implements ModInitializer {
	public static final String MOD_ID = "lan-whitelist";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> {
			commandDispatcher.register(
					literal("whitelist")
							.executes(context -> {
								context.getSource().getServer().getPlayerManager().setWhitelistEnabled(true);
								context.getSource().getServer().getPlayerManager().getWhitelist().add(new WhitelistEntry(context.getSource().getPlayerOrThrow().getGameProfile()));
								return 1;
							})
			);
		});
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}

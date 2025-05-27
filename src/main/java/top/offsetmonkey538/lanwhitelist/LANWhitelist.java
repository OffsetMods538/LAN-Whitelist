package top.offsetmonkey538.lanwhitelist;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LANWhitelist implements ModInitializer {
	public static final String MOD_ID = "lan-whitelist";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// Actually turns out this won't be reached anyway cause environment in fabric.mod.json is set to client but ehh just in case I guess?
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) logServer();
	}


	public static <T> T logServer(T returnValue) {
		logServer();
		return returnValue;
	}
	public static void logServer() {
		LOGGER.error("You have installed LAN Whitelist on a dedicated server!");
		LOGGER.error("This mod will only do anything on a SINGLEPLAYER LAN world");
		LOGGER.error("You can ignore this error, but know that this won't do anything on a DEDICATED server.");
	}

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}
}

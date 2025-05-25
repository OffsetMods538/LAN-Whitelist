package top.offsetmonkey538.lanwhitelist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.integrated.IntegratedServer;
import top.offsetmonkey538.lanwhitelist.persistent.WhitelistPersistentState;

import java.io.IOException;

import static com.mojang.text2speech.Narrator.LOGGER;

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

			// Always add host player to whitelist. Wouldn't want to ban them from their singleplayer world now, would I?
			server.getPlayerManager().getWhitelist().add(new WhitelistEntry(integratedServer.getHostProfile()));

			try {
				server.getPlayerManager().getWhitelist().load();
			} catch (IOException e) {
				LOGGER.warn("Failed to load white-list: ", e);
			}
		});
	}
}

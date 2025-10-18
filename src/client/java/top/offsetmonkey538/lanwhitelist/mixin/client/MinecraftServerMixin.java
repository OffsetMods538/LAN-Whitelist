package top.offsetmonkey538.lanwhitelist.mixin.client;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.offsetmonkey538.lanwhitelist.LANWhitelist;
import top.offsetmonkey538.lanwhitelist.config.WhitelistEnabled;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Inject(
            method = "setUseAllowlist",
            at = @At("TAIL")
    )
    private void lan_whitelist$storeWhitelistEnabledIntoPersistentState(boolean whitelistEnabled, CallbackInfo ci) {
        final MinecraftServer thiz = ((MinecraftServer) (Object) this);
        if (!(thiz instanceof IntegratedServer server)) {
            LANWhitelist.logServer();
            return;
        }

        WhitelistEnabled.setWhitelistEnabled(server, whitelistEnabled);
    }
}

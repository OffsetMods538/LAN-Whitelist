package top.offsetmonkey538.lanwhitelist.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.offsetmonkey538.lanwhitelist.LANWhitelist;
import top.offsetmonkey538.lanwhitelist.LANWhitelistClient;
import top.offsetmonkey538.lanwhitelist.config.WhitelistEnabled;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Mutable
    @Final
    @Accessor("whitelist")
    protected abstract void setWhitelist(Whitelist whitelist);

    // Can't just modify on assignment, because "server" won't be set at that point yet.
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void lan_whitelist$storeSingleplayerWhitelistInWorldFolder(CallbackInfo ci) {
        final PlayerManager thiz = ((PlayerManager) (Object) this);
        if (!(thiz.getServer() instanceof IntegratedServer server)) {
            LANWhitelist.logServer();
            return;
        }

        final Path savePath = server.getSavePath(WorldSavePath.ROOT).resolve(LANWhitelist.MOD_ID).resolve(thiz.getWhitelist().getFile().getName());
        try {
            Files.createDirectories(savePath.getParent());
        } catch (IOException e) {
            LANWhitelist.LOGGER.error("Failed to create parent directories for whitelist file! Whitelist will fail to save.", e);
        }
        setWhitelist(new Whitelist(savePath.toFile()));
    }

    @Inject(
            method = "setWhitelistEnabled",
            at = @At("TAIL")
    )
    private void lan_whitelist$storeWhitelistEnabledIntoPersistentState(boolean whitelistEnabled, CallbackInfo ci) {
        final PlayerManager thiz = ((PlayerManager) (Object) this);
        if (!(thiz.getServer() instanceof IntegratedServer server)) {
            LANWhitelist.logServer();
            return;
        }

        WhitelistEnabled.setWhitelistEnabled(server, whitelistEnabled);
    }

    @Inject(
            method = "reloadWhitelist",
            at = @At("TAIL")
    )
    private void lan_whitelist$reloadWhitelistEvenOnIntegratedServer(CallbackInfo ci) {
        final PlayerManager thiz = ((PlayerManager) (Object) this);
        if (!(thiz.getServer() instanceof IntegratedServer)) {
            LANWhitelist.logServer();
            return;
        }

        try {
            thiz.getWhitelist().load();
        } catch (IOException e) {
            LANWhitelist.LOGGER.warn("Failed to load white-list: ", e);
        }
    }

    @Inject(
            method = "onPlayerConnect",
            at = @At("RETURN")
    )
    private void lan_whitelist$notifyHostOfWhitelistStatus(CallbackInfo ci, @Local(argsOnly = true) ServerPlayerEntity player) {
        if (!(player.getServer() instanceof IntegratedServer integratedServer)) {
            LANWhitelist.logServer();
            return;
        }

        LANWhitelistClient.sendEnabledMessageToHost(integratedServer, WhitelistEnabled.isWhitelistEnabled(integratedServer));
    }
}

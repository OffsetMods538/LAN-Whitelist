package top.offsetmonkey538.lanwhitelist.mixin.client;

import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PlayerSaveHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.offsetmonkey538.lanwhitelist.persistent.WhitelistPersistentState;

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
    private void lan_whitelist$storeSingleplayerWhitelistInWorldFolder(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, PlayerSaveHandler saveHandler, int maxPlayers, CallbackInfo ci) {
        final PlayerManager thiz = ((PlayerManager) (Object) this);
        if (thiz.getServer().isDedicated()) return;

        setWhitelist(new Whitelist(thiz.getServer().getSavePath(WorldSavePath.ROOT).resolve(thiz.getWhitelist().getFile().getName()).toFile()));
    }

    @Inject(
            method = "setWhitelistEnabled",
            at = @At("TAIL")
    )
    private void lan_whitelist$storeWhitelistEnabledIntoPersistentState(boolean whitelistEnabled, CallbackInfo ci) {
        final PlayerManager thiz = ((PlayerManager) (Object) this);
        if (thiz.getServer().isDedicated()) return;

        final WhitelistPersistentState state = WhitelistPersistentState.getServerState(thiz.getServer());
        state.enabled = whitelistEnabled;
        state.markDirty();
    }
}

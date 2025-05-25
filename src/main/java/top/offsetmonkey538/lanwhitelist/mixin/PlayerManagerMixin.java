package top.offsetmonkey538.lanwhitelist.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.Whitelist;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PlayerSaveHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Mutable
    @Final
    @Accessor("whitelist")
    protected abstract void setWhitelist(Whitelist whitelist);

    // @Redirect(
    //         method = "<init>",
    //         at = @At(
    //                 value = "FIELD",
    //                 target = "Lnet/minecraft/server/PlayerManager;whitelist:Lnet/minecraft/server/Whitelist;",
    //                 opcode = Opcodes.PUTFIELD
    //         )
    // )
    // private void lan_whitelist$storeSingleplayerWhitelistInWorldFolder(PlayerManager instance, Whitelist value) {
    //
    // }

    // @ModifyArg(
    //         method = "<init>",
    //         at = @At(
    //                 value = "INVOKE",
    //                 target = "Lnet/minecraft/server/Whitelist;<init>(Ljava/io/File;)V"
    //         )
    // )
    // private File lan_whitelist$storeSingleplayerWhitelistInWorldFolder(File file) {
    //     final PlayerManager thiz = ((PlayerManager) (Object) this);
    //     if (thiz.getServer().isDedicated()) return file;

    //     return thiz.getServer().getSavePath(WorldSavePath.ROOT).resolve(file.getName()).toFile();
    // }

    // Can't just modify on assignment like I tried above, because "server" wasn't set at that point yet.
    @Inject(
            method = "<init>",
            at = @At("TAIL")
    )
    private void lan_whitelist$storeSingleplayerWhitelistInWorldFolder(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, PlayerSaveHandler saveHandler, int maxPlayers, CallbackInfo ci) {
        final PlayerManager thiz = ((PlayerManager) (Object) this);
        if (thiz.getServer().isDedicated()) return;

        setWhitelist(new Whitelist(thiz.getServer().getSavePath(WorldSavePath.ROOT).resolve(thiz.getWhitelist().getFile().getName()).toFile()));
    }
}

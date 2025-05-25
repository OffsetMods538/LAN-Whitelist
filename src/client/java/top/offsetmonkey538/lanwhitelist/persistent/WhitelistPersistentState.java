package top.offsetmonkey538.lanwhitelist.persistent;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import static top.offsetmonkey538.lanwhitelist.LANWhitelist.MOD_ID;

public class WhitelistPersistentState extends PersistentState {
    private static final String ENABLED_KEY = "enabled";

    public Boolean enabled = false;

    private WhitelistPersistentState(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        this.enabled = nbt.getBoolean(ENABLED_KEY);
    }

    private WhitelistPersistentState() {

    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putBoolean(ENABLED_KEY, enabled);
        return nbt;
    }

    public static final Type<WhitelistPersistentState> type = new Type<>(
            WhitelistPersistentState::new,
            WhitelistPersistentState::new,
            null
    );

    public static WhitelistPersistentState getServerState(final MinecraftServer server) {
        final ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world == null) throw new IllegalStateException();

        return world.getPersistentStateManager().getOrCreate(type, MOD_ID);
    }
}

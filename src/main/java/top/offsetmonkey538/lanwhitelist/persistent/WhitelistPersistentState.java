package top.offsetmonkey538.lanwhitelist.persistent;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.Whitelist;
import net.minecraft.world.PersistentState;

public class WhitelistPersistentState extends PersistentState {

    public Boolean enabled = false;

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return null;
    }
}

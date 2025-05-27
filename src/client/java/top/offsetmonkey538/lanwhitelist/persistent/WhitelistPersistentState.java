package top.offsetmonkey538.lanwhitelist.persistent;

import com.mojang.datafixers.util.Function4;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import top.offsetmonkey538.lanwhitelist.LANWhitelist;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static top.offsetmonkey538.lanwhitelist.LANWhitelist.MOD_ID;

public class WhitelistPersistentState extends PersistentState {
    private static final String ENABLED_KEY = "enabled";
    private static final Codec<WhitelistPersistentState> CODEC = Codec.BOOL.xmap(WhitelistPersistentState::new, state -> state.enabled);

    public Boolean enabled = false;

    private WhitelistPersistentState(boolean enabled) {
        this.enabled = enabled;
    }

    private WhitelistPersistentState() {

    }

    // At some point, Minecraft started using the registry lookup stuff, but it's not available in 1.20, so I don't use '@Override' and have implementations both with and without the lookup
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return writeNbt(nbt);
    }
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putBoolean(ENABLED_KEY, enabled);
        return nbt;
    }



    public static WhitelistPersistentState getServerState(final MinecraftServer server) {
        final ServerWorld world = server.getWorld(World.OVERWORLD);
        if (world == null) throw new IllegalStateException();

        return VersionsHandler.getOrCreate.getOrCreate(world.getPersistentStateManager(), WhitelistPersistentState.CODEC, WhitelistPersistentState::new, MOD_ID);
    }

    // Version at the end of variables signifies the last supported version
    private static class VersionsHandler {
        private static final MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();

        private static final String getOrCreateV1d20d1 = resolver.mapMethodName(
                "intermediary",
                "net.minecraft.class_26", // PersistentStateManager
                "method_17924", // getOrCreate
                org.objectweb.asm.Type.getMethodDescriptor(
                        org.objectweb.asm.Type.getObjectType("net/minecraft/class_18"), // PersistentState
                        org.objectweb.asm.Type.getType(Function.class),
                        org.objectweb.asm.Type.getType(Supplier.class),
                        org.objectweb.asm.Type.getType(String.class)
                )
        );

        private static final String typeV1d21d4 = resolver.mapClassName(
                "intermediary",
                "net.minecraft.class_18$class_8645" // PersistentState.Type
        );
        private static final String getOrCreateV1d21d4 = resolver.mapMethodName(
                "intermediary",
                "net.minecraft.class_26", // PersistentStateManager
                "method_17924", // getOrCreate
                org.objectweb.asm.Type.getMethodDescriptor(
                        org.objectweb.asm.Type.getObjectType("net/minecraft/class_18"), // PersistentState
                        org.objectweb.asm.Type.getObjectType("net/minecraft/class_18$class_8645"), // PersistentState.Type
                        org.objectweb.asm.Type.getType(String.class)
                )
        );

        private static final String typeV1d21d5 = resolver.mapClassName(
                "intermediary",
                "net.minecraft.class_10741" // PersistentState.Type
        );
        private static final String getOrCreateV1d21d5 = resolver.mapMethodName(
                "intermediary",
                "net.minecraft.class_26", // PersistentStateManager
                "method_17924", // getOrCreate
                org.objectweb.asm.Type.getMethodDescriptor(
                        org.objectweb.asm.Type.getObjectType("net/minecraft/class_18"), // PersistentState
                        org.objectweb.asm.Type.getObjectType("net.minecraft.class_10741") // PersistentStateType
                )
        );


        private static final GetOrCreateMethod getOrCreate;

        static {
            GetOrCreateMethod result;
            try {
                final Method method = PersistentStateManager.class.getDeclaredMethod(getOrCreateV1d20d1, Function.class, Supplier.class, String.class);
                result = (manager, codec, emptySupplier, id) -> {
                    try {
                        return (WhitelistPersistentState) method.invoke(
                                manager,
                                (Function<NbtCompound, WhitelistPersistentState>) nbt -> CODEC.parse(NbtOps.INSTANCE, nbt).resultOrPartial(LANWhitelist.LOGGER::error).orElse(new WhitelistPersistentState()),
                                emptySupplier,
                                id
                        );
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                };
            } catch (NoSuchMethodException e) {
                LANWhitelist.LOGGER.info("1.20.1 getOrCreate doesn't exist, trying 1.21.4", e);

                try {
                    final Class<?> typeClass = Class.forName(typeV1d21d4);
                    final Constructor<?> typeConstructor = typeClass.getConstructor(
                            Supplier.class,
                            BiFunction.class,
                            DataFixTypes.class
                    );

                    final Method method = PersistentStateManager.class.getDeclaredMethod(getOrCreateV1d21d4, typeClass, String.class);

                    result = (manager, codec, emptySupplier, id) -> {
                        try {
                            return (WhitelistPersistentState) method.invoke(
                                    manager,
                                    typeConstructor.newInstance(emptySupplier, (BiFunction<NbtCompound, ?, WhitelistPersistentState>) (nbt, lookup) -> CODEC.parse(NbtOps.INSTANCE, nbt).resultOrPartial(LANWhitelist.LOGGER::error).orElse(new WhitelistPersistentState()), null),
                                    id
                            );
                        } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
                            throw new RuntimeException(ex);
                        }
                    };
                } catch (ClassNotFoundException | NoSuchMethodException e2) {
                    LANWhitelist.LOGGER.info("1.21.4 getOrCreate doesn't exist, trying 1.21.5", e2);

                    try {
                        final Class<?> typeClass = Class.forName(typeV1d21d5);
                        final Constructor<?> typeConstructor = typeClass.getConstructor(
                                String.class,
                                Supplier.class,
                                Codec.class,
                                DataFixTypes.class
                        );

                        final Method method = PersistentStateManager.class.getDeclaredMethod(getOrCreateV1d21d5, typeClass);

                        result = (manager, codec, emptySupplier, id) -> {
                            try {
                                return (WhitelistPersistentState) method.invoke(manager, typeConstructor.newInstance(
                                        id,
                                        emptySupplier,
                                        codec,
                                        null
                                ));
                            } catch (InvocationTargetException | IllegalAccessException | InstantiationException ex) {
                                throw new RuntimeException(ex);
                            }
                        };
                    } catch (ClassNotFoundException | NoSuchMethodException e3) {
                        throw new RuntimeException("1.21.5 also didn't match!", e3);
                    }
                }
            }
            getOrCreate = result;
        }
    }

    @FunctionalInterface
    private interface GetOrCreateMethod {
        WhitelistPersistentState getOrCreate(PersistentStateManager manager, Codec<WhitelistPersistentState> codec, Supplier<WhitelistPersistentState> emptySupplier, String id);
    }
}

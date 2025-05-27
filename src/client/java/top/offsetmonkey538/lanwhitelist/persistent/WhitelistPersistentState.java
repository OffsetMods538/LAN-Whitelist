package top.offsetmonkey538.lanwhitelist.persistent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
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
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
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


    //  The 'writeNbt' method needs to be overriden in versions up to (and including) 1.21.4.
    //
    //  First problem is that before 1.20.5, the method just took an NbtCompound, but up to 1.21.4 it also takes a registry wrapper.
    //   Solution: include both method (without @Override) definitions so the correct method will always be implemented.
    //
    //  Second problem is that neither of the super methods actually exist at build time, because 1.21.5 doesn't use nbt for this at all. (also a problem when building on 1.21.4 or 1.20.4, just then only one of the methods doesn't exist)
    //  Thus loom won't know to remap them to anything, and they'll be left with just the yarn names, which won't be implemented when anything other than yarn is used (for example production where intermediary is used)
    //   Solution: include the methods with names from the three main mappings that could be used: yarn, intermediary, mojmaps

    //  With registry wrapper (?.??.? - 1.20.4)

    // yarn
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return writeNbtImplementation(nbt);
    }
    // intermediary
    public NbtCompound method_75(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return writeNbtImplementation(nbt);
    }
    // mojmaps
    public NbtCompound save(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        return writeNbtImplementation(nbt);
    }

    //  Without registry wrapper (1.20.5 - 1.21.4)

    public NbtCompound writeNbt(NbtCompound nbt) {
        return writeNbtImplementation(nbt);
    }
    // intermediary
    public NbtCompound method_75(NbtCompound nbt) {
        return writeNbtImplementation(nbt);
    }
    // mojmaps
    public NbtCompound save(NbtCompound nbt) {
        return writeNbtImplementation(nbt);
    }

    //  Actual implementation of it
    public NbtCompound writeNbtImplementation(NbtCompound nbt) {
        return (NbtCompound) CODEC.encodeStart(NbtOps.INSTANCE, this).resultOrPartial(LANWhitelist.LOGGER::error).orElseThrow();
        //nbt.putBoolean(ENABLED_KEY, enabled);
        //return nbt;
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
                        org.objectweb.asm.Type.getObjectType("net/minecraft/class_10741") // PersistentStateType
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
                                (Function<NbtCompound, WhitelistPersistentState>) nbt -> getResultOrPartialFromNbt(CODEC, nbt, LANWhitelist.LOGGER::error).orElse(new WhitelistPersistentState()),
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
                                    typeConstructor.newInstance(emptySupplier, (BiFunction<NbtCompound, ?, WhitelistPersistentState>) (nbt, lookup) -> getResultOrPartialFromNbt(CODEC, nbt, LANWhitelist.LOGGER::error).orElse(new WhitelistPersistentState()), null),
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

        private static <T extends PersistentState> Optional<T> getResultOrPartialFromNbt(Codec<T> codec, NbtCompound nbt, Consumer<String> onError) {
            try {
                final Object dataResult = codec.parse(NbtOps.INSTANCE, nbt);
                final Method getResultOrPartial = dataResult.getClass().getDeclaredMethod("resultOrPartial", Consumer.class);
                //noinspection unchecked
                return (Optional<T>) getResultOrPartial.invoke(dataResult, onError);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @FunctionalInterface
    private interface GetOrCreateMethod {
        WhitelistPersistentState getOrCreate(PersistentStateManager manager, Codec<WhitelistPersistentState> codec, Supplier<WhitelistPersistentState> emptySupplier, String id);
    }
}

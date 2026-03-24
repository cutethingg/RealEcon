package com.femtendo.realecon.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerWealthProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    // The official reference to our capability
    public static Capability<PlayerWealth> PLAYER_WEALTH = CapabilityManager.get(new CapabilityToken<PlayerWealth>() { });

    private PlayerWealth wealth = null;
    private final LazyOptional<PlayerWealth> optional = LazyOptional.of(this::createPlayerWealth);

    private PlayerWealth createPlayerWealth() {
        if (this.wealth == null) {
            this.wealth = new PlayerWealth();
        }
        return this.wealth;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == PLAYER_WEALTH) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerWealth().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerWealth().loadNBTData(nbt);
    }
}
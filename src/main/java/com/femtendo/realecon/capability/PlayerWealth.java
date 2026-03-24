package com.femtendo.realecon.capability;

import net.minecraft.nbt.CompoundTag;

public class PlayerWealth {
    public enum AdminMode { NORMAL, OWN_ALL, OWN_NONE }

    private long balance;             // Current cash
    private long netWorth;            // Total value (cash + items)
    private long lastCheckedWealth;   // Result of last scan
    private long lastCheckTimeMs;     // Timestamp of last scan
    private AdminMode debugMode = AdminMode.NORMAL;

    // --- Basic Getters/Setters ---
    public long getBalance() { return balance; }
    public void setBalance(long balance) { this.balance = balance; }

    public long getNetWorth() { return netWorth; }
    public void setNetWorth(long netWorth) { this.netWorth = netWorth; }

    public AdminMode getDebugMode() { return debugMode; }
    public void setDebugMode(AdminMode debugMode) { this.debugMode = debugMode; }

    // --- Scanner Logic Getters/Setters ---
    public long getLastCheckedWealth() { return lastCheckedWealth; }
    public long getLastCheckTimeMs() { return lastCheckTimeMs; }

    public void setLastCheckedData(long amount, long timeMs) {
        this.lastCheckedWealth = amount;
        this.lastCheckTimeMs = timeMs;
    }

    // --- Capability Paperwork ---
    public void copyFrom(PlayerWealth source) {
        this.balance = source.balance;
        this.netWorth = source.netWorth;
        this.lastCheckedWealth = source.lastCheckedWealth;
        this.lastCheckTimeMs = source.lastCheckTimeMs;
        this.debugMode = source.debugMode;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putLong("balance", balance);
        nbt.putLong("netWorth", netWorth);
        nbt.putLong("lastCheckedWealth", lastCheckedWealth);
        nbt.putLong("lastCheckTimeMs", lastCheckTimeMs);
        nbt.putInt("adminMode", debugMode.ordinal());
    }

    public void loadNBTData(CompoundTag nbt) {
        balance = nbt.getLong("balance");
        netWorth = nbt.getLong("netWorth");
        lastCheckedWealth = nbt.getLong("lastCheckedWealth");
        lastCheckTimeMs = nbt.getLong("lastCheckTimeMs");
        debugMode = AdminMode.values()[nbt.getInt("adminMode")];
    }
}
package com.femtendo.realecon.logic;

import com.femtendo.realecon.RealEcon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SecurityLogger {
    // The threshold: Flag if wealth increases by this amount within the time window
    private static final long SUSPICIOUS_AMOUNT = 100000;
    private static final long TIME_WINDOW_MS = 5000; // 5 seconds

    public static void audit(ServerPlayer player, long newBalance, long oldBalance, long lastCheckTime) {
        long currentTime = System.currentTimeMillis();
        long timeDifference = currentTime - lastCheckTime;
        long wealthDifference = newBalance - oldBalance;

        // If this is their first time logging in, ignore the spike
        if (lastCheckTime == 0) return;

        // Velocity Check: Did they gain a massive amount of money in a tiny amount of time?
        if (wealthDifference >= SUSPICIOUS_AMOUNT && timeDifference <= TIME_WINDOW_MS) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String dimension = player.level().dimension().location().toString();
            String coords = String.format("X:%.0f Y:%.0f Z:%.0f", player.getX(), player.getY(), player.getZ());

            String logEntry = String.format("[%s] FLAG: %s spiked by $%d in %dms! | New Bal: $%d | Loc: %s [%s]",
                    timestamp, player.getName().getString(), wealthDifference, timeDifference, newBalance, dimension, coords);

            // 1. Print to Server Console
            System.err.println("[REALECON SECURITY] " + logEntry);

            // 2. Write to Physical Audit Log File
            writeToPhysicalFile(logEntry);
        }
    }

    private static void writeToPhysicalFile(String logEntry) {
        try {
            File logDir = new File(FMLPaths.GAMEDIR.get().toFile(), "logs");
            if (!logDir.exists()) logDir.mkdir();

            File auditFile = new File(logDir, "realecon_audit.log");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(auditFile, true))) {
                writer.write(logEntry);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("[RealEcon] Failed to write to audit log: " + e.getMessage());
        }
    }
}
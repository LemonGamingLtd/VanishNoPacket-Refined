package org.kitteh.vanish.compat;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

public interface SchedulerAdapter {

    void runDelayed(Plugin plugin, Runnable task, long delayTicks);

    void runDelayedAt(Plugin plugin, Location location, Runnable task, long delayTicks);

    void runAtFixedRate(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks);

    static SchedulerAdapter create(Plugin plugin) {
        if (isPaper()) {
            return new PaperSchedulerAdapter();
        }
        return new BukkitSchedulerAdapter();
    }

    static boolean isPaper() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
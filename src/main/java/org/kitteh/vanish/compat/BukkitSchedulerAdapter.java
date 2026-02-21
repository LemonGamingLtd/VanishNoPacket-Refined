package org.kitteh.vanish.compat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

final class BukkitSchedulerAdapter implements SchedulerAdapter {

    @Override
    public void runDelayed(Plugin plugin, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runDelayedAt(Plugin plugin, Location location, Runnable task, long delayTicks) {
        Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
    }

    @Override
    public void runAtFixedRate(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
        Bukkit.getScheduler().runTaskTimer(plugin, task, initialDelayTicks, periodTicks);
    }
}
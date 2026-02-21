package org.kitteh.vanish.compat;

import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

final class PaperSchedulerAdapter implements SchedulerAdapter {

    @Override
    public void runDelayed(Plugin plugin, Runnable task, long delayTicks) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, ignored -> task.run(), delayTicks);
    }

    @Override
    public void runDelayedAt(Plugin plugin, Location location, Runnable task, long delayTicks) {
        plugin.getServer().getRegionScheduler().runDelayed(plugin, location, ignored -> task.run(), delayTicks);
    }

    @Override
    public void runAtFixedRate(Plugin plugin, Runnable task, long initialDelayTicks, long periodTicks) {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, ignored -> task.run(), initialDelayTicks, periodTicks);
    }
}
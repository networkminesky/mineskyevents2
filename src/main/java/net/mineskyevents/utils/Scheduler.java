package net.mineskyevents.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public final class Scheduler {

    private static Plugin plugin;

    private Scheduler() {}

    public static void init(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static Task runTask(Runnable runnable) {
        return wrap(Bukkit.getScheduler().runTask(plugin, runnable));
    }

    public static Task runTaskLater(Runnable runnable, long delayTicks) {
        return wrap(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks));
    }

    public static Task runTaskTimer(Runnable runnable, long delayTicks, long periodTicks) {
        return wrap(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks));
    }

    public static Task runTaskAsync(Runnable runnable) {
        return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
    }

    private static Task wrap(org.bukkit.scheduler.BukkitTask task) {
        return new Task() {
            @Override
            public void cancel() {
                task.cancel();
            }

            @Override
            public boolean isCancelled() {
                return task.isCancelled();
            }
        };
    }

    public interface Task {
        void cancel();
        boolean isCancelled();
    }
}

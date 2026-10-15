package net.mineskyevents.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class Scheduler {

    private static Impl impl;

    /**
     * Inicializa o Wrapper de Schedulers.
     * Deve ser chamado obrigatoriamente no onEnable() do seu Plugin.
     *
     * @param plugin A instância do seu JavaPlugin
     */
    public static void init(Plugin plugin) {
        if (impl != null) return;

        if (isFolia()) {
            try {
                impl = (Impl) Class.forName("net.mineskyevents.utils.Scheduler$FoliaImpl")
                        .getConstructor(Plugin.class)
                        .newInstance(plugin);
            } catch (Exception e) {
                plugin.getLogger().warning("Não foi possível carregar o scheduler nativo do Folia. Usando o Bukkit clássico.");
                impl = new BukkitImpl(plugin);
            }
        } else {
            impl = new BukkitImpl(plugin);
        }
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Task runTask(Runnable runnable) {
        return impl.runTask(runnable);
    }

    public static Task runTaskLater(Runnable runnable, long delayTicks) {
        return impl.runTaskLater(runnable, delayTicks);
    }

    public static Task runTaskTimer(Runnable runnable, long delayTicks, long periodTicks) {
        return impl.runTaskTimer(runnable, delayTicks, periodTicks);
    }

    public static Task runTaskAsync(Runnable runnable) {
        return impl.runTaskAsync(runnable);
    }

    public static Task runTaskLaterAsync(Runnable runnable, long delayTicks) {
        return impl.runTaskLaterAsync(runnable, delayTicks);
    }

    public static Task runTaskTimerAsync(Runnable runnable, long delayTicks, long periodTicks) {
        return impl.runTaskTimerAsync(runnable, delayTicks, periodTicks);
    }

    public static Task runTaskAtLocation(Location location, Runnable runnable) {
        return impl.runTaskAtLocation(location, runnable);
    }

    public static Task runTaskLaterAtLocation(Location location, Runnable runnable, long delayTicks) {
        return impl.runTaskLaterAtLocation(location, runnable, delayTicks);
    }

    public static Task runTaskTimerAtLocation(Location location, Runnable runnable, long delayTicks, long periodTicks) {
        return impl.runTaskTimerAtLocation(location, runnable, delayTicks, periodTicks);
    }

    public static Task runTaskAtEntity(Entity entity, Runnable runnable) {
        return runTaskAtEntity(entity, runnable, null);
    }

    public static Task runTaskAtEntity(Entity entity, Runnable runnable, Runnable retired) {
        return impl.runTaskAtEntity(entity, runnable, retired);
    }

    public static Task runTaskLaterAtEntity(Entity entity, Runnable runnable, long delayTicks) {
        return runTaskLaterAtEntity(entity, runnable, null, delayTicks);
    }

    public static Task runTaskLaterAtEntity(Entity entity, Runnable runnable, Runnable retired, long delayTicks) {
        return impl.runTaskLaterAtEntity(entity, runnable, retired, delayTicks);
    }

    public static Task runTaskTimerAtEntity(Entity entity, Runnable runnable, long delayTicks, long periodTicks) {
        return runTaskTimerAtEntity(entity, runnable, null, delayTicks, periodTicks);
    }

    public static Task runTaskTimerAtEntity(Entity entity, Runnable runnable, Runnable retired, long delayTicks, long periodTicks) {
        return impl.runTaskTimerAtEntity(entity, runnable, retired, delayTicks, periodTicks);
    }

    public interface Task {
        void cancel();
        boolean isCancelled();
    }

    public interface Impl {
        Task runTask(Runnable runnable);
        Task runTaskAsync(Runnable runnable);
        Task runTaskLater(Runnable runnable, long delayTicks);
        Task runTaskLaterAsync(Runnable runnable, long delayTicks);
        Task runTaskTimer(Runnable runnable, long delayTicks, long periodTicks);
        Task runTaskTimerAsync(Runnable runnable, long delayTicks, long periodTicks);

        Task runTaskAtLocation(Location location, Runnable runnable);
        Task runTaskLaterAtLocation(Location location, Runnable runnable, long delayTicks);
        Task runTaskTimerAtLocation(Location location, Runnable runnable, long delayTicks, long periodTicks);

        Task runTaskAtEntity(Entity entity, Runnable runnable, Runnable retired);
        Task runTaskLaterAtEntity(Entity entity, Runnable runnable, Runnable retired, long delayTicks);
        Task runTaskTimerAtEntity(Entity entity, Runnable runnable, Runnable retired, long delayTicks, long periodTicks);
    }

    public static class BukkitImpl implements Impl {
        private final Plugin plugin;

        public BukkitImpl(Plugin plugin) {
            this.plugin = plugin;
        }

        private Task wrap(org.bukkit.scheduler.BukkitTask task) {
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

        @Override
        public Task runTask(Runnable runnable) {
            return wrap(Bukkit.getScheduler().runTask(plugin, runnable));
        }

        @Override
        public Task runTaskAsync(Runnable runnable) {
            return wrap(Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable));
        }

        @Override
        public Task runTaskLater(Runnable runnable, long delayTicks) {
            return wrap(Bukkit.getScheduler().runTaskLater(plugin, runnable, delayTicks));
        }

        @Override
        public Task runTaskLaterAsync(Runnable runnable, long delayTicks) {
            return wrap(Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delayTicks));
        }

        @Override
        public Task runTaskTimer(Runnable runnable, long delayTicks, long periodTicks) {
            return wrap(Bukkit.getScheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks));
        }

        @Override
        public Task runTaskTimerAsync(Runnable runnable, long delayTicks, long periodTicks) {
            return wrap(Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delayTicks, periodTicks));
        }

        @Override
        public Task runTaskAtLocation(Location location, Runnable runnable) {
            return runTask(runnable);
        }

        @Override
        public Task runTaskLaterAtLocation(Location location, Runnable runnable, long delayTicks) {
            return runTaskLater(runnable, delayTicks);
        }

        @Override
        public Task runTaskTimerAtLocation(Location location, Runnable runnable, long delayTicks, long periodTicks) {
            return runTaskTimer(runnable, delayTicks, periodTicks);
        }

        @Override
        public Task runTaskAtEntity(Entity entity, Runnable runnable, Runnable retired) {
            return runTask(() -> {
                if (entity.isValid()) {
                    runnable.run();
                } else if (retired != null) {
                    retired.run();
                }
            });
        }

        @Override
        public Task runTaskLaterAtEntity(Entity entity, Runnable runnable, Runnable retired, long delayTicks) {
            return runTaskLater(() -> {
                if (entity.isValid()) {
                    runnable.run();
                } else if (retired != null) {
                    retired.run();
                }
            }, delayTicks);
        }

        @Override
        public Task runTaskTimerAtEntity(Entity entity, Runnable runnable, Runnable retired, long delayTicks, long periodTicks) {
            return runTaskTimer(() -> {
                if (entity.isValid()) {
                    runnable.run();
                } else if (retired != null) {
                    retired.run();
                }
            }, delayTicks, periodTicks);
        }
    }

    // Alterado para classe pública para ser instanciável por Class.forName()
    @SuppressWarnings("unused")
    public static class FoliaImpl implements Impl {
        private final Plugin plugin;

        public FoliaImpl(Plugin plugin) {
            this.plugin = plugin;
        }

        private Task wrap(io.papermc.paper.threadedregions.scheduler.ScheduledTask task) {
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

        @Override
        public Task runTask(Runnable runnable) {
            return wrap(Bukkit.getGlobalRegionScheduler().run(plugin, t -> runnable.run()));
        }

        @Override
        public Task runTaskAsync(Runnable runnable) {
            return wrap(Bukkit.getAsyncScheduler().runNow(plugin, t -> runnable.run()));
        }

        @Override
        public Task runTaskLater(Runnable runnable, long delayTicks) {
            return wrap(Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> runnable.run(), Math.max(1L, delayTicks)));
        }

        @Override
        public Task runTaskLaterAsync(Runnable runnable, long delayTicks) {
            return wrap(Bukkit.getAsyncScheduler().runDelayed(plugin, t -> runnable.run(), delayTicks * 50L, TimeUnit.MILLISECONDS));
        }

        @Override
        public Task runTaskTimer(Runnable runnable, long delayTicks, long periodTicks) {
            return wrap(Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, t -> runnable.run(), Math.max(1L, delayTicks), Math.max(1L, periodTicks)));
        }

        @Override
        public Task runTaskTimerAsync(Runnable runnable, long delayTicks, long periodTicks) {
            return wrap(Bukkit.getAsyncScheduler().runAtFixedRate(plugin, t -> runnable.run(), delayTicks * 50L, periodTicks * 50L, TimeUnit.MILLISECONDS));
        }

        @Override
        public Task runTaskAtLocation(Location location, Runnable runnable) {
            return wrap(Bukkit.getRegionScheduler().run(plugin, location, t -> runnable.run()));
        }

        @Override
        public Task runTaskLaterAtLocation(Location location, Runnable runnable, long delayTicks) {
            return wrap(Bukkit.getRegionScheduler().runDelayed(plugin, location, t -> runnable.run(), Math.max(1L, delayTicks)));
        }

        @Override
        public Task runTaskTimerAtLocation(Location location, Runnable runnable, long delayTicks, long periodTicks) {
            return wrap(Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, t -> runnable.run(), Math.max(1L, delayTicks), Math.max(1L, periodTicks)));
        }

        @Override
        public Task runTaskAtEntity(Entity entity, Runnable runnable, Runnable retired) {
            return wrap(entity.getScheduler().run(plugin, t -> runnable.run(), retired));
        }

        @Override
        public Task runTaskLaterAtEntity(Entity entity, Runnable runnable, Runnable retired, long delayTicks) {
            return wrap(entity.getScheduler().runDelayed(plugin, t -> runnable.run(), retired, Math.max(1L, delayTicks)));
        }

        @Override
        public Task runTaskTimerAtEntity(Entity entity, Runnable runnable, Runnable retired, long delayTicks, long periodTicks) {
            return wrap(entity.getScheduler().runAtFixedRate(plugin, t -> runnable.run(), retired, Math.max(1L, delayTicks), Math.max(1L, periodTicks)));
        }
    }
}
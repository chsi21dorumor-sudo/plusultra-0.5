package com.plusultra.quirk;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;

/** Tiny server-thread task queue: run something N ticks from now (acid pools, despawn timers, boss attacks). */
public final class Scheduler {
    private static final Logger LOGGER = LogUtils.getLogger();

    private record Task(long due, Runnable run) {}

    private static final List<Task> TASKS = new ArrayList<>();
    private static long now = 0;

    private Scheduler() {}

    public static void schedule(int delayTicks, Runnable run) {
        TASKS.add(new Task(now + Math.max(1, delayTicks), run));
    }

    /** Call once per server tick. */
    public static void tick() {
        now++;
        if (TASKS.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (t.due <= now) {
                due.add(t);
                it.remove();
            }
        }
        for (Task t : due) {
            try {
                t.run.run();
            } catch (RuntimeException e) {
                LOGGER.error("Plus Ultra scheduled task failed", e);
            }
        }
    }

    public static void clear() {
        TASKS.clear();
    }
}

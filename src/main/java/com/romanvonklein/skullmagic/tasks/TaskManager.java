package com.romanvonklein.skullmagic.tasks;

import java.util.ArrayList;

import com.romanvonklein.skullmagic.SkullMagic;

public class TaskManager {
    private ArrayList<DelayedTask> queued = new ArrayList<>();

    public void tick() {
        ArrayList<DelayedTask> done = new ArrayList<>();
        for (DelayedTask task : this.queued) {
            if (task.delayRemaining > 0) {
                SkullMagic.LOGGER.info("Counting down from " + task.delayRemaining);
                task.delayRemaining--;
                SkullMagic.LOGGER.info("to " + task.delayRemaining);
            } else {
                try {
                    if (task.action()) {
                        done.add(task);
                    }
                } catch (Exception e) {
                    SkullMagic.LOGGER.warn("Failed executing task: " + task.name + ". Exception: " + e.toString());
                }
            }
        }
        for (DelayedTask task : done) {
            SkullMagic.LOGGER.info("Removing finished task: " + task.name);

            queued.remove(task);
        }
    }

    public void queueTask(DelayedTask task) {
        this.queued.add(task);
    }

}

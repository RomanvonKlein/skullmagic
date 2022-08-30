package com.romanvonklein.skullmagic.tasks;

import java.util.ArrayList;

public class TaskManager {
    private ArrayList<DelayedTask> queued = new ArrayList<>();

    public void tick() {
        ArrayList<DelayedTask> done = new ArrayList<>();
        for (DelayedTask task : this.queued) {
            if (task.delayRemaining > 0) {
                task.delayRemaining--;
            } else {
                if (task.action()) {
                    done.add(task);
                }
            }
        }
        for (DelayedTask task : done) {
            queued.remove(task);
        }
    }

    public void queueTask(DelayedTask task) {
        this.queued.add(task);
    }

}

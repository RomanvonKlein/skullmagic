package com.romanvonklein.skullmagic.tasks;

import org.apache.commons.lang3.function.TriFunction;

public class DelayedTask {
    public int delayRemaining;
    private Object[] data;

    private TriFunction<Object[], Object, Object, Boolean> actionFunc;
    public String name;

    public DelayedTask(String name, int ticks, TriFunction<Object[], Object, Object, Boolean> triFunction,
            Object[] data) {
        this.name = name;
        this.delayRemaining = ticks;
        this.data = data;
        this.actionFunc = triFunction;
    }

    public boolean action() {
        return this.actionFunc.apply(this.data, null, null);
    }
}

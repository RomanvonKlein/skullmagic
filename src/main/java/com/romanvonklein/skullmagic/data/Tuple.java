package com.romanvonklein.skullmagic.data;

public class Tuple<S1, S2> {
    public S1 first;
    public S2 second;

    public Tuple(S1 first, S2 second) {
        this.first = first;
        this.second = second;
    }
}

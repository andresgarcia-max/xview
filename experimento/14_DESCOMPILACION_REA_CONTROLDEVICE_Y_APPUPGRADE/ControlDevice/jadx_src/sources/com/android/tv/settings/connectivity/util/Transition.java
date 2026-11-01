package com.android.tv.settings.connectivity.util;

/* loaded from: classes.dex */
public class Transition {
    public State destination;
    public int event;
    public State source;

    public Transition(State state, int i, State state2) {
        this.source = state;
        this.event = i;
        this.destination = state2;
    }
}

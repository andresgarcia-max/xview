package com.control.device.settings.connectivity;

import androidx.lifecycle.ViewModel;
import com.android.tv.settings.connectivity.util.Transition;
import com.control.device.settings.connectivity.State;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* loaded from: classes.dex */
public class StateMachine extends ViewModel {
    private Callback mCallback;
    private Map<State, List<Transition>> mTransitionMap = new HashMap();
    private LinkedList<State> mStatesList = new LinkedList<>();
    private State.StateCompleteListener mCompletionListener = new State.StateCompleteListener() { // from class: com.control.device.settings.connectivity.StateMachine$$ExternalSyntheticLambda0
        @Override // com.control.device.settings.connectivity.State.StateCompleteListener
        public final void onComplete(int i) {
            StateMachine.this.updateState(i);
        }
    };

    public interface Callback {
        void onFinish(int i);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void addState(State state, int i, State state2) {
        if (!this.mTransitionMap.containsKey(state)) {
            this.mTransitionMap.put(state, new ArrayList());
        }
        this.mTransitionMap.get(state).add(new Transition(state, i, state2));
    }

    public void setStartState(State state) {
        this.mStatesList.addLast(state);
    }

    public void start(boolean z) {
        if (this.mStatesList.isEmpty()) {
            throw new IllegalArgumentException("Start state not set");
        }
        State currentState = getCurrentState();
        if (z) {
            currentState.processForward();
        } else {
            currentState.processBackward();
        }
    }

    public void back() {
        updateState(1);
    }

    public State getCurrentState() {
        if (this.mStatesList.isEmpty()) {
            return null;
        }
        return this.mStatesList.getLast();
    }

    public void finish(int i) {
        this.mCallback.onFinish(i);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateState(int i) {
        if (i == 4) {
            finish(-1);
            return;
        }
        if (i == 3) {
            finish(0);
            return;
        }
        if (i == 1) {
            if (this.mStatesList.size() < 2) {
                this.mCallback.onFinish(0);
                return;
            } else {
                this.mStatesList.removeLast();
                this.mStatesList.getLast().processBackward();
                return;
            }
        }
        State state = null;
        State currentState = getCurrentState();
        if (this.mTransitionMap.get(currentState) != null) {
            for (Transition transition : this.mTransitionMap.get(currentState)) {
                if (transition.event == i) {
                    state = (State) transition.destination;
                }
            }
        }
        if (state == null) {
            if (i == 2) {
                this.mCallback.onFinish(-1);
                return;
            }
            throw new IllegalArgumentException(getCurrentState().getClass() + "Invalid transition " + i);
        }
        addToStack(state);
        state.processForward();
    }

    private void addToStack(State state) {
        for (int size = this.mStatesList.size() - 1; size >= 0; size--) {
            if (equal(state, this.mStatesList.get(size))) {
                for (int size2 = this.mStatesList.size() - 1; size2 >= size; size2--) {
                    this.mStatesList.removeLast();
                }
            }
        }
        this.mStatesList.addLast(state);
    }

    private boolean equal(State state, State state2) {
        return state.getClass().equals(state2.getClass());
    }

    public State.StateCompleteListener getListener() {
        return this.mCompletionListener;
    }
}

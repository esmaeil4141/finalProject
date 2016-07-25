package io.sharif.pavilion.network.StateControllers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.sharif.pavilion.network.StateControllers.MachineStates.ClientState;

public class ClientStateController {

    private final HashMap<ClientState, List<ClientState>> stateMachine;
    {
        stateMachine = new HashMap<>();
        stateMachine.put(
                ClientState.STARTED,
                Arrays.asList(
                        ClientState.IDLE,
                        ClientState.STOPPED
                ));
        stateMachine.put(
                ClientState.JOINING,
                Arrays.asList(
                        ClientState.STARTED,
                        ClientState.LEFT
                ));
        stateMachine.put(
                ClientState.JOINED,
                Collections.singletonList(
                        ClientState.JOINING
                ));
        stateMachine.put(
                ClientState.CONNECTING,
                Arrays.asList(
                        ClientState.JOINED,
                        ClientState.DISCONNECTED
                ));
        stateMachine.put(
                ClientState.CONNECTED,
                Collections.singletonList(
                        ClientState.CONNECTING
                ));
        stateMachine.put(
                ClientState.DISCONNECTING,
                Collections.singletonList(
                        ClientState.CONNECTED
                ));
        stateMachine.put(
                ClientState.DISCONNECTED,
                Collections.singletonList(
                        ClientState.DISCONNECTING
                ));
        stateMachine.put(
                ClientState.LEAVING,
                Arrays.asList(
                        ClientState.DISCONNECTED,
                        ClientState.JOINED
                ));
        stateMachine.put(
                ClientState.LEFT,
                Collections.singletonList(
                        ClientState.LEAVING
                ));
        stateMachine.put(
                ClientState.STOPPED,
                Arrays.asList(
                        ClientState.STARTED,
                        ClientState.LEFT
                ));
    }

    private ClientState currentState;

    public ClientStateController() {
        this.currentState = ClientState.IDLE;
    }

    public synchronized boolean checkAndReturn(ClientState state) {
        List<ClientState> list = stateMachine.get(state);
        for (ClientState clientState : list) {
            if (currentState == clientState)
                return true;
        }
        return false;
    }

    public synchronized void setCurrentState(ClientState state) {
        this.currentState = state;
    }

    public synchronized ClientState getCurrentState() {
        return currentState;
    }
}

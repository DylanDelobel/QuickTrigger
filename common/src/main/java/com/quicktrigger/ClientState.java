package com.quicktrigger;

public final class ClientState {
    public static volatile String currentServerKey = null;
    public static volatile boolean serverHasMod = false;
    public static volatile int maxHomes = 1;

    private ClientState() {}
}

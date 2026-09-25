package com.quicktrigger;

// shared between the loaders + ConfigScreen, written from the netty thread so volatile
public final class ClientState {
    public static volatile String currentServerKey = null;//null = not connected
    public static volatile boolean serverHasMod= false;
    public static volatile int maxHomes=1;

    private ClientState() {}
}

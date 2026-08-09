package com.imherobrine.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

final class FabricServerEvents {
    private FabricServerEvents() {
    }

    static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
        });
    }
}

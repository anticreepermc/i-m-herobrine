package com.imherobrine.fabric;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import com.imherobrine.common.HerobrineGameLogic;

final class FabricServerEvents {
    private FabricServerEvents() {
    }

    static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            HerobrineGameLogic.placeTotemIfNeeded(server.overworld());
        });
    }
}

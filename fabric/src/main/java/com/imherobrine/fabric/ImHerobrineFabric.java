package com.imherobrine.fabric;

import net.fabricmc.api.ModInitializer;

public final class ImHerobrineFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricNetworking.register();
        FabricServerEvents.register();
    }
}

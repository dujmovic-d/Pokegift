package io.github.polymeta.pokegift.fabric;

import io.github.polymeta.pokegift.Pokegift;
import net.fabricmc.api.ModInitializer;

public class PokegiftFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Pokegift.init();
    }
}

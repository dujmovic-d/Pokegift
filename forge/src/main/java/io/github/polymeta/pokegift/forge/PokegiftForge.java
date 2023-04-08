package io.github.polymeta.pokegift.forge;

import io.github.polymeta.pokegift.Pokegift;
import net.minecraftforge.fml.common.Mod;

@Mod(Pokegift.MOD_ID)
public class PokegiftForge {

    public PokegiftForge() {
        Pokegift.init();
    }
}

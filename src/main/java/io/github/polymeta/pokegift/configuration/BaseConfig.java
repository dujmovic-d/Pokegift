package io.github.polymeta.pokegift.configuration;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.polymeta.pokegift.Pokegift;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BaseConfig {
    public static Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create();

    public boolean cooldownEnabled = true;
    public int cooldown = 5;
    public List<String> blacklist = new ArrayList<>();

    public MessageConfig messages = new MessageConfig();

    public static class MessageConfig {
        public String pokegiftFeedback = "<gray>[<white>Poke<red>gift<gray>] <white>Are you sure you want to gift your" +
                " <aqua>lvl <level> <pokemon></aqua> to <green><player></green>?<giftconfirm><yellow> Click here to confirm!</giftconfirm>";
        public String cooldownFeedback = "<gray>[<white>Poke<red>gift<gray>] <red>You are on cooldown!";
        public String pokemonNotAllowed = "<gray>[<white>Poke<red>gift<gray>] <red>You cannot gift this pokemon!";
        public String receivedPokemonFeedback = "<gray>[<white>Poke<red>gift<gray>] <green><player> gifted you a <pokemon>!";
        public String successFeedback = "<gray>[<white>Poke<red>gift<gray>] <green>Successfully gifted your pokemon!";
        public String errorCantGiftYourself = "<gray>[<white>Poke<red>gift<gray>]<red> Can't gift yourself pokemon!";
        public String errorCouldntTakePokemon = "<gray>[<white>Poke<red>gift<gray>]<red> Something went wrong taking the pokemon from you";
        public String errorCouldntGivePokemon = "<gray>[<white>Poke<red>gift<gray>]<red> Something went wrong sending the pokemon to <player>";


        public Text pokegiftFeedback(Pokemon pokemon, int slot, ServerPlayerEntity targetPlayer) {
            var miniMessage = MiniMessage.builder()
                    .tags(TagResolver.builder()
                            .resolvers(TagResolver.standard())
                            .resolver(TagResolver.resolver("giftconfirm", Tag.styling(ClickEvent.runCommand("/pokegift " + (slot + 1) + " " + targetPlayer.getName().getString() + " --confirm"))))
                            .build())
                    .build();

            var text = miniMessage.deserialize(this.pokegiftFeedback,
                    Placeholder.unparsed("level", String.valueOf(pokemon.getLevel())),
                    Placeholder.unparsed("pokemon", pokemon.getDisplayName().getString()),
                    Placeholder.unparsed("player", targetPlayer.getName().getString()));

            return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
        }

        public Text cooldownFeedback() {
            var text = Pokegift.miniMessage.deserialize(this.cooldownFeedback);
            return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
        }

        public Text successFeedback() {
            var text = Pokegift.miniMessage.deserialize(this.successFeedback);
            return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
        }

        public Text pokemonNotAllowed() {
            var text = Pokegift.miniMessage.deserialize(this.pokemonNotAllowed);
            return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
        }

        public Text errorCantGiftYourself() {
            var text = Pokegift.miniMessage.deserialize(this.errorCantGiftYourself);
            return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
        }

        public Text errorCouldntTakePokemon() {
            var text = Pokegift.miniMessage.deserialize(this.errorCouldntTakePokemon);
            return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
        }

        public Text errorCouldntGivePokemon(ServerPlayerEntity targetPlayer) {
            var text = Pokegift.miniMessage.deserialize(this.errorCouldntGivePokemon,
                    Placeholder.unparsed("player", targetPlayer.getName().getString()));

            return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
        }

        public Text receivedPokemonFeedback(ServerPlayerEntity player, Pokemon pokemon) {
            var text = Pokegift.miniMessage.deserialize(this.receivedPokemonFeedback,
                    Placeholder.unparsed("player", player.getName().getString()),
                    Placeholder.unparsed("pokemon", pokemon.getDisplayName().getString())
            );

            return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(text));
        }
    }
}

package io.github.polymeta.pokegift.commands;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.permission.CobblemonPermission;
import com.cobblemon.mod.common.api.permission.PermissionLevel;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.storage.party.PartyPosition;
import com.cobblemon.mod.common.command.argument.PartySlotArgumentType;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.polymeta.pokegift.Pokegift;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.TimeUnit;

public class Gift {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        var giftCommand = dispatcher.register(
                LiteralArgumentBuilder.<ServerCommandSource>literal("pokegift")
                        .requires(req -> Cobblemon.INSTANCE.getPermissionValidator().hasPermission(req,
                                new CobblemonPermission("pokegift.command.gift.base", PermissionLevel.NONE)))
                        .then(CommandManager.argument("slot", PartySlotArgumentType.Companion.partySlot())
                                .then(CommandManager.argument("player", EntityArgumentType.player())
                                        .then(CommandManager.argument("confirmation", StringArgumentType.greedyString()).executes(ExecuteWithConfirm))
                                        .executes(Execute)))
        );
        dispatcher.register(LiteralArgumentBuilder.<ServerCommandSource>literal("pgift").redirect(giftCommand));
    }

    private static final ConcurrentSkipListSet<UUID> playersOnCooldown = new ConcurrentSkipListSet<>();

    private static final Command<ServerCommandSource> Execute = context -> {
        var slot = PartySlotArgumentType.Companion.getPokemon(context, "slot");
        var player = context.getSource().getPlayerOrThrow();
        var targetPlayer = EntityArgumentType.getPlayer(context, "player");
        if(player.getUuid().equals(targetPlayer.getUuid())) {
            player.sendMessage(Pokegift.config.messages.errorCantGiftYourself());
            return Command.SINGLE_SUCCESS;
        }
        var slotNo = ((PartyPosition)slot.getStoreCoordinates().get().getPosition()).getSlot();
        var canBypass = Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player,
                new CobblemonPermission("pokegift.command.gift.bypass",
                        PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS));
        if(playersOnCooldown.contains(player.getUuid()) && !canBypass && Pokegift.config.cooldownEnabled) {
            player.sendMessage(Pokegift.config.messages.cooldownFeedback());
            return Command.SINGLE_SUCCESS;
        }
        if(isPokemonForbidden(slot) && !canBypass) {
            player.sendMessage(Pokegift.config.messages.pokemonNotAllowed());
            return Command.SINGLE_SUCCESS;
        }
        player.sendMessage(Pokegift.config.messages.pokegiftFeedback(slot, slotNo, targetPlayer));

        return Command.SINGLE_SUCCESS;
    };

    private static final Command<ServerCommandSource> ExecuteWithConfirm = context -> {
        var slot = PartySlotArgumentType.Companion.getPokemon(context, "slot");
        var confirmation = StringArgumentType.getString(context, "confirmation");
        if(!confirmation.trim().equals("--confirm")){
            return Execute.run(context);
        }
        var player = context.getSource().getPlayerOrThrow();
        var targetPlayer = EntityArgumentType.getPlayer(context, "player");
        if(player.getUuid().equals(targetPlayer.getUuid())) {
            player.sendMessage(Pokegift.config.messages.errorCantGiftYourself());
            return Command.SINGLE_SUCCESS;
        }
        var canBypass = Cobblemon.INSTANCE.getPermissionValidator().hasPermission(player,
                new CobblemonPermission("pokegift.command.gift.bypass",
                        PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS));
        if(playersOnCooldown.contains(player.getUuid()) && !canBypass && Pokegift.config.cooldownEnabled) {
            player.sendMessage(Pokegift.config.messages.cooldownFeedback());
            return Command.SINGLE_SUCCESS;
        }
        if(isPokemonForbidden(slot) && !canBypass) {
            player.sendMessage(Pokegift.config.messages.pokemonNotAllowed());
            return Command.SINGLE_SUCCESS;
        }
        var playerParty = Cobblemon.INSTANCE.getStorage().getParty(player);
        var targetParty = Cobblemon.INSTANCE.getStorage().getParty(targetPlayer);
        if(playerParty.remove(slot)) {
            if(!targetParty.add(slot)) {
                player.sendMessage(Pokegift.config.messages.errorCouldntGivePokemon(targetPlayer));
                playerParty.add(slot); //give pokemon back if something went wrong
                return Command.SINGLE_SUCCESS;
            }
        }
        else {
            player.sendMessage(Pokegift.config.messages.errorCouldntTakePokemon());
            return Command.SINGLE_SUCCESS;
        }

        if(Pokegift.config.cooldownEnabled && !canBypass) {
            playersOnCooldown.add(player.getUuid());
            Pokegift.scheduler.schedule(() -> {playersOnCooldown.remove(player.getUuid());}, Pokegift.config.cooldown, TimeUnit.MINUTES);
        }
        player.sendMessage(Pokegift.config.messages.successFeedback());
        targetPlayer.sendMessage(Pokegift.config.messages.receivedPokemonFeedback(player, slot));
        return Command.SINGLE_SUCCESS;
    };

    private static boolean isPokemonForbidden(Pokemon pokemon) {
        for (String property : Pokegift.config.blacklist) {
            if(PokemonProperties.Companion.parse(property, " ", "=").matches(pokemon)) {
                return true;
            }
        }
        return false;
    }

}

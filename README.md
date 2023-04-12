# Pokegift

Gift your Pokémon to other players! Works on both fabric and forge

## Commands & Permissions

| Command                                             | Permission                     |
|-----------------------------------------------------|--------------------------------|
| `/pokegift <slot> <player> [--confirm]` OR `/pgift` | `pokegift.command.gift.base`   |
| Bypass pgift cooldown                               | `pokegift.command.gift.bypass` |

## Config explanation

### General

- `cooldownEnabled` - Whether to enable cool-downs on the pokegift command
- `cooldown` - Cool-down in **MINUTES**. Only used if above value is set to `true`
- `blacklist` - A list of Pokémon properties that can not be gifted, an example entry would be "cobblemon:charmander", but you can even get more complex as we use the Pokémon properties under the hood.

### Message Config

As a preface, this plugin uses [MiniMessage](https://docs.advntr.dev/minimessage/format.html) to parse these messages.
It's a powerful api allowing for various formatting options for you as user.
Refer to the default messages to see what placeholders are allowed where.

- `pokegiftFeedback` - The confirmation question that gets sent to the player when they do /pokegift without confirmation
    - be sure to leave the `<giftconfirm>` tag in as everything in that allows the player to click it to confirm the gift
- `cooldownFeedback` - message that gets sent when the player is on cooldown
- `pokemonNotAllowed` - message that gets sent when a player attempts to gift a forbidden Pokémon
- `successFeedback` - message that get sent on successful gift
- `receivedPokemonFeedback` - message sent to player who received the gift
- `errorCantGiftYourself` - error message that gets displayed when a player attempts to gift to themselves
- `errorCouldntTakePokemon` - generic error message indicating something went wrong whilst taking the Pokémon away from the gifter
- `errorCouldntGivePokemon` - generic error message indicating something went wrong whilst giving the Pokémon to the target player
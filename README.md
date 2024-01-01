# SlashSpec
A Minecraft Spigot plugin to allow players to temporarily fly around in spectator mode on an SMP server while being visible to non-spectators.

Spiritual successor to https://github.com/ohowe1/SpectatorModeRewrite, which is no longer maintained :(

`config.yml` file with comments [here](https://github.com/SeriousGuy888/SlashSpec/blob/main/src/main/resources/config.yml).

## Features

### Main `/spec` command
> Permission Node: `slashspec.use` (granted by default)
> 
> Aliases:
> - `/sp`
> - `/s`

- Temporarily go into spectator mode and fly around freely. Be teleported back when you run the command again.
- Your location, gamemode, flying state, fall damage, air bubbles, fire and freezing ticks will all be remembered, and they will be restored when you come out of spec.
- Other players can see you as a floating head (**requires dependency [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)**) while you are in spectator mode.
  - If the server does not have ProtocolLib installed, other players will see a white particle trail instead.
- Admins can toggle spec mode for other people.
  - Syntax: `/spec force <player> [<in/out>]`
  - Permission: `slashspec.ghost.others`

### Ghost Mode `/spec ghost`
> Permission Node: `slashspec.ghost`
> 
> Syntax: `/spec ghost [<enable/disable>] [<other player> (for admins)]`

- Ghost mode is off by default; other people can see you while you are in spectator mode by default.
- Turn on ghost mode to turn off the floating head feature that makes you visible to other players.
- Admins can toggle ghost mode for other people. Permission: `slashspec.ghost.others`

### Teleport `/spec tp`
> Permission Node: `slashspec.teleport`
> 
> Syntax: `/spec tp <player>`

- Allows you to teleport to another player while you are in spec.
- The other player can disable this, if desired, using `/spec tptoggle`.

### Toggle Teleportation `/spec tptoggle`
> Permission Node: `slashspec.teleport.toggle`
> 
> Syntax: `/spec tptoggle [<enable/disable>]`

- Toggle whether other players are allowed to teleport to you (or another player, for admins).
- If disabled, other people will not be able to use `/spec tp` to teleport to you.
- Admins can toggle this for other people.
  - Syntax: `/spec tptoggle [<enable/disable>] [<player>]`
  - Permission: `slashspec.teleport.toggle.others`

### Fireworks `/spec firework`
> Permission Node: `slashspec.firework`
> 
> Syntax: `/spec firework [<colour>] [<flight duration>/instant] [<other player> (for admins)]`

- Launches a firework at your location. Might be useful for signalling your location.
- You don't have to be in spectator mode to use it (although you do need the permission node)
- Admins can launch fireworks at other people's locations. Permission: `slashspec.firework.others`

### The spectator mode hotbar teleport teleportation menu is disabled by default.
> Permission Node: `slashspec.use_hotbar_teleport_menu` (denied by default)

The spectator mode hotbar teleport menu is a feature that is apparently not that well known.

> If a number key is pressed, the player can teleport to a specific player on that server by pressing `1`, or the player can teleport to a team member by pressing `2` and afterward clicking on the number of the player twice to be teleported or once to see their name.
>
>  _From the [Minecraft Wiki](https://minecraft.wiki/w/Spectator#GUIs)_

However, as players in spectator mode might be able to use it to teleport to whoever they wish, this plugin makes the menu not usable by default. Attempting to teleport using the menu will not work unless the player specifically has the permission node granted. Server operators are not affected by this.

### Combat Tagging

Players will not be able to enter enter spec if they have recently (30 seconds by default, changeable in `config.yml`) been attacked by another player or if they have recently attacked another player. This is useful if players are abusing spec to escape combat situations. This will work for both melee and ranged attacks.

## ProtocolLib Dependency
ProtocolLib is an optional dependency for this plugin, but **_it is required for the floating heads feature to work_**.

If you don't already have it installed, download ProtocolLib from https://www.spigotmc.org/resources/protocollib.1997/ and add the plugin to the server along with SlashSpec.

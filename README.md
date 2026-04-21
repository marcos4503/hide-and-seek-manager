<p align="center" style="font-size: 2px;">
    <img src="This-Repository/mod-cover.png" />
    <br> 
    To download this mod's JAR file, go to the folder corresponding to your Minecraft version, in this Repository, then navigate to the "build/libs" directory. All JAR versions of this mod will be located there.
</p>

# About this Mod

This mod was developed to be the core of modpacks created for playing a Hide'n Seek minigame. As is typical in this game, each player needs to type commands to obtain items, shrink, etc. This mod was created to simplify ALL game management. To play Hide'n Seek with this mod, you must first prepare a world to play in. To prepare the world, follow these steps:

- Make sure the world is already available in your game's world menu.
- Make sure the Commands are active in the world. If they are not, use `NBTExplorer` to activate Commands in the world by editing it directly. Enter the world.
- Find the location where you want the game to take place. In the center of that location, use the commands `/setworldspawn ~ ~ ~` and `/worldborder center ~ ~`. This will make the world's starting point your location.
- Now, set a world border so that the area where players can roam isn't infinite. To do this, use the command `/worldborder set 100`. It's recommended that you use values ​​between 64 and 100, so the playable area won't be too small or too large.
- Turn off the time passage with: `/gamerule doDaylightCycle false`.
- Set a fixed time using: `/time set 3000`. It is recommended to use a time between 1000 and 11500 to ensure it is not nighttime.
- Ensure that Keep Inventory is turned off with: `/gamerule keepInventory false`.
- Turn off Random Tick Speed ​​to prevent the world from evolving, using: `/gamerule randomTickSpeed ​​0`.
- Turn off the Fire Tick using: `/gamerule doFireTick false`.
- Turn off weather cycling using: `/gamerule doWeatherCycle false`.
- Set the default game mode to Adventure. Use: `/defaultgamemode adventure`.
- Build an area of ​​at least 6x6 meters, which will be your base. Place the `Game Totem Head Powered` on this base, similar to the image above. Build it in a way that makes it difficult to access from any angle, so as not to make it too easy for the Hiders. There can only be a maximum of ONE `Game Totem Head Powered` per map.

> [!NOTE]
> If you want the game to generate Adrenaline Injections during the match, simply add at least 2 or more Gold Blocks to the map. These Gold Blocks cannot have anything above them. They will be used to generate the Adrenaline Injections.

Once all the steps are completed, the world will be ready to play Hide'n Seek. To start playing Hide'n Seek, after preparing your world, first open it for LAN so your friends can play with you. At least 2 players are required. Once you're ready to play, simply interact with the `Game Totem Head Powered`. It looks like a Carved Pumpkin!

<img src="This-Repository/totem.png" />
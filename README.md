# Player Finder

[Download from Modrinth](https://modrinth.com/mod/playerfinder)

Player Finder is a Minecraft Mod for Fabric, that allows users to find the coordinates of other players on your server.  

![Demonstration](https://raw.githubusercontent.com/GalvinPython/minecraft-playerfinder/main/.github/assets/java_ptTODXNtAi.png)

**Note:** The "the" in the dimensions have been removed since this screenshot was taken.

## Usage

Use the `/findplayer` command and enter the player's name. There is also a dropdown of players, so you don't have to type it in full and can use autocomplete!

![Usage](https://raw.githubusercontent.com/GalvinPython/minecraft-playerfinder/main/.github/assets/javaw_QXmxvWN79i.png)

## Features

- `/findplayer <name>` command shows the target player’s coordinates
- Coordinates colored based on dimension
- Shows distance in blocks if in the same dimension
- Tab-complete/autocomplete support for player names
- Hover text + click-to-suggest teleport command  
- Optional integration with permissions plugins (e.g. LuckPerms)  

## Compatible Versions

| Mod Version | Game Version(s) | Mod Version Title |
|-------------|-----------------|-------------------|
| 1.0.3       | 1.21.8          | 1.0.3+1.21.8      |
| 1.0.2       | 1.21.8          | 1.0.2+1.21.8      |
| 1.0.1       | 1.21 – 1.21.1   | 1.0.1+1.21        |
| 1.0.0       | 1.21 – 1.21.1   | 1.0.0+1.21        |

## Important Note

This mod is a **server-side** mod! You are **not** required to install this mod on the client-side, unless you are using the "Open to LAN" feature to create a server on your world.

Also: This mod requires the [Fabric API](https://modrinth.com/mod/fabric-api).

PS: *You cannot use this mod on servers that don't use this mod (Player Finder). The commands are created server-side*

## Changelog

### 1.0.3

- Updated to use new Minecraft 1.21.8+ text API (`HoverEvent.ShowText`, `ClickEvent.SuggestCommand`)  
- Fixed compatibility with latest Fabric / Yarn mappings  

### 1.0.2

- Added support for **per-player permissions**  
  - Each lookup requires `playerfinder.find.<playername>` (e.g. `playerfinder.find.steve`)  
  - Lets you allow or deny lookups on a per-user basis with LuckPerms or another permissions provider
- Root command now gated by `playerfinder.find` (fallback OP 2)  
- Removed fallback that previously allowed non-ops to run the command with `.self`

### 1.0.1

- Added colours to the coordinates depending on the dimension
- Updated dimensions to remove "the" in the name
- Added the distance in blocks to the player
- Updated links in the mod metadata

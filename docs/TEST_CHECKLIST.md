# Manual test checklist

Run each item in instance A (reference: NeoForge 26.1.2, Silent Lib 11.2.0, Silent Gear 4.2.3)
and instance B (this port). Note any difference next to the item.

## How to launch

There are two ways to start the game. Both are used, for different jobs.

| | `runClient` (dev launch) | Modrinth App |
|---|---|---|
| Used for | every milestone test of the port | instance A (reference), and the full M11 pass of the port |
| Start it | `.\gradlew.bat :silent-gear-fabric:runClient` in the VS Code terminal | click Play on a profile |
| What it does | builds the code, then starts Minecraft with both mods straight from the build folders | starts a normal Minecraft install with jars in the profile's `mods` folder |
| Account | fake offline player (`Player...`), so `401` and Realms errors in the log are normal | your real account |
| Game folder and log | `silent-gear-fabric/run/`, log at `silent-gear-fabric/run/logs/latest.log` | the profile folder, Modrinth App shows the log |
| Extra mods | Fabric API, Forge Config API Port, Trinkets, Mod Menu are added by Gradle | you add them yourself |

Instance B (the port in Modrinth), when needed: create a Fabric profile for 26.1.2, add Fabric API,
Forge Config API Port, Trinkets Updated and Mod Menu from Modrinth, then copy
`silent-lib-fabric/build/libs/silent-lib-26.1.2-fabric-11.2.0.jar` and
`silent-gear-fabric/build/libs/silent-gear-26.1.2-fabric-4.2.3.jar` into the profile's `mods` folder.
Do not copy the `-sources.jar` files.

## Per-milestone quick checks

### M0 skeleton (passed 2026-10-18 with `runClient`)
- [x] `.\gradlew.bat :silent-gear-fabric:runClient` reaches the title screen
- [x] Log lists `silentgear 4.2.3` and `silentlib 11.2.0` under "Loading N mods"
- [x] Log shows `Silent Lib (Fabric) common init`, `Silent Gear (Fabric) common init, trinkets loaded: true`,
      and both `client init` lines
- [x] No errors from silentgear or silentlib in the log
- [ ] (added after the test) Mods button on the title screen lists "Silent Gear (Fabric)" and "Silent Lib (Fabric)"

### M2 Silent Lib subset (passed 2026-10-18 with `runClient`)
- [x] `runClient` reaches the title screen, no `NoClassDefFoundError` or `ClassNotFoundException` in the log
- [x] Log shows `Silent Lib (Fabric) initialized` and `Silent Lib (Fabric) client initialized`
- [x] Mods button lists both mods (Silent Lib shows a default icon, upstream has none either)
- [x] Create a creative world (cheats on). No errors from silentlib when the world loads
- [x] Type `/sl_tp` and press Tab: the command autocompletes (it is Silent Lib's teleport command)
- [x] `/sl_tp @s minecraft:the_nether 0 80 0` moves you to the Nether, then
      `/sl_tp @s minecraft:overworld 0 100 0` brings you back
- [x] Save and quit: no errors in the log
- Reopen check moved to M4, where starter items first write to the persisted player data

### M4 registries and basic content (to be expanded when M4 is done)
- [ ] New world: you receive the starter blueprint package once. Save, quit, reopen the world:
      you do **not** receive it again (tests Silent Lib's persisted player data)
- [ ] Cutout blocks (flax and fluffy crops, flowers, saplings) render with transparent pixels, not black

## Full pass (M11 and Phase 2)

### Boot and stability
- [ ] Client starts, no errors or warnings from silentgear/silentlib in the log
- [ ] Dedicated server starts and a client can join
- [ ] World save, quit, reload keeps all gear and block entity contents intact

### Content
- [ ] Every item in the creative tab has a texture and a translated name
- [ ] Ores generate in new chunks (overworld, nether, end)
- [ ] Every block places, breaks, and drops correctly

### Data
- [ ] `/reload` with no errors
- [ ] Custom data pack that adds one material and modifies one existing material works
- [ ] Material, part, and trait stats in tooltips match the reference

### Crafting
- [ ] Blueprints and templates craft
- [ ] Parts craft from several materials, including mixed-material parts
- [ ] One of every gear type crafts
- [ ] Vanilla item conversion recipe works
- [ ] Each crafting station block works (inputs, outputs, hopper interaction)

### Behavior
- [ ] Mining speed and correct-tool rules per tier
- [ ] Strip, till, path, shear actions
- [ ] Durability loss, broken state, repair via kit and anvil
- [ ] Armor protection values match the reference
- [ ] Bows, crossbows, slingshot and other ranged gear
- [ ] Sample of traits from every trait effect type
- [ ] Accessory items in Trinkets slots
- [ ] Elytra-style flight item
- [ ] Config change takes effect

### Client
- [ ] Gear rendering per material (inventory, hand, dropped item, armor on player)
- [ ] All GUIs open, render, and handle shift-click
- [ ] Tooltips (normal, shift, ctrl views)
- [ ] Key mappings appear in controls and work
- [ ] Recipe viewer (JEI) shows Silent Gear recipes

### Multiplayer
- [ ] Two clients see the same gear stats and visuals
- [ ] Server-side `/reload` updates clients

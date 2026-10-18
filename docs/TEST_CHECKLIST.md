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

### M3 resources (passed 2026-10-18 with `runClient`)
Data pack files are left out of the build until M4, so this checks the client assets only.
- [x] `runClient` reaches the title screen
- [x] Options > Language: pick another language (for example Deutsch), then switch back. No crash
- [x] Open the existing test world (or create one): it loads, no errors from silentlib or silentgear
      other than the expected item model ones (see below)
- [x] Save and quit to the title screen, then close the game
- Known upstream warnings, same on NeoForge: "Unresolved texture references" for block models
  `alloy_forge`, `salvager`, `gear_smithing_table`, `refabricator`, and "Missing texture references"
  for `gear_smithing_table`, `material_grader`. Compare with instance A when it is set up.
- Expected in the log until M9: errors about 86 `silentgear` item models with unknown
  `silentgear:gear_part`, `silentgear:material` or `silentgear:paint_color` types. Claude filters these.

### M4 registries and basic content (passed 2026-10-18 with `runClient`)
Gear behavior (durability, traits, tool actions) is not wired yet, see PORTING_NOTES "Hooks still to wire".
- [x] `runClient` reaches the title screen (after two startup fixes)
- [x] Log shows `Silent Gear (Fabric) initialized` and `Silent Gear (Fabric) client initialized`
- [x] Create a **new** creative world (old test worlds predate the content). It loads
- [x] Creative inventory has two tabs: "Silent Gear: Main" and "Silent Gear: Gear". Fabric puts modded tabs on
      **page 2**: use the arrow buttons at the top of the creative screen
- [x] Items in both tabs have names and textures (tridents were missing a texture, fixed)
- [x] Place and break a few blocks: an ore, a storage block, netherwood log, a crafting station
      (Gear Smithing Table, Alloy Forge). They drop themselves or their items
- [x] Strip a netherwood log with a vanilla axe
- [x] Open the Alloy Forge GUI (crafting is M7). The Gear Smithing Table has no GUI upstream either; it is a
      decorative block and villager workstation
- [x] Open the Material Grader, Paint Mixer and Salvager GUIs (failed in the first M4 run, fixed)
- [x] Starlight Charger and Material Grader screens show their titles (label color fix)
- [x] New world: you receive the starter blueprint package once. Save, quit, reopen the world:
      you do **not** receive it again (tests Silent Lib's persisted player data)
- [x] Cutout blocks (flax and fluffy crops, flowers, saplings) render with transparent pixels, not black
- [x] Options > Controls > Key Binds, **Inventory** section: "Cycle Back", "Cycle Next", "Display Properties and
      Traits", "Display Construction and Materials", "Display Trait Descriptions", "Open Item" (upstream has no
      Silent Gear section)
- [x] `/reload` runs. Errors about materials, parts or traits are expected until M5 is checked

### M5 data-driven systems
- [x] Counts match the NeoForge reference (2026-10-18): 77 traits, 131 materials (123 standard),
      48 parts (48 compound), 2116 recipes, 1892 advancements, 5 chest loot injections
- [x] `/reload` runs (M4)
- [x] Test data pack `test-packs/sg_port_test` (overrides iron, adds "Test Metal"):
  1. Copy the folder `test-packs/sg_port_test` into the world's `datapacks` folder
     (Fabric dev: `silent-gear-fabric/run/saves/<world>/datapacks/`, Modrinth: the profile's
     `saves/<world>/datapacks/`), then run `/reload`. `/datapack list` shows `file/sg_port_test` enabled
  2. Iron ingot tooltip, holding the "Display Properties and Traits" key (Left Ctrl): durability **999**,
     harvest speed **20** (normally 250 and 6)
  3. Copper block tooltip shows the gold "SGear Material" line, and with Left Ctrl durability **1234**.
     With advanced tooltips on (F3+H) it also shows "Material ID: sgtest:test_metal" and
     "Material data pack: file/sg_port_test". The material name itself is never shown in tooltips
  4. Same results in the NeoForge reference instance
  5. Remove the folder, `/reload` again: iron is back to 250 and 6, Test Metal is gone

### M6 networking and sync (dedicated server)
- [x] One time setup: run `.\gradlew.bat :silent-gear-fabric:runServer` once. It stops and creates
      `silent-gear-fabric/run-server/`. In there, set `eula=true` in `eula.txt` (this accepts the Minecraft
      EULA) and `online-mode=false` in `server.properties` (the dev client has no real account)
- [x] Start `runServer`, then `runClient` in a second terminal, Multiplayer > Direct Connection > `localhost`.
      The server has no window: its terminal is the console (commands without `/`). The dev client is
      named `Dev`, so type `op Dev` there once to use creative mode and `/give`
- [x] Client log shows "Received 77 traits / 131 materials / 48 parts from server"
- [x] Iron ingot tooltip (Left Ctrl) shows iron's stats
- [x] Copy the test pack into `run-server/world/datapacks/`, type `reload` in the server console: the client
      tooltip shows durability 999 without reconnecting

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

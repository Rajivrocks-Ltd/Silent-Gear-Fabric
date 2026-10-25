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

## Automated gametests

From M8 on, many checks run automatically on a headless test server (Fabric's gametest API). The tests are a
small dev-only mod in `silent-gear-fabric/src/gametest` that is never shipped in the jar.

- Run: `.\gradlew.bat :silent-gear-fabric:runGametest` (about 20 seconds). It is not part of `build`.
- Result: the console ends with "All N required tests passed" or lists the failures. Details are in
  `silent-gear-fabric/build/gametest/junit.xml` and `silent-gear-fabric/build/run/gameTest/logs/latest.log`.
- "Optional" tests document known upstream bugs; they may fail without failing the run.
- VS Code: after pulling these changes, run "Java: Clean Java Language Server Workspace" or reload the Gradle
  project, so the editor knows the new `gametest` source folder (until then it shows "not on the classpath").

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

### M7 gear crafting (core passed 2026-10-19; unticked items carried over to the M11 full pass)
Do each step in the Fabric dev client and in the NeoForge reference instance, and note any difference.
Survival-like checks matter here: use a crafting table, not just the creative menu. Gear *behavior*
(durability loss, traits, tool actions) is M8, so only check crafting results and tooltips.
- [x] `runClient` starts (new mixins load). (First run crashed on a wrong mixin target, fixed)
- [x] Craft a blueprint (for example the pickaxe blueprint) from blueprint paper
- [x] Craft a part with the blueprint (for example a pickaxe head from iron): **the blueprint stays in the grid**
- [x] Craft parts from several materials. Mixed materials in one part (e.g. 2 iron + 1 gold) give **no
      result**: upstream's compound part recipe requires all materials to be the same (retested 2026-10-25)
- [x] Craft a full tool (pickaxe head + rod): the tool appears with the right materials in its tooltip
- [x] Put two damaged copies of the same Silent Gear tool in the grid: **no result** (vanilla's combine
      repair must not accept them; it would wipe the parts)
- [x] Craft at least one of each gear type: sword, katana, machete, knife, dagger, spear, trident, mace,
      shield, bow, crossbow, slingshot, arrow, pickaxe, shovel, axe, paxel, hammer, excavator, saw,
      prospector hammer, hoe, mattock, sickle, shears, fishing rod, helmet, chestplate, leggings, boots,
      elytra, ring, bracelet, necklace. Compare stats in the tooltips (Left Ctrl) with the reference.
      (All crafted 2026-10-25, mostly with the quick recipes. The stat comparison with the reference is
      carried over to the M11 full pass)
- [x] A template is used up, a blueprint stays in the grid (2026-10-25)
- [x] Part swap: an iron rod replaces the wooden rod on a diamond pickaxe; a tip and a grip go on a pickaxe
- [x] Ring, bracelet and necklace with a diamond setting
- [x] Vanilla tool alone in the grid converts to a Silent Gear tool. A vanilla leather helmet converts to a
      wearable Silent Gear helmet, and a lining can be added to it (2026-10-25)
- [x] Lined armor taken out of the grid no longer crashes (`RegistryMixin`, 2026-10-25)
- [x] Armor tooltip says "Magic Armor", not "magic_armor" (Port fix, lang entry, 2026-10-25)
- [x] Helmet plates alone in the grid give a wearable helmet (no lining needed) (2026-10-25). The +8 magic
      armor comes from the leather plates (upstream data), not from the lining; a wool lining adds knockback
      resistance and Flexible
- [ ] Each station works and shows its progress arrow while working: Alloy Forge, Crude Mixer, Super Mixer,
      Recrystallizer, Refabricator, Material Grader, Metal Press, Paint Mixer, Salvager, Starlight Charger,
      Stone Anvil
- [ ] A hopper above a station inserts items, a hopper below takes the output
- [ ] Mod kit and blueprint book: put one in a recipe, it stays in the grid
- [x] Crafting a gear item grants the related Silent Gear advancements (check the advancements screen)
- [ ] JEI (dev runtime only): press R on an item to see recipes, U to see uses. Silent Gear adds its own
      pages: gear crafting, Alloy Forge and mixers, Material Grader, Salvager, tool actions. Blueprint and
      part recipes show in the crafting table page

### M8 gear behavior (passed 2026-10-25)
Most of this list is now checked by automated gametests (see "Automated gametests" above), marked "(auto)".
Result of the first full run, 2026-10-25: all required tests pass. What is left for a person is what a server
test cannot see: the client, real movement, and things that need time in the world. Fabric dev client
(Trinkets is loaded there); make the gear in creative or with JEI and hold Left Ctrl to see traits. Compare
with the NeoForge reference when something looks off.
- [x] `runClient` starts and a world loads. (The gametest server already loads every common mixin; this
      checks the client side)
- [x] Durability (auto): a gear pickaxe loses durability when mining, and at its last point it becomes broken
      and stays in the hand. A broken gear axe mines slowly, and so does a broken pickaxe or hammer (Port fix
      2026-10-25; upstream's broken pickaxe kept full speed)
- [x] Broken pickaxe in play: the break sound plays once, and the very next block mines very slowly
      (first try 2026-10-25: slow worked, but the sound played twice with one fast block between; fixed and
      confirmed)
- [x] Durability bar on a gear tool matches the tooltip (client only)
- [x] Repair in an anvil (auto): damaged pickaxe + iron ingots is repaired and costs XP; pickaxe + spoon
      upgrade gets the Spoon trait and mines dirt faster
- [x] Upgrade in the crafting grid: pickaxe + spoon upgrade gives a pickaxe that mines dirt fast (2026-10-25;
      the advanced upgrade base takes diamond shards, which are the `c:nuggets/diamond` item)
- [x] Mining speed (auto): Lustrous (diamond) mines faster in light than in the dark
- [x] XP (auto): Ancient (stone gives level 2) adds 25% per level to block and mob experience; mined ores
      drop experience through the block drops hook
- [x] Bonus drops (auto): Gold Digger (dimerald head, or a tyrian steel tip) gives clearly more gold nuggets
      from nether gold ore than iron
- [x] Magmatic (auto): a pickaxe with a **crimson steel tip** drops iron ingots from iron ore
- [x] Enchantment traits (auto): Silky (brass head, or an amethyst tip) mines glass like Silk Touch
- [x] Mob drops (auto): cows, pigs and sheep drop sinew; zombies and husks leather scraps; spiders fine silk
- [x] Fuel (auto): wood gear (Flammable) is furnace fuel, iron gear is not
- [x] Fuel in a real furnace: a wood gear sword burns and the flame lasts
- [x] Shears (auto): gear shears shear a sheep, cut a leash, collect honeycomb, carve a pumpkin; broken
      shears do not shear
- [x] Axe, shovel, hoe (auto): strip a log, make a path, till dirt; a broken axe does nothing
- [x] Sword sweep (auto): a full strength gear sword attack also hurts the mob next to the target
- [x] Fishing rod (auto): the hook of a cast gear fishing rod stays out
- [x] Fishing rod: you can actually catch a fish (survival, 2026-10-25)
- [x] Armor (auto): a gold gear helmet (Brilliant) keeps piglins calm
- [x] Snow Walker (auto, Port fix 2026-10-25): leather gear boots let the wearer walk on powder snow
      (upstream never did; see PORTING_NOTES)
- [x] Snow Walker in play: walk onto powder snow with leather or wool gear boots, you should not sink
      (2026-10-25). Note: gear leather armor is not in `minecraft:freeze_immune_wearables` (upstream neither), so
      it does not stop freezing like vanilla leather does. Freezing itself works: 7 seconds inside powder snow,
      then damage every 2 seconds (gametest `playerFreezesInPowderSnow`)
- [x] Moonwalker and Bounce (auto): azure silver boots take less fall damage; boots with a **slime lining**
      take none
- [x] Moonwalker and Bounce feel right in play (2026-10-25). Moonwalker boots show "-60% Gravity" twice in
      the tooltip: upstream bug, the modifier is stored twice but applies once (see PORTING_NOTES)
- [x] Swift Swim (auto): a ring with a **prismarine setting** raises the swim speed attribute in the ring slot
- [x] Swift Swim: you really swim faster with that ring (2026-10-25)
- [x] Elytra (auto): a gear elytra can glide from the chest slot and has durability
- [x] Elytra: gliding works in play and costs durability, from the chest slot and from the Back slot
- [x] Trinkets (auto): the player has Ring, Bracelet, Necklace and Back slots; ring, bracelet, necklace and
      elytra each fit their slot (a ring does not fit the necklace slot); using a ring equips it; trinkets
      are kept on death; a gold ring calms piglins
- [x] Trinkets slots show in the inventory screen with their icons (2026-10-25). The "hand" group (Ring, Bracelet) has its
      own extra slot above the offhand slot; the "chest" group (Necklace, Back) opens from the chestplate slot.
      Hover or click each to see the slots inside
- [x] Tooltip (auto): a ring does not say "Requires Trinkets to equip"

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

# Silent Gear Fabric Port: Project Plan

**Goal:** port Silent Gear to Fabric for Minecraft 26.2.

**Strategy (two phases):**

1. **Phase 1, same-version port:** port the latest upstream Silent Gear (4.2.3, NeoForge, Minecraft 26.1.2) to Fabric on the *same* Minecraft version (26.1.2). Only the loader changes, so every bug found is a porting bug, not a version bug.
2. **Phase 2, version bump:** once Phase 1 is validated, move the Fabric port from 26.1.2 to 26.2.

**Working model:** every milestone below ends with a green `./gradlew build` and a manual test checklist in game.
From M8 on, many checks also run automatically as gametests (`runGametest`).

**Status:** Phase 1 is complete (2026-10-25, upstream 4.2.3 on Minecraft 26.1.2, branch `26.1.2`). Phase 2 is on
branch `26.2`.

> Upstream moved from 4.2.1 to 4.2.3 during the port, so 4.2.3 was ported. Older version numbers below are from
> the original research.
>
> Note on "Forge APIs": current Silent Gear is built on **NeoForge**, not Minecraft Forge. The reference APIs we port *from* are NeoForge's. Minecraft Forge is not needed for this project.

---

## 0. Key facts and decisions (verify before starting)

| Item | Value as researched (Oct 2026) | Action |
|---|---|---|
| Upstream mod | Silent Gear 4.2.1, NeoForge, MC 26.1.2 (also 4.2.1 / 4.2.1.1 on 1.21.1) | Port the 26.1.2 build |
| Upstream license | MIT | Keep LICENSE and credit SilentChaos512 |
| Upstream library deps | Silent Lib 11.2.0 (NeoForge 26.1 to 26.1.2), silent-utils, mixbox (shaded) | Silent Lib has **no Fabric version**, we must port it |
| Upstream optional deps | JEI, EMI, Curios, Caelus | Replace with Fabric equivalents (see section 4) |
| Minecraft 26.x | Unobfuscated, ships real names and parameter names | No mappings needed, NeoForge and Fabric code share the same vanilla names |
| Java | 25 for 26.x | Install JDK 25 |
| Fabric Loader | 0.19.3 or newer | Check fabricmc.net/develop |
| Fabric API | 0.155.2+26.1.2 (Phase 1), 0.155.2+26.2 (Phase 2), or newer | Check Modrinth for latest |
| Fabric Loom | New plugin id `net.fabricmc.fabric-loom` (non-remapping line for 26.x) | Deps use `implementation`, not `modImplementation` |

**Decisions to lock in now:**

- [ ] **Mod id:** keep `silentgear` so existing data packs and add-on data stay compatible. Display name e.g. "Silent Gear (Fabric)".
- [ ] **Single-loader repo:** Fabric only, no Architectury or multiloader setup. Simpler to build and to test.
- [ ] **Silent Lib strategy:** port it as a Gradle subproject (`silent-lib-fabric`) inside the same repo, and only port the parts Silent Gear actually uses. This keeps it reusable for other Silent mods later.
- [ ] **Accessory API:** Trinkets Updated (`eu.pb4:trinkets`, has 26.1 and 26.2 builds) or Accessories. Recommendation: Trinkets Updated, since its API is shared across Fabric and NeoForge and it has 26.2 builds.
- [ ] **Recipe viewer:** JEI (Fabric) or EMI. Verify which has a 26.1.2 Fabric build when milestone M8 is reached. Treat as optional.
- [ ] **Courtesy:** let SilentChaos512 know about the port (Discord or GitHub issue). Not required by MIT, but good practice and avoids duplicate effort.

---

## 1. Environment setup

- [ ] Install **JDK 25** (Eclipse Temurin is the usual choice). Set `JAVA_HOME`.
- [ ] Install **IntelliJ IDEA** (Community is fine) plus the **Minecraft Development** plugin.
- [ ] Install **Git**.
- [ ] Install a launcher for manual testing: **Prism Launcher** (recommended, easy side-by-side instances) or the Modrinth App.
- [ ] Create a **GitHub personal access token** with `read:packages`. Only needed to build the upstream NeoForge reference, because Silent Lib and silent-utils are published on GitHub Packages. Put it in `~/.gradle/gradle.properties`:
  ```properties
  gpr.username=YOUR_GITHUB_USERNAME
  gpr.token=YOUR_TOKEN
  ```

---

## 2. Download resources

### 2.1 Upstream source (reference, read-only)

```bash
mkdir -p reference && cd reference

# Silent Gear
git clone https://github.com/SilentChaos512/Silent-Gear.git
cd Silent-Gear
git branch -r            # find the branch for 26.1.x (default branch is 1.21.1)
git tag --list           # find the 4.2.1 tag if one exists
# checkout the branch/tag whose gradle.properties has minecraft_version=26.1.2 and mod_version=4.2.1
cd ..

# Silent Lib (repo name is not hyphenated)
git clone https://github.com/SilentChaos512/SilentLib.git
# checkout the branch matching Silent Lib 11.2.0 / MC 26.1.x

# silent-utils
git clone https://github.com/SilentChaos512/silent-utils.git
```

- [ ] Record the exact commit hashes of all three in `PORTING_NOTES.md`. Every later diff is against these.
- [ ] Build the upstream NeoForge Silent Gear once (`./gradlew build`) to confirm the reference compiles and to get a known-good jar.
- [ ] Check whether `silent-utils` is plain Java with no loader imports. If so, it can be used on Fabric unchanged (as a dependency or via jar-in-jar).

### 2.2 Fabric resources

- [ ] **Fabric template mod** for 26.1.2: generate one at https://fabricmc.net/develop/template/ (gives a correct `build.gradle`, `settings.gradle`, `gradle.properties`, `fabric.mod.json` for 26.x).
- [ ] **Fabric API source** for reference: `git clone https://github.com/FabricMC/fabric.git` and check out the 26.1.2 branch. Useful to read real API signatures.
- [ ] **Fabric documentation:** https://docs.fabricmc.net/develop/ (bookmark: Porting, Mappings, Data Generation, Networking, Rendering).
- [ ] **Fabric API 26.1 porting guide** (lists the Yarn to Mojang renames inside Fabric API itself). Important: many Fabric API class names changed in 26.1, so old tutorials will use outdated names.
- [ ] **"Fabric for Minecraft 26.1" blog post** on fabricmc.net (high-level API changes).

### 2.3 NeoForge resources (to understand what is being replaced)

- [ ] **NeoForge source** at the version upstream uses: `git clone https://github.com/neoforged/NeoForge.git`, check out the matching tag. Needed to see what each NeoForge hook or event actually does before replacing it.
- [ ] **NeoForge docs:** https://docs.neoforged.net/
- [ ] **NeoForge migration primers** (vanilla change summaries per version). Primarily useful in Phase 2 for 26.1 to 26.2 vanilla changes, since they describe vanilla code, not NeoForge code.

### 2.4 Fabric-side dependencies

| Purpose | Library | 26.1.x | 26.2 |
|---|---|---|---|
| Core hooks | Fabric API | 0.155.2+26.1.2 | 0.155.2+26.2 |
| Configs (keeps NeoForge `ModConfigSpec` code nearly unchanged) | Forge Config API Port (Fuzs) | v26.1.5 | v26.2.1 |
| Accessory slots (replaces Curios) | Trinkets Updated (`eu.pb4:trinkets`) | 4.0.0-beta.2+26.1 | 4.1.0-beta.1+26.2 |
| Elytra-style flight (replaces Caelus) | Fabric API entity events (custom elytra) | built in | built in |
| Recipe viewer (optional) | JEI Fabric or EMI | verify | verify |
| Config screen in menu (optional) | Mod Menu | verify | verify |
| Color mixing | mixbox 2.0.0 | jar-in-jar via Loom `include` | same |

### 2.5 Manual test instances (Prism Launcher)

- [ ] **Instance A, reference:** NeoForge 26.1.2 + Silent Lib 11.2.0 + Silent Gear 4.2.1 (from Modrinth/CurseForge). This is the "how it should behave" baseline.
- [ ] **Instance B, port:** Fabric 26.1.2 + Fabric API + Forge Config API Port + Trinkets + the built port jars.
- [ ] Optional **dedicated server** for each, to catch client/server sync bugs.

---

## 3. Repository setup

```
silent-gear-fabric/
  reference/                 # upstream clones, git-ignored, read-only
  silent-lib-fabric/         # Gradle subproject: ported Silent Lib subset
  silent-gear-fabric/        # Gradle subproject: the port
  docs/
    PORTING_NOTES.md         # running log: decisions, upstream commits, known gaps
    NEOFORGE_API_INVENTORY.md
    API_MAPPING.md           # NeoForge -> Fabric lookup table (section 4)
    TEST_CHECKLIST.md        # manual test cases (section 7)
  settings.gradle
  build.gradle
  gradle.properties
```

- [ ] Create the repo, add `reference/` to `.gitignore`.
- [ ] Start from the Fabric template build files, then turn it into a two-subproject build.
- [ ] Copy upstream `LICENSE` and add a credits section to the README.
- [ ] Branching: one long-lived branch per Minecraft version (`26.1.2`, `26.2`), like upstream. Work happens on short
  branches (one per milestone or fix) that merge into their version branch after the manual test passes. `main`
  points to the newest version.

---

## 4. NeoForge to Fabric API mapping (seed for `docs/API_MAPPING.md`)

Fabric API module names below are the pre-26.1 names. Some modules and classes were renamed to Mojang-style names in 26.1, check the Fabric API 26.1 porting guide for the current names.

| NeoForge concept | Fabric replacement | Notes |
|---|---|---|
| `@Mod` class, `neoforge.mods.toml` | `ModInitializer`, `ClientModInitializer`, `fabric.mod.json` | Split common and client init explicitly |
| Mod event bus / `RegisterEvent` / `DeferredRegister` | Direct `Registry.register(...)` during `onInitialize` | Keep a small helper that mimics `DeferredHolder` to limit code churn |
| Custom registries (`RegistryBuilder`) | `FabricRegistryBuilder` | Silent Gear registers gear types, part types, trait effect types, etc. |
| Datapack registries | Fabric dynamic registries API | Only if upstream uses them |
| Game event bus (`NeoForge.EVENT_BUS`) | Fabric callbacks (lifecycle, player block break, living entity damage, tooltip, etc.) | Where no callback exists, use a **mixin** |
| `PlayerEvent.BreakSpeed`, `HarvestCheck` | Mixin into `Player` destroy speed / correct-tool checks | Vanilla `Tool` data component covers much of this in 26.x |
| `AnvilUpdateEvent` | Mixin into `AnvilMenu` | Needed for gear repair in anvils |
| `IItemExtension` overrides on gear items | `FabricItem` interface where available, otherwise mixins | Inventory step will list every override used |
| `ItemAbility` (tool actions: strip, till, path, shear) | Implement `useOn` behavior directly, plus conventional tags | No direct Fabric equivalent |
| Data attachments | Fabric Data Attachment API | Close 1:1 mapping |
| Capabilities / `IItemHandler` on block entities | Vanilla `Container`/`WorldlyContainer` plus Fabric Transfer API | Fabric wraps vanilla containers automatically |
| Payload registration (`RegisterPayloadHandlersEvent`) | Fabric Networking API (payload type registry + server/client play networking) | Reuse the same `CustomPacketPayload` records and `StreamCodec`s |
| Configuration-phase sync tasks | Fabric server configuration networking | Relevant if materials sync during login |
| `ModConfigSpec` configs | Forge Config API Port | Code stays almost identical |
| Global loot modifiers | Fabric loot API (modify loot tables / drops events) | Rewrite JSON modifiers as code |
| Biome modifiers (ore worldgen JSON) | Fabric biome modification API | Rewrite as code |
| `neoforge:conditions` in JSON | `fabric:load_conditions` (resource conditions API) | Patch JSON files |
| NeoForge custom ingredient types | Fabric custom ingredient API | Patch JSON files |
| Data maps | Custom reload listener or code | Only if upstream uses them |
| Reload listeners | Fabric resource loader API | Material/part/trait loading lives here |
| Datagen (`GatherDataEvent`) | Fabric data generation API | Can be deferred, see M9 |
| Custom geometry / baked models (dynamic gear models) | Fabric model loading API plus Fabric rendering API | **Highest risk area** |
| Item tints, tooltip components | Fabric rendering / tooltip component callbacks | |
| Key mappings | Fabric key binding API | |
| Menus with extra open data | Fabric extended screen handler (menu) type | |
| Creative tabs | Fabric item group API | |
| Curios integration | Trinkets Updated | |
| Caelus (elytra flight from gear) | Fabric custom elytra entity event | |
| Access transformers | Access widener (class tweaker) | Translate entries 1:1 |
| Game tests | Fabric GameTest API | Optional |
| `c:` conventional tags | Fabric convention tags v2 | Both loaders use `c:`, mostly compatible |

---

## 5. Phase 1: port Silent Gear 4.2.3 to Fabric 26.1.2

Each milestone: implement and get `./gradlew build` green, then run `./gradlew runClient` (and `runServer` where noted) and work through the checklist. Commit only after the test passes.

### M0. Skeleton boots
- **Work:** set up the two-subproject Gradle build from the Fabric template, Java 25, Loom (non-remapping), Fabric API, Forge Config API Port, Trinkets, mixbox via jar-in-jar. Empty initializers that log a message.
- **Test:** game launches to title screen, mod appears in the mod list, log shows the init message, no errors.

### M1. Inventory and triage (no code changes)
- **Work:** scan the reference repos and produce `docs/NEOFORGE_API_INVENTORY.md`:
  - every `net.neoforged.*` import in Silent Gear and Silent Lib, grouped by category from section 4, with file counts
  - every `IItemExtension` / `IBlockExtension` method overridden
  - every event subscribed to
  - every Silent Lib class Silent Gear uses (this defines the Silent Lib port scope)
  - every resource file containing `neoforge:` keys (conditions, loot modifiers, biome modifiers, data maps, ingredients)
  - access transformer entries and existing mixins
- **Review:** sanity-check the list, confirm Silent Lib scope, update `API_MAPPING.md` with anything new.

### M2. Silent Lib subset
- **Work:** port only the Silent Lib classes listed in M1 into `silent-lib-fabric`. Replace its NeoForge registration, networking, and config code using section 4.
- **Test:** game boots with both mods, no missing class errors.

### M3. Resources copied as-is, then patched
Big shortcut: most of Silent Gear's content is JSON (materials, parts, traits, recipes, models, lang) and is loader-agnostic.
- **Work:** copy `src/main/resources` and `src/generated/resources` from upstream. Replace `neoforge.mods.toml` with `fabric.mod.json`. Convert every `neoforge:` construct found in M1 (conditions, ingredients, loot modifiers, biome modifiers) to Fabric equivalents or note it for code in later milestones.
- **Test:** boots, no resource parse errors in the log.

### M4. Registries and basic content
- **Work:** port registration of blocks, items, block entities, menus, recipe types and serializers, custom registries, creative tabs, sounds, data components.
- **Test:** creative tab is populated, all items have names and textures (static ones at least), blocks place and break, no missing-registry errors.

### M5. Data-driven systems
- **Work:** port reload listeners for materials, parts, traits, gear types, plus the trait/effect type registries.
- **Test:** `/reload` works without errors, material count in log matches the NeoForge reference instance, a custom test data pack overriding one material is picked up.

### M6. Networking and sync
- **Work:** port all payloads and the material/part/trait sync to clients on join and after `/reload`.
- **Test:** with `runServer` + client, join the server, tooltips show correct material stats on the client, `/reload` on the server updates client tooltips.

### M7. Gear crafting
- **Work:** blueprints, templates, part crafting, gear assembly recipes, vanilla-to-gear conversion recipes, repair kits, crafting station blocks and their menus/screens, block entity inventories.
- **Test:** craft a blueprint, parts from several materials, and at least one of every gear type (tool, weapon, armor, bow/crossbow, curio-type item). Use each crafting block. Compare outputs against reference instance A.

### M8. Gear behavior
- **Work:** mining speed and harvest levels, tool actions (strip, till, path, shear), durability and broken-gear behavior, repair in crafting grid and anvil, traits (each trait effect type), armor stats, ranged weapons, elytra-style flight, Trinkets integration, configs, optional recipe viewer plugin.
- **Test:** break blocks of every tier with matching and non-matching tools, use each tool action, take damage with armor, break and repair gear, trigger a sample of traits from each trait effect type, equip an accessory item in a Trinkets slot, edit a config value and confirm it applies.

### M9. Client rendering (highest risk, budget extra time)
- **Work:** dynamic layered gear models, material-based tinting, broken-gear model state, armor rendering, custom tooltip components, screens and key mappings.
- **Test:** gear made from different materials looks different and matches reference instance A, inventory and hand rendering, armor on player, tooltips (normal and expanded), all GUIs.

### M10. Datagen (optional for release)
- **Work:** port data generators to Fabric's data generation API, run datagen, diff the output against the copied `src/generated/resources`.
- **Review:** the diff should be empty or only expected Fabric/NeoForge format differences. Until this is done, keep using the copied generated resources.

### M11. Phase 1 full validation
- **Test:** the full `docs/TEST_CHECKLIST.md` (section 7) in singleplayer and on a dedicated server, side by side with reference instance A.
- **Exit criteria:** no crashes, no errors in logs during the test pass, behavior matches the reference for every checklist item, or deviations are documented in `PORTING_NOTES.md` as known gaps.
- Tag the release `4.2.3+26.1.2` (mod version, then Minecraft version).

---

## 6. Phase 2: Fabric 26.1.2 to 26.2

- [ ] **Check upstream first:** see if SilentChaos512 has published a NeoForge 26.2 version. If yes, diff upstream 26.1.2 against 26.2 and replay the relevant game-logic changes on the Fabric port.
- [ ] **Read change summaries:** Fabric's 26.2 blog post and docs, the NeoForge 26.1 to 26.2 migration primer (vanilla changes), Minecraft Wiki "Java Edition 26.2".
- [ ] **Work:** bump Minecraft, Fabric Loader, Loom, Fabric API (0.155.2+26.2 or newer), Forge Config API Port (v26.2.x), Trinkets (4.1.x+26.2), recipe viewer. Fix compile errors, one area at a time.
- [ ] **Known 26.2 change areas from other mods' ports:** screen management (for example `Minecraft.setScreen` was renamed to `setScreenAndShow`), some GUI/HUD and rendering internals moved. Expect most work in client code (M9 areas).
- [ ] **Test:** rerun the full `TEST_CHECKLIST.md` on 26.2, singleplayer and dedicated server. Also load a world created in the 26.1.2 port to confirm gear items and block entities survive the upgrade.
- [ ] Tag `4.2.3+26.2` (or the matching upstream version).

---

## 7. Manual test checklist (seed for `docs/TEST_CHECKLIST.md`)

Run each item in instance A (reference) and instance B (port), note any difference.

**Boot and stability**
- [ ] Client starts, no errors or warnings from silentgear/silentlib in the log
- [ ] Dedicated server starts and a client can join
- [ ] World save, quit, reload keeps all gear and block entity contents intact

**Content**
- [ ] Every item in the creative tab has a texture and a translated name
- [ ] Ores generate in new chunks (check each dimension upstream uses)
- [ ] Every block places, breaks, and drops correctly

**Data**
- [ ] `/reload` with no errors
- [ ] Custom data pack that adds one material and modifies one existing material works
- [ ] Material, part, and trait stats in tooltips match the reference

**Crafting**
- [ ] Blueprints and templates craft
- [ ] Parts craft from several materials, including mixed-material parts
- [ ] One of every gear type crafts
- [ ] Vanilla item conversion recipe works
- [ ] Each crafting station block works (inputs, outputs, hopper interaction)

**Behavior**
- [ ] Mining speed and correct-tool rules per tier
- [ ] Strip, till, path, shear actions
- [ ] Durability loss, broken state, repair via kit and anvil
- [ ] Armor protection values match the reference
- [ ] Bows/crossbows/ranged gear
- [ ] Sample of traits from every trait effect type
- [ ] Accessory items in Trinkets slots
- [ ] Elytra-style flight item (if present upstream)
- [ ] Config change takes effect

**Client**
- [ ] Gear rendering per material (inventory, hand, dropped item, armor on player)
- [ ] All GUIs open, render, and handle shift-click
- [ ] Tooltips (normal, shift, ctrl views if upstream has them)
- [ ] Key mappings appear in controls and work
- [ ] Recipe viewer shows Silent Gear recipes (if integrated)

**Multiplayer**
- [ ] Two clients see the same gear stats and visuals
- [ ] Server-side `/reload` updates clients

---

## 8. Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Dynamic gear model/rendering system | High, most loader-specific code | Do it last (M9), study Fabric model loading API early, prototype one gear type first |
| Silent Lib has no Fabric version | Medium | Port only the used subset (M1 defines scope) |
| `IItemExtension` hooks with no Fabric equivalent | Medium | Mixins, kept small and listed in `PORTING_NOTES.md` |
| Fabric API class renames in 26.1 | Low/medium, confuses older tutorials | Always check the 26.1 porting guide and the cloned Fabric API source |
| Trinkets Updated is beta on 26.x | Low/medium | Keep integration behind a soft dependency check so the mod runs without it |
| Upstream changes during the port | Low | Pin upstream commits in `PORTING_NOTES.md`, sync deliberately after Phase 1 |
| Large refactors drifting from upstream logic | Medium | Rule: port loader glue only, never rewrite game logic |

---

## 9. Release

- [ ] `fabric.mod.json`: dependencies on Fabric API, Forge Config API Port, Silent Lib Fabric; suggests Trinkets and recipe viewer.
- [ ] README with credits to SilentChaos512, MIT license, link to upstream, clear "unofficial port" wording.
- [ ] Publish to Modrinth (and CurseForge if wanted), marking it as an unofficial Fabric port.
- [ ] Decide on a sync policy for future upstream releases.

---

## Reference links

- Silent Gear: https://github.com/SilentChaos512/Silent-Gear
- Silent Lib: https://github.com/SilentChaos512/SilentLib
- silent-utils: https://github.com/SilentChaos512/silent-utils
- Fabric docs: https://docs.fabricmc.net/develop/
- Fabric porting guide: https://docs.fabricmc.net/develop/porting/
- Fabric mappings migration (26.x): https://docs.fabricmc.net/develop/porting/mappings/
- Fabric template generator: https://fabricmc.net/develop/template/
- Fabric API source: https://github.com/FabricMC/fabric
- NeoForge source: https://github.com/neoforged/NeoForge
- NeoForge docs: https://docs.neoforged.net/
- Forge Config API Port: https://www.curseforge.com/minecraft/mc-mods/forge-config-api-port
- Trinkets Updated: https://github.com/Patbox/trinkets

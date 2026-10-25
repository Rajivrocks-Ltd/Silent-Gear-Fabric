1. Never write documents with m-dashes e.g. "--". Use simple, but clear language.
2. Document code to the set standards of whatever language we are using. For this project it will mostly be Java.

# Silent Gear Fabric port

## Context
- Porting Silent Gear 4.2.3 (NeoForge, MC 26.1.2) to Fabric. Upstream source is in reference/ (read-only, never edit).
- The full plan is SILENT_GEAR_FABRIC_PORT_PLAN.md. Work one milestone at a time.
- Minecraft 26.x is unobfuscated. Vanilla class/method names are identical between NeoForge and Fabric.
- Lookup tables: docs/API_MAPPING.md, docs/NEOFORGE_API_INVENTORY.md. Log decisions in docs/PORTING_NOTES.md.
- Fabric API source for 26.1.2 is in reference/fabric. Check real signatures there, do not rely on old tutorials (many Fabric API names changed in 26.1).
- NeoForge 26.1.2.95 sources are in reference/neoforge-src, to see what a NeoForge hook really does.
- The user codes in VS Code on Windows and tests with the Modrinth App plus `runClient`.
- Since M10, `silent-gear-fabric/src/generated/resources` is written by `runDatagen` (the ported data generators). Change the generators, not those JSON files.

## Rules
- Port loader-specific glue only. Do not change game logic, balance, or data formats.
- No net.neoforged imports may remain in silent-gear-fabric or silent-lib-fabric, except the classes that Forge Config API Port itself provides (ModConfigSpec, ModConfig, ConfigurationScreen).
- Keep package names and class names from upstream where possible so diffs against upstream stay readable.
- Prefer Fabric API over mixins. When a mixin is needed, keep it minimal and list it in PORTING_NOTES.md.
- Dependencies use `implementation` (non-remapping Loom for 26.x).
- Finish every task with `.\gradlew.bat build` passing (JAVA_HOME must point to JDK 25). Do not run the game interactively (`runClient`); the user tests by hand. Automated gametests (headless Gradle tasks) are allowed (user approved 2026-10-25).
- At the end of each task, write a short summary: files changed, mixins added, anything not ported, and what the user should test.

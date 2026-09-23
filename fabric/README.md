# Origins: Wildfire's Gender Compat — Fabric 1.21.1

Compatibility mod between:

- [Origins](https://modrinth.com/mod/origins) 1.13.0-pre.3+mc.1.21.1 (Apace)
- [Pehkui](https://modrinth.com/mod/pehkui) 3.8.3 (Virtuoel)
- [Wildfire's Female Gender Mod](https://modrinth.com/mod/female-gender) 3.2.1+1.21 (WildfireRomeo)

It adds a "Gender" origin layer with two selectable origins:

- **Female** — slightly smaller scale (Pehkui height 0.93), +agility, lower attack damage, more taken damage, less exhaustion, Wildfire female voice/bust synced.
- **Male** — slightly larger scale (Pehkui height 1.05), slower movement speed, more attack damage, less taken damage, more exhaustion, mining haste, Wildfire male voice synced.

Compatibility is done via public APIs and datapack JSONs only. No code was copied from the mods above. See `../CREDITS.txt`.

## Install

Requires Minecraft 1.21.1, Fabric Loader 0.19.x and Fabric API 0.116.17+1.21.1.

Copy `originswildfirecompat-fabric-1.21.1-1.0.0.jar` into the `mods` folder, then **manually** add:

- `Origins-1.13.0-pre.3+mc.1.21.1.jar`
- `Pehkui-3.8.3+1.14.4-1.21.jar`
- `wildfire_gender` 3.2.1+1.21

Dependencies are **not** downloaded automatically. Fabric only *requires* them via `depends` in `fabric.mod.json` and refuses to boot if any is missing.

## Build

From this folder (Java 21):

```sh
../gradlew build
```

Output: `build/libs/originswildfirecompat-fabric-1.21.1-1.0.0.jar`

## Known issue: crash with the Async mod

**This version crashes if the `async` mod (`async-fabric`) is installed.** The game opens and the Gender origin layer loads correctly, but opening a world crashes with:

```text
java.lang.NullPointerException: Ticking player
    at net.minecraft.class_3222.method_14226 (ServerPlayerEntity.playerTick)
```

The log also shows repeated `Error in async entity tick` errors — `ArrayIndexOutOfBoundsException:
Index -1 out of bounds for length 65` thrown from `Apoli InventoryUtil.getAllSlots`, ticked in
parallel by `com.axalotl.async` (`async$overwriteEntityTicking`).

Root cause: an incompatibility between the **alpha `async` mod and Origins/Apoli**. Async ticks
entities on worker threads, which races with Apoli's thread-unsafe player-inventory iteration and
corrupts player state, killing the vanilla `playerTick`. It is **not** caused by this mod:

- no `originswildfirecompat` frame appears anywhere in the crash stack;
- the crash happens while the origin-selection screen is still open, before Female/Male is chosen,
  so none of this mod's powers are even active at crash time.

Workaround: remove `async-fabric-*.jar` from the `mods` folder. Verified: without Async the world
opens, Female/Male are selectable, scale/attributes sync, and the log is clean of errors.

# Plus Ultra — a My Hero Academia fan mod (NeoForge 1.21.1)

Unofficial fan project. *My Hero Academia* belongs to Kohei Horikoshi / Shueisha.
All code, textures and data in this project are original; nothing is taken from Clover Craft or any other mod.

## Features

| System | What it does |
|---|---|
| **Quirks** | 6 Quirks (One For All, Half-Cold Half-Hot, Explosion, Hardening, Zero Gravity, Engine), 3 abilities each |
| **Stamina** | Resource bar with regen, used by every ability (free in creative and in the Training Ground) |
| **Levels / Plus Ultra** | Using abilities earns XP. Level scales damage and max stamina. The 3rd ability unlocks at level 5 (configurable) |
| **Mission Board** | Right-click for a Patrol (kill 10 hostiles) or Villain mission. Sneak + right-click abandons it |
| **Villain boss** | Tough melee boss with boss bar and a periodic Decay shockwave. Drops emeralds and sometimes a Quirk Orb |
| **Hero ranks** | Missions give hero points: Trainee → Rookie → Pro → Top 10 → Number One. Hero License item shows your rank |
| **Hero Agencies** | `/agency create|join|leave|info|list`. Members within 16 blocks get +20% stamina regen each (max +60%) |
| **Training Ground** | `/training` / `/training leave`. Flat, always-daytime dimension; abilities cost no stamina |
| **Items** | Quirk Orb (awakens a random Quirk), Quirk Eraser (removes it), Hero License, Mission Board, Villain Spawn Egg |

Keys (rebindable): **Z / X / C** = abilities 1 / 2 / 3.

Admin commands: `/plusultra quirk set <player> <id>`, `/plusultra quirk clear <player>`, `/plusultra level set <player> <n>`, `/plusultra info`.

Config: `config/plusultra-common.toml` (damage multiplier, stamina regen, max level, Plus Ultra unlock level,
whether quirks can hurt players, whether Explosion abilities break blocks).

## Building

Requires JDK 21 and Gradle 8.8+ (the Gradle wrapper is not included).

    gradle wrapper        # once, to generate ./gradlew
    ./gradlew runClient   # test in a dev client
    ./gradlew build       # jar ends up in build/libs/

Versions are in `gradle.properties` (`neo_version`) and `build.gradle` (ModDevGradle plugin version).
Bump them if Gradle can't resolve them.

## Status

Written without access to Minecraft/NeoForge jars, so it has **not been compiled or run yet**. Expect to fix a few
small API mismatches on the first build. Likely spots: `SoundEvents` constants that are `Holder`s rather than
`SoundEvent`s in your NeoForge version, `AttachmentType` builder overloads, and `RegisterGuiLayersEvent` naming.

## Project layout

    core/      registries (items, blocks, entities, attachments, tabs, dimension key) + server events
    quirk/     Quirk enum, per-player data, ability effects, ability manager (cost / cooldown / XP)
    mission/   mission board block, mission logic, ranks
    agency/    saved-data Hero Agencies
    entity/    Villain boss
    item/      Quirk Orb, Quirk Eraser, Hero License
    net/       payloads: UseAbility (client→server), Sync (server→client)
    client/    keybinds, HUD, villain renderer
    command/   /plusultra, /agency, /training

## Extending

- **New Quirk**: add an enum constant in `Quirk`, write its three ability methods in `Abilities`, add lang keys
  `quirk.plusultra.<id>` and `ability.plusultra.<id>.<ability>`.
- **Better visuals/animations**: swap the vanilla humanoid renderer for GeckoLib models; add Photon/particle effects.
- **New mission types**: add a constant and a case in `Missions.assign / onKill / describe`.

## v0.2 additions: talents, fusion, visuals

Keys (rebindable): Z/X/C abilities, **V** talent tree, **G** swap active Quirk, **B** Fusion Strike.

- **Skill points / talent tree**: every level-up gives 1 point. Spend on Power (+8% damage), Endurance (+10 stamina),
  Recovery (+10% regen), Focus (-5% cooldowns), Efficiency (-5% stamina cost). Respec is free in the screen.
- **Quirk fusion**: reach level 8 (config `fusionUnlockLevel`), craft a Fusion Catalyst
  (amethyst shards, diamonds, nether star + Quirk Orb) and use it to gain a second random Quirk.
  G swaps which one is on Z/X/C (each keeps its own cooldowns). B fires both Quirks' first abilities together
  at 1.3x power (needs Plus Ultra level). A Quirk Eraser removes the fused Quirk first, then the main one.
  Villains drop a Fusion Catalyst 8% of the time.
- **Visuals**: ring/beam/spiral/sphere particle effects for every ability, glowing auras on buffs, level-up burst,
  new HUD (cooldown-fill ability rows, stamina numbers, fusion lines), new villain model (horns, spiked pads, cloak,
  glowing eyes, rage phase below 50% health), redrawn item/block textures.
- Admin: `/plusultra quirk fuse <player> <quirk>`; `/plusultra level set` now also resets talents and grants points.

Most likely compile fixes if any: `DustParticleOptions(Vector3f, float)` in Fx.java (1.21.1 takes a Vector3f),
`EyesLayer.renderType()` in VillainEyesLayer.java.

## v0.3 additions: more Quirks, support items, villains, raids

- **8 new Quirks** (3 abilities each): Creation, Invisibility, Fierce Wings, Dark Shadow, Black Whip, Acid,
  Earphone Jack, Tape. They roll from Quirk Orbs automatically. (`/plusultra quirk set <player> <id>`: creation,
  invisibility, fierce_wings, dark_shadow, black_whip, acid, earphone_jack, tape.)
- **Support items**: Grappling Hook (right-click, pulls you to the block you look at, 250 uses), Support Gloves
  (right-click impact punch, works without a Quirk), Stamina Potion (+60 stamina) and Greater Stamina Potion
  (full stamina and cooldown reset), Hero Costume (helmet, jacket, pants, boots: +5% stamina regen per piece,
  +10% Quirk damage with the full set).
- **Villains**: Decay Lord (shockwave), Nomu (leap slam, regenerates when enraged, large), Hero Killer (blinks behind
  you and paralyses), Blue Flame (blue fire cone), Alliance Thug (minion). All have spawn eggs and loot tables.
  Villain missions now pick a random boss.
- **Villain Alliance raids**: 25% of Mission Board rolls start a 3-wave raid (thugs, thugs + Hero Killer,
  boss + escorts). Fails if you die, leave the area or run out of 12 minutes. Admin:
  `/plusultra raid start|stop <player>`.
- **Textures**: detailed 64x64 skins for every villain with glowing eyes, new armor layers and item icons.

Most likely compile fixes if any: `Item#getUseDuration(ItemStack, LivingEntity)` in StaminaPotionItem,
`ArmorMaterial` constructor in ModArmor, `LivingEntityRenderer#scale` in VillainRenderer.

## v0.4 additions: real animations

Every ability now plays a **timed client-side animation** instead of a one-tick particle dump.
The server sends one small `AnimPayload` per ability; the client plays it over several ticks.
This is smoother, uses far less bandwidth, and means other players see the effects too.

| Quirk | Animations |
|---|---|
| One For All | Green lightning arcs along the arm into a punch with air blast, ground ring and cracks; lightning-wrapped leap; crackling Full Cowl aura |
| Half-Cold Half-Hot | Fire jet that follows your aim; ice spikes erupting in a widening V; half-fire / half-ice nova; split fire/ice aura |
| Explosion | Palm sparks; staged MHA explosion (white flash, swelling fireball, crackle, shock ring, debris, black smoke, camera shake and screen flash); rocket-blast trail; Howitzer Impact spiral wind-up, then the blast |
| Hardening | Red steel shards, steel aura, lightning shock |
| Zero Gravity | Gravity bubble with bubbles rising off targets; meteors streaking down with trails and impacts |
| Engine | Exhaust and speed lines, blue lightning aura, kick shock rings |
| Creation | Particles converge to forge the shield and golems (golems now appear when the forging finishes); cannon bolt with iron-chip impact |
| Invisibility | Shimmer, light streak between you and the target, twinkling dome |
| Fierce Wings | Flapping wings of glowing bones and feather tips; feather volley where each feather flies to a target; wind gust; updraft |
| Dark Shadow | Tendril lunges with red eyes; shadow columns erupting from the ground |
| Black Whip | Whip flies out, wraps or snaps and retracts, dark tendril look; anchor swings |
| Acid | Arcing acid glob; bubbling puddle that pulses with each damage tick |
| Earphone Jack | Rings of sound travelling forward with notes; ground ripples and bass-drop shake |
| Tape | Tape strand flies from your hand and **wraps up the target in a spiral**, cocoon for Tape Cocoon, taut rope for swings |

### How it works

    quirk/FxType         every animation type (ordinal goes over the wire)
    quirk/Fx.play(...)   server: Fx.play(FxType.X).src(p).a(vec).rgb(0x...).send(p)
    net/AnimPayload      the packet
    client/anim/         ClientAnims (manager, camera shake, screen flash), AnimFactory,
                         AnimsCore / AnimsFire / AnimsMisc (the animations), P (particle helpers),
                         FxParticle (fully controllable particle)
    assets/.../particles + textures/particle   glow, spark, feather, tape, puff sprites

To add an animation: add an `FxType`, write an `Anim` subclass (`step()` runs once per tick and returns
true when done), add one line in `AnimFactory`, and call it from the ability with `Fx.play(...)`.

Gameplay (damage, cooldowns, effects, movement) is unchanged, except that the Howitzer's damage and the
Iron Guardians' spawn are delayed 8 ticks so they land with the animation.
The old server-side aura particles were replaced by client auras.

### Likely compile fixes (this version has not been built yet)

* `FxParticle` - protected field names (`quadSize`, `roll`, `alpha`) or `getLightColor` access
* `ClientModEvents.particles` - the `registerSpriteSet` lambda shape
* `ClientGameEvents.onCamera` - `ViewportEvent.ComputeCameraAngles` setter names
* `P.dust` / `P.vanilla` - `ClientLevel.addParticle` overload

## v0.5 additions: borrowed art and sound (personal use)

* **27 ability sounds** (punch, thunder, lightning, fire, ice, wings, whip, sound waves, ...) wired to the matching
  abilities through `core/ModSounds`.
* **Ability icons** in the HUD: every ability row shows its icon with a cooldown sweep, dark overlay when locked,
  plus fusion and swap icons.
* **Villain skins** are now HD character skins (Decay Lord = Shigaraki, Blue Flame = Dabi, Nomu = Muscular,
  Hero Killer = Kurogiri, Thug = Compress). The glowing-eye overlay is blank for them, because it was drawn for the
  old skins and would not line up with the new faces. The horn/cape/pad model parts read an empty part of the
  texture, so they are invisible on these skins (checked).

These assets come from another mod and are **not licensed for sharing**. See `CREDITS-ASSETS.md`.

# Borrowed assets (PERSONAL USE ONLY)

This build contains art and sound taken from **My Hero Craft** (jar `My-Hero-Craft-32_0_2.jar`, mod id `mhc`,
credited in its metadata to *kujira* and *Tommy*). That mod is marked **All Rights Reserved**.

They are included here for one person's private game only. **Do not upload, share or publish this build**
(GitHub repos that are public, CurseForge, Modrinth, Discord, etc.). Before sharing the mod, delete the files
listed below, or get permission from the original authors.

| What | Where in this project | Original |
|---|---|---|
| 27 ability sound effects | `assets/plusultra/sounds/*.ogg`, `sounds.json` | `assets/mhc/sounds/` |
| 42 ability icons + fusion + swap icon | `assets/plusultra/textures/gui/ability/*.png` | `assets/mhc/textures/gui/icon/kosei_*.png` |
| 5 villain skins | `assets/plusultra/textures/entity/{villain,blue_flame,nomu,hero_killer,thug}.png` | `assets/mhc/textures/villan/` (`rengo_shigaraki`, `rengo_dabi`, `rengo_muscular`, `rengo_kurogiri`, `rengo_compress`) |

## To make the build shareable again

1. Delete the three groups above.
2. Delete the sound names from `core/ModSounds.java` and restore vanilla `SoundEvents.*` in the abilities (or
   generate new sounds).
3. Remove the icon drawing from `client/QuirkHud.java` (the `icon(...)` calls) or draw your own icons.
4. Restore villain skins (the v0.4 procedurally generated ones are in the v0.4 zip).

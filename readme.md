# Crave & Feast
Reworks hunger to reward food diversity. Crave one food for double nutrition; overeating reduces gains.  
Mod link: https://modrinth.com/mod/crave-and-feast

This mod makes food diversity finally matter.

Crave & Feast redesigns Minecraft’s hunger system to reward variety and discourage repetition.

![Craving Image](https://cdn.modrinth.com/data/cached_images/ef2915bd12a591555ee57fdd62c421b6ad84c375.png)

Each player has one food they crave. Eating that food gives 2× hunger and saturation, while eating the same thing repeatedly provides reduced benefits. This makes survival gameplay more balanced and flavorful — no more living on steak forever.

![Craving food](https://cdn.modrinth.com/data/cached_images/58c3b5bcfaaa150850cb9af738644add3dc2d678.png) 
![Never eaten](https://cdn.modrinth.com/data/cached_images/b1af1c125762794da42dd84b86c48c301727da2c.png) 
![Rarely eaten](https://cdn.modrinth.com/data/cached_images/481d1f379eb107ea164dfee2d5bde584d4ebffd9.png)
![Often eaten](https://cdn.modrinth.com/data/cached_images/0b6e8039a9e8b8ea557072ff3a7fc752d647917a.png)

### ⚙️ Default Gameplay Rules

All values are configurable in config/crave_and_feast.json. Here’s how the defaults work:

| Rule                        | Condition                               | Multiplier | Meaning                                        |
|-----------------------------|-----------------------------------------|------------|------------------------------------------------|
| 🍗 Craving food	            | Current craving item                    | × 2.0      | Always gives double nourishment and saturation |
| 🥩 First time eating a food | Eaten ≤ 0 times                         | × 2.0      | “Never eaten” — strongest bonus for new foods  |
| 🍞 Rarely eaten food        | Eaten ≤ 7 times                         | × 1.4      | Still somewhat rewarding                       |
| 🍖 Often eaten food         | Eaten > 7 times                         | × 0.8      | Small penalty — you’re getting used to it      |
| ⏳ Craving change interval   | Every 24 000 ticks (≈ 20 min real-time) | —          | Your craving rotates automatically             |
| 🍴 Craving limit per round  | After 8 times eating the same craving   | —          | A new craving will be chosen early             |
| 📜 Food history length      | 32 entries                              | —          | Only the most recent 32 foods are remembered   |

HUD defaults:

- Position = (hudX: 10, hudY: 10)
- Shows your current craving icon and name

### 🧩 Features

- Dynamic craving system that changes automatically
- Diminishing returns for repetitive foods
- Simple HUD indicator showing your craving
- Fully configurable multipliers, timing, and history length
- Works in both singleplayer and multiplayer

### ✅ Compatibility

- Minecraft 1.21.10
- Requires Fabric API
- Lightweight, safe to add or remove mid-save
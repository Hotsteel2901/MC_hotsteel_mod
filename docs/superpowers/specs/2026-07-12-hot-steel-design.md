# Hot Steel（热钢）模组设计规格

- **日期**: 2026-07-12
- **平台**: Minecraft 1.21.1 / Java 21 / Fabric
- **mod id**: `hotsteel`  **包名**: `com.hotsteel`  **名称**: Hot Steel
- **映射**: Mojmap（官方映射） + Fabric Loom
- **工具链假设（评审可改）**: Fabric Loader 0.16.x、Fabric API 0.116.x (1.21.1)、Loom 1.7+

## 1. 目标

新增一整套「热钢」装备，以及配套的新材料「钢」和一条多步加工链。全部装备数值高于下界合金，且穿齐全套盔甲可触发自定义的「超级抗火」。

## 2. 材料与加工链

| 物品 | 类型 | 获取 | 属性 |
|------|------|------|------|
| 粗铁 / 铁锭 | 原版 | 原版流程，无改动 | - |
| 粗钢 `crude_steel` | 物品 | 铁锭 → 原版高炉鼓风冶炼 | - |
| 粗钢块 `crude_steel_block` | 方块 | 4× 粗钢（2×2 合成）；可放置 | - |
| 钢锭 `steel_ingot` | 物品 | 粗钢块 → 原版高炉冶炼 | 防火（同下界合金）；本身无用途 |
| 热钢锭 `hot_steel_ingot` | 物品 | 钢锭掉落物在岩浆浸泡 ~100 tick 原地转化 | 防火；合成全部装备的材料 |

链路：`铁矿→粗铁→铁锭(原版)` → 高炉→`粗钢` → 2×2→`粗钢块` → 高炉→`钢锭` → 岩浆浸泡→`热钢锭` → 合成装备。

- 高炉配方：`iron_ingot → crude_steel`、`crude_steel_block → steel_ingot`（datagen blasting recipe，不与原版冲突）。
- 岩浆转化：Mixin 挂 `ItemEntity.tick()`，对 `steel_ingot` 掉落物计数在岩浆中的 tick，满 100 tick 替换为 `hot_steel_ingot` 掉落物，附带粒子/音效。

## 3. 装备清单（全部用热钢锭合成，全部防火）

- 盔甲(4)：`hot_steel_helmet` / `hot_steel_chestplate` / `hot_steel_leggings` / `hot_steel_boots` — 新 `ArmorMaterial "hot_steel"`
- 工具(5)：`hot_steel_sword` / `hot_steel_pickaxe` / `hot_steel_axe` / `hot_steel_shovel` / `hot_steel_hoe` — 新 `Tier "hot_steel"`
- 新武器：`hot_steel_knife`（刀）— 轻快武器
- 远程/特殊(4)：`hot_steel_bow` / `hot_steel_crossbow` / `hot_steel_trident` / `hot_steel_shield`
  - 三叉戟：自定义投掷实体 `HotSteelTridentEntity` + 客户端渲染器（原版三叉戟实体写死模型/拾取物）
  - 盾：静态模型（不支持旗帜图案），保留格挡逻辑

## 4. 数值（均高于下界合金）

| 属性 | 下界合金 | 热钢 |
|------|---------|------|
| 工具耐久 | 2031 | 2800 |
| 挖掘速度 | 9.0 | 10.0 |
| 攻击加成 | +4.0 | +5.0（剑显示≈10） |
| 附魔值 | 15 | 18 |
| 盔甲防御(靴/腿/甲/盔) | 3/6/8/3 | 4/7/9/4 |
| 盔甲韧性 | 3.0 | 4.0 |
| 击退抗性 | 0.1 | 0.15 |
| 盔甲耐久倍率 | 37 | 45 |
| 挖掘等级 | 可挖一切 | 同下界合金（用 `INCORRECT_FOR_NETHERITE_TOOL` 标签） |

- 刀：伤害 ~7、攻速 -2.0（比剑 -2.4 快）
- 弓/弩：更高耐久、拉满更快
- 三叉戟：投掷伤害 10、耐久 500
- 盾：耐久 500
- 修复材料：`hot_steel_ingot`

## 5. 超级抗火（穿全套 4 件盔甲）

- 每 server tick 检测：穿齐 4 件热钢盔甲 且 处于**火环境**（`isInLava()` / `isOnFire()` / 站在火里）。
- 满足时计时器累加，**≤1200 tick(60s)** 内生效：
  - 取消火/岩浆伤害：Fabric `ServerLivingEntityEvents.ALLOW_DAMAGE`（IN_FIRE / ON_FIRE / LAVA / HOT_FLOOR）。
  - 岩浆里像水里一样游泳：Mixin 让移动/游泳姿势代码在岩浆中走「水物理」分支 + 强制游泳姿势。
  - HUD：自定义 `MobEffect super_fire_resistance` 作图标 + 剩余秒数倒计时（每 tick 刷新剩余时长）。
- **满 60s 仍在火环境**：移除效果，恢复正常火/岩浆伤害（开始正常燃烧）。
- **离开火环境**：计时器归零，下次进入重新计 60s。
- 计时状态：server 端 `Map<UUID,Integer>`（重启丢失可接受）。

## 6. 工程结构（com.hotsteel）

- `HotSteel`(ModInitializer) / `HotSteelClient`(ClientModInitializer) / `HotSteelDataGen`(DataGeneratorEntrypoint)
- `registry/`：ModItems、ModBlocks、ModMaterials(Tier+ArmorMaterial)、ModEntities、ModEffects、ModParticles、ModCreativeTab、ModRecipeUtil
- `item/`：KnifeItem、HotSteelBowItem、HotSteelCrossbowItem、HotSteelTridentItem、HotSteelShieldItem
- `entity/`：HotSteelTridentEntity（+ client renderer）
- `effect/`：SuperFireResistanceEffect
- `logic/`：SuperFireResistanceHandler（server tick）
- `mixin/`：ItemEntityMixin（岩浆转化）、LivingEntityMixin（岩浆游泳/姿势）
- datagen：Recipes、Models、Loot、Tags（工具/附魔标签）、Localization（en_us + zh_cn）

## 7. 贴图工作流（重要）

1. 先生成占位 **SVG**（按最终 PNG 文件名命名，见下表的「暂存名」）。
2. 复制到 `/storage/emulated/0/TIE_TU`，**暂停**。
3. 用户修缮 SVG 并转换成同名 PNG（放回同一目录）。
4. 用户说「继续」后，按暂存名→目标路径映射，把 PNG 装入 `src/main/resources/assets/hotsteel/textures/...`；无需理解图像内容。

### 贴图映射表（暂存名 → 目标资源路径，尺寸）

资源前缀：`assets/hotsteel/textures/`

| 暂存名 (SVG/PNG) | 目标路径 | 尺寸 |
|---|---|---|
| crude_steel | item/crude_steel.png | 16×16 |
| steel_ingot | item/steel_ingot.png | 16×16 |
| hot_steel_ingot | item/hot_steel_ingot.png | 16×16 |
| hot_steel_sword | item/hot_steel_sword.png | 16×16 |
| hot_steel_pickaxe | item/hot_steel_pickaxe.png | 16×16 |
| hot_steel_axe | item/hot_steel_axe.png | 16×16 |
| hot_steel_shovel | item/hot_steel_shovel.png | 16×16 |
| hot_steel_hoe | item/hot_steel_hoe.png | 16×16 |
| hot_steel_knife | item/hot_steel_knife.png | 16×16 |
| hot_steel_helmet | item/hot_steel_helmet.png | 16×16 |
| hot_steel_chestplate | item/hot_steel_chestplate.png | 16×16 |
| hot_steel_leggings | item/hot_steel_leggings.png | 16×16 |
| hot_steel_boots | item/hot_steel_boots.png | 16×16 |
| hot_steel_bow | item/hot_steel_bow.png | 16×16 |
| hot_steel_bow_pulling_0 | item/hot_steel_bow_pulling_0.png | 16×16 |
| hot_steel_bow_pulling_1 | item/hot_steel_bow_pulling_1.png | 16×16 |
| hot_steel_bow_pulling_2 | item/hot_steel_bow_pulling_2.png | 16×16 |
| hot_steel_crossbow | item/hot_steel_crossbow.png | 16×16 |
| hot_steel_crossbow_pulling_0 | item/hot_steel_crossbow_pulling_0.png | 16×16 |
| hot_steel_crossbow_pulling_1 | item/hot_steel_crossbow_pulling_1.png | 16×16 |
| hot_steel_crossbow_pulling_2 | item/hot_steel_crossbow_pulling_2.png | 16×16 |
| hot_steel_crossbow_arrow | item/hot_steel_crossbow_arrow.png | 16×16 |
| hot_steel_crossbow_firework | item/hot_steel_crossbow_firework.png | 16×16 |
| hot_steel_trident_item | item/hot_steel_trident.png | 16×16 |
| hot_steel_shield | item/hot_steel_shield.png | 16×16 |
| crude_steel_block | block/crude_steel_block.png | 16×16 |
| hot_steel_layer_1 | models/armor/hot_steel_layer_1.png | 64×32 |
| hot_steel_layer_2 | models/armor/hot_steel_layer_2.png | 64×32 |
| hot_steel_trident_entity | entity/hot_steel_trident.png | 32×32 |
| super_fire_resistance | mob_effect/super_fire_resistance.png | 18×18 |

> 注意 trident 有两个贴图：`hot_steel_trident_item`（GUI 图标）与 `hot_steel_trident_entity`（3D 模型），暂存名不同以避免冲突。

## 8. 合成配方（datagen）

- blasting: `iron_ingot → crude_steel`
- shaped 2×2: `crude_steel ×4 → crude_steel_block`
- blasting: `crude_steel_block → steel_ingot`
- 岩浆转化（非配方）：`steel_ingot → hot_steel_ingot`
- crafting: 用 `hot_steel_ingot` 合成盔甲/工具（原版图案）、刀、弓、弩、三叉戟、盾
- 附魔标签：加入 `minecraft:enchantable/*` 相关标签以支持附魔台

## 9. 创造模式选项卡

自定义 `CreativeModeTab "Hot Steel"`，图标 = `hot_steel_ingot`，包含全部物品。

## 10. 测试/验证

- `./gradlew build` 通过（编译 + datagen）。
- `./gradlew runDatagen` 生成资源无误。
- 手测：加工链、装备数值、超级抗火计时与岩浆游泳、三叉戟投掷。

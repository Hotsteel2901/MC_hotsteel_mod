package com.hotsteel.data;

import java.util.concurrent.CompletableFuture;

import com.hotsteel.registry.ModBlocks;
import com.hotsteel.registry.ModCreativeTab;
import com.hotsteel.registry.ModEffects;
import com.hotsteel.registry.ModEntities;
import com.hotsteel.registry.ModItems;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;

public class ModChineseLangProvider extends FabricLanguageProvider {

    public ModChineseLangProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, "zh_cn", registries);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider registries, TranslationBuilder tb) {
        tb.add(ModItems.CRUDE_STEEL, "粗钢");
        tb.add(ModItems.STEEL_INGOT, "钢锭");
        tb.add(ModItems.HOT_STEEL_INGOT, "热钢锭");
        tb.add(ModItems.HOT_STEEL_NUGGET, "热钢粒");
        tb.add(ModItems.MOLTEN_CORE, "熔核");
        tb.add(ModItems.HOT_STEEL_APPLE, "热钢苹果");
        tb.add(ModBlocks.CRUDE_STEEL_BLOCK, "粗钢块");
        tb.add(ModBlocks.STEEL_BLOCK, "钢块");
        tb.add(ModBlocks.HOT_STEEL_BLOCK, "热钢块");
        tb.add(ModBlocks.HOT_STEEL_STAIRS, "热钢楼梯");
        tb.add(ModBlocks.HOT_STEEL_SLAB, "热钢台阶");
        tb.add(ModBlocks.HOT_STEEL_WALL, "热钢墙");
        tb.add(ModBlocks.HOT_STEEL_FORGE, "热钢锻炉");
        tb.add(ModBlocks.HOT_STEEL_SMELTER, "热钢熔炼池");
        tb.add(ModBlocks.HOT_STEEL_DOOR, "热钢门");
        tb.add(ModBlocks.HOT_STEEL_TRAPDOOR, "热钢活板门");
        tb.add(ModBlocks.HOT_STEEL_FENCE, "热钢栅栏");
        tb.add(ModBlocks.HOT_STEEL_BRICKS, "热钢砖");
        tb.add(ModBlocks.HOT_STEEL_PRESSURE_PLATE, "热钢压力板");
        tb.add(ModBlocks.HOT_STEEL_LANTERN, "热钢灯笼");
        tb.add(ModBlocks.HOT_STEEL_CHAIN, "热钢锁链");
        tb.add(ModBlocks.HOT_STEEL_LADDER, "热钢梯子");

        tb.add(ModItems.HOT_STEEL_HELMET, "热钢头盔");
        tb.add(ModItems.HOT_STEEL_CHESTPLATE, "热钢胸甲");
        tb.add(ModItems.HOT_STEEL_LEGGINGS, "热钢护腿");
        tb.add(ModItems.HOT_STEEL_BOOTS, "热钢靴子");

        tb.add(ModItems.HOT_STEEL_SWORD, "热钢剑");
        tb.add(ModItems.HOT_STEEL_MACE, "热钢战锤");
        tb.add(ModItems.HOT_STEEL_KNIFE, "热钢刀");
        tb.add(ModItems.HOT_STEEL_PICKAXE, "热钢镐");
        tb.add(ModItems.HOT_STEEL_AXE, "热钢斧");
        tb.add(ModItems.HOT_STEEL_SHOVEL, "热钢铲");
        tb.add(ModItems.HOT_STEEL_HOE, "热钢锄");
        tb.add(ModItems.HOT_STEEL_PAXEL, "热钢镐斧铲");

        tb.add(ModItems.HOT_STEEL_BOW, "热钢弓");
        tb.add(ModItems.HOT_STEEL_CROSSBOW, "热钢弩");
        tb.add(ModItems.HOT_STEEL_TRIDENT, "热钢三叉戟");
        tb.add(ModItems.HOT_STEEL_SHIELD, "热钢盾");
        tb.add(ModItems.HOT_STEEL_ARROW, "热钢箭");
        tb.add(ModItems.HOT_STEEL_FIREBALL, "热钢火球");
        tb.add(ModItems.HOT_STEEL_FISHING_ROD, "热钢钓鱼竿");
        tb.add(ModItems.HOT_STEEL_SICKLE, "热钢镰刀");
        tb.add(ModItems.LAVA_BOTTLE, "熔岩瓶");
        tb.add(ModItems.LAVA_GOLEM_SPAWN_EGG, "熔岩傀儡刷怪蛋");
        tb.add(ModItems.FIRE_WRAITH_SPAWN_EGG, "烈火怨灵刷怪蛋");

        tb.add(ModEntities.HOT_STEEL_TRIDENT, "热钢三叉戟");
        tb.add(ModEntities.LAVA_GOLEM, "熔岩傀儡");
        tb.add(ModEntities.FIRE_WRAITH, "烈火怨灵");
        tb.add(ModEffects.SUPER_FIRE_RESISTANCE.value(), "超级抗火");
        tb.add(ModCreativeTab.HOT_STEEL_TAB_KEY, "热钢");

        // 说明文字
        tb.add("item.hotsteel.hot_steel_pickaxe.lore", "自动熔炼：采掘矿石直接化为锭");
        tb.add("item.hotsteel.melee.lore", "灼热之刃：攻击时引燃目标");
        tb.add("item.hotsteel.hot_steel_arrow.lore", "灼热之矢：命中时引燃目标");
        tb.add("item.hotsteel.armor.lore", "套装加成：2件免疫火焰，4件触发超级抗火");
        tb.add("item.hotsteel.hot_steel_sword.lore", "右键：向前打出一道火焰（消耗耐久）");
        tb.add("item.hotsteel.hot_steel_axe.lore", "整树砍伐：砍断一根原木，整棵树一起倒");
        tb.add("item.hotsteel.hot_steel_shovel.lore", "潜行+使用：一次性挖开3x3软质方块");
        tb.add("item.hotsteel.hot_steel_hoe.lore", "右键成熟作物：3x3范围收割并自动补种");
        tb.add("item.hotsteel.hot_steel_fireball.lore", "可投掷：爆炸并点燃周围区域");
        tb.add("item.hotsteel.lava_bottle.lore", "可投掷：将落点区域化为岩浆");
        tb.add("item.hotsteel.lava_golem_spawn_egg.lore", "召唤熔岩守卫——防火、可在岩浆上漂浮");
        tb.add("item.hotsteel.fire_wraith_spawn_egg.lore", "召唤下界烈火怨灵——掉落熔核");
        tb.add("item.hotsteel.hot_steel_fishing_rod.lore", "钓上来的鱼会自动烤熟");
        tb.add("item.hotsteel.hot_steel_sickle.lore", "一键收割5x5范围成熟作物并自动补种");
        tb.add("block.hotsteel.hot_steel_forge.lore", "手持受损热钢装备右键，可用热钢锭修复");
        tb.add("block.hotsteel.hot_steel_smelter.lore", "把矿石丢上去——瞬间熔炼成锭");
        tb.add("block.hotsteel.hot_steel_pressure_plate.lore", "会灼烧站在上面的生物");
        tb.add("item.hotsteel.molten_core.lore", "右键：瞬间熔炼背包里所有可熔炼物品");
        tb.add("item.hotsteel.hot_steel_paxel.lore", "镐+斧+铲三合一——采掘矿石自动熔炼成锭");
        tb.add("item.hotsteel.hot_steel_apple.lore", "滚烫的小零食：随时获得火焰免疫与生命恢复");
        tb.add("block.hotsteel.hot_steel_chain.lore", "发光且防火的锁链——哪里都能挂");
        tb.add("block.hotsteel.hot_steel_ladder.lore", "永不燃烧、微微发光的梯子");
        tb.add("block.hotsteel.hot_steel_block.lore", "附近玩家获得被动火焰抗性");

        // 成就
        tb.add("advancements.hotsteel.crude_steel.title", "这是钢？");
        tb.add("advancements.hotsteel.crude_steel.description", "冶炼出你的第一块粗钢。看着还挺糙。");
        tb.add("advancements.hotsteel.steel_ingot.title", "哇！钢锭！");
        tb.add("advancements.hotsteel.steel_ingot.description", "获得第一块钢锭。亮闪闪、防火，但暂时没啥用。");
        tb.add("advancements.hotsteel.hot_steel_ingot.title", "？！热热！？");
        tb.add("advancements.hotsteel.hot_steel_ingot.description", "获得第一块热钢锭。烫！千万别徒手拿。");
        tb.add("advancements.hotsteel.full_armor.title", "燃烧吧，炽热的钢铁！");
        tb.add("advancements.hotsteel.full_armor.description", "穿上整套热钢护甲。岩浆？不存在的。");
        tb.add("advancements.hotsteel.hot_steel_hoe.title", "终极的终极奉献");
        tb.add("advancements.hotsteel.hot_steel_hoe.description", "打造一把热钢锄。全模组最强材料……做了把锄头。肃然起敬。");
        tb.add("advancements.hotsteel.steel_block.title", "货架人生");
        tb.add("advancements.hotsteel.steel_block.description", "把钢锭存进钢块里。");
        tb.add("advancements.hotsteel.hot_steel_block.title", "炽热的地基");
        tb.add("advancements.hotsteel.hot_steel_block.description", "锻造一块热钢块。其实很适合做信标基座。");
        tb.add("advancements.hotsteel.hot_steel_mace.title", "热钢重击");
        tb.add("advancements.hotsteel.hot_steel_mace.description", "打造一把热钢战锤。从高处坠落能打出额外伤害。");
        tb.add("advancements.hotsteel.auto_smelt.title", "熔岩铸矿");
        tb.add("advancements.hotsteel.auto_smelt.description", "用热钢镐采掘矿石，当场得到锭。");
        tb.add("advancements.hotsteel.set_bonus_2.title", "火焰结界");
        tb.add("advancements.hotsteel.set_bonus_2.description", "穿上2件热钢护甲，抵御火焰伤害。");
        tb.add("advancements.hotsteel.tree_felling.title", "一刀砍倒整片森林");
        tb.add("advancements.hotsteel.tree_felling.description", "用热钢斧一斧砍断原木，整棵树应声而倒。");
        tb.add("advancements.hotsteel.area_dig.title", "铲出一个大坑");
        tb.add("advancements.hotsteel.area_dig.description", "潜行时用热钢铲一次性挖开3x3区域。");
        tb.add("advancements.hotsteel.forge_repair.title", "回到锻炉旁");
        tb.add("advancements.hotsteel.forge_repair.description", "在热钢锻炉上修复一件受损的热钢装备。");
        tb.add("advancements.hotsteel.lava_golem.title", "熔岩保镖");
        tb.add("advancements.hotsteel.lava_golem.description", "召唤一只熔岩傀儡——它能在岩浆上漂浮并灼烧敌人。");
        tb.add("advancements.hotsteel.fire_wraith.title", "斩杀怨灵");
        tb.add("advancements.hotsteel.fire_wraith.description", "击败一只烈火怨灵，夺取它的熔核。");
        tb.add("advancements.hotsteel.sickle_harvest.title", "大丰收");
        tb.add("advancements.hotsteel.sickle_harvest.description", "用热钢镰刀收割一大片成熟作物。");
        tb.add("advancements.hotsteel.smelter_use.title", "即时熔炼");
        tb.add("advancements.hotsteel.smelter_use.description", "把矿石丢上热钢熔炼池，看它当场变成锭。");
        tb.add("advancements.hotsteel.molten_core.title", "火焰之心");
        tb.add("advancements.hotsteel.molten_core.description", "手持一块熔核——烈火怨灵仍在燃烧的心脏。");
        tb.add("advancements.hotsteel.molten_core_use.title", "一键熔炼");
        tb.add("advancements.hotsteel.molten_core_use.description", "消耗一块熔核，瞬间熔炼整个背包。");
        tb.add("advancements.hotsteel.hot_steel_paxel.title", "万用神兵");
        tb.add("advancements.hotsteel.hot_steel_paxel.description", "锻造一把热钢镐斧铲——镐、斧、铲集于一身。");
        tb.add("advancements.hotsteel.hot_steel_apple.title", "咬一口炽热");
        tb.add("advancements.hotsteel.hot_steel_apple.description", "吃掉一个热钢苹果。没错它很烫，但这正是重点。");
        tb.add("advancements.hotsteel.hot_steel_chain.title", "连锁锻炉");
        tb.add("advancements.hotsteel.hot_steel_chain.description", "打造一条热钢锁链——发光且防火。");

        // 聊天提示
        tb.add("message.hotsteel.flame_ward_on", "火焰结界启动——火焰伤害免疫！");
        tb.add("message.hotsteel.flame_ward_off", "火焰结界失效。");
        tb.add("message.hotsteel.super_fire_on", "超级抗火启动！");
        tb.add("message.hotsteel.super_fire_off", "超级抗火失效！");
        tb.add("message.hotsteel.forge_no_damage", "这件装备已经是满耐久的了。");
        tb.add("message.hotsteel.forge_need_ingots", "热钢锭不足，还需要%s个。");
        tb.add("message.hotsteel.forge_repair", "已在锻炉修复（消耗%s个热钢锭）。");

        // =====================================================================
        //  熔炉纪元
        // =====================================================================
        tb.add(ModItems.MOLTEN_SHARD, "熔核碎片");
        tb.add(ModItems.MOLTEN_STEEL_INGOT, "熔铸锭");
        tb.add(ModItems.MOLTEN_STEEL_NUGGET, "熔铸粒");
        tb.add(ModItems.FORGE_HEART, "熔炉之心");
        tb.add(ModItems.HOT_STEEL_CODEX, "热钢手册");
        tb.add(ModItems.SCORCHED_PAGE, "焦痕残页");
        tb.add(ModBlocks.MOLTEN_STEEL_BLOCK, "熔铸块");
        tb.add(ModBlocks.MOLTEN_ALTAR, "熔火祭坛");
        tb.add(ModBlocks.MOLTEN_GLASS, "熔火玻璃");
        tb.add(ModBlocks.CHARRED_BRICKS, "焦黑砖");
        tb.add(ModBlocks.MOLTEN_STEEL_CHAIN, "熔铸锁链");
        tb.add(ModBlocks.MOLTEN_LANTERN, "熔铸灯");

        tb.add(ModItems.MOLTEN_STEEL_HELMET, "熔铸头盔");
        tb.add(ModItems.MOLTEN_STEEL_CHESTPLATE, "熔铸胸甲");
        tb.add(ModItems.MOLTEN_STEEL_LEGGINGS, "熔铸护腿");
        tb.add(ModItems.MOLTEN_STEEL_BOOTS, "熔铸靴子");

        tb.add(ModItems.MOLTEN_STEEL_SWORD, "熔铸剑");
        tb.add(ModItems.MOLTEN_STEEL_SCYTHE, "熔铸战镰");
        tb.add(ModItems.MOLTEN_STEEL_PICKAXE, "熔铸镐");
        tb.add(ModItems.MOLTEN_STEEL_AXE, "熔铸斧");
        tb.add(ModItems.MOLTEN_STEEL_SHOVEL, "熔铸铲");
        tb.add(ModItems.MOLTEN_STEEL_HOE, "熔铸锄");

        tb.add(ModItems.SLAG_CRAWLER_SPAWN_EGG, "熔渣爬虫刷怪蛋");
        tb.add(ModItems.EMBER_WISP_SPAWN_EGG, "火翼灵刷怪蛋");
        tb.add(ModItems.ANCIENT_FORGEBORN_SPAWN_EGG, "远古熔铸者刷怪蛋");

        tb.add(ModEntities.SLAG_CRAWLER, "熔渣爬虫");
        tb.add(ModEntities.EMBER_WISP, "火翼灵");
        tb.add(ModEntities.ANCIENT_FORGEBORN, "远古熔铸者");
        tb.add(ModEffects.FORGE_BLESSING.value(), "熔炉祝福");

        // 手册界面
        tb.add("gui.hotsteel.codex.title", "熔炉纪元编年史");
        tb.add("gui.hotsteel.codex.scroll_hint", "滚动以继续阅读");

        // 剧情文本——从旧熔炉的编年史残卷中复原的章节
        tb.add("hotsteel.chronicle.chapter_1.title", "钢铁之始");
        tb.add("hotsteel.chronicle.chapter_1.body",
            "在熔炉纪元之前，老铁匠们只识得铁。直到那一夜，炉火被鼓得比以往任何时候都旺；待炉渣冷却，灰烬里静静躺着一块灰白的钢。\n\n"
            + "那第一块粗钢又暗又脆，却藏着铁永远磨不出的锋刃。他们淬火、折叠、再淬火，固执的手掌间终于敲出了第一批钢锭。\n\n"
            + "编年史为那一夜只留下一行字：『炉火燃至天明，而天明即是钢铁。』");
        tb.add("hotsteel.chronicle.chapter_2.title", "熔火之路");
        tb.add("hotsteel.chronicle.chapter_2.body",
            "仅有钢铁并不足够。铁匠们背着钢锭，走向地底奔流的火河；在岩浆如水的深处，他们学会以山峦之血淬炼钢材。\n\n"
            + "浸入火流的钢锭并未熔化，反而饮尽高温，归来时已成热钢，即便在暗处也微微发亮。披挂它的人发现，火焰竟自行退避，仿佛这金属仍记得自己的来处。\n\n"
            + "自那日起，通往锻炉的路被人赤足走过，火，不再是危险。");
        tb.add("hotsteel.chronicle.chapter_3.title", "烈火之魂");
        tb.add("hotsteel.chronicle.chapter_3.body",
            "再往深处，于烈焰吞噬之地，铁匠们遇见似生非生之物——以火为躯的怨灵，于余烬噼啪中低语，穿过重重火幕，他们称其烈火怨灵。\n\n"
            + "每只怨灵体内都燃着一枚熔核：一团永不冷却的炽热，纵然浸入水中亦然。铁匠们交换、猎杀，往往为之丧命，只因他们终于明白，熔核正是熔炉纪元的关键。\n\n"
            + "『身裹烈焰，』编年史告诫，『它们只与倾听余烬者说话。』");
        tb.add("hotsteel.chronicle.chapter_4.title", "熔铸之躯");
        tb.add("hotsteel.chronicle.chapter_4.body",
            "转机出现在一名铁匠将熔核按入热钢锭、趁其炽白之时反复锻打的那一刻。金属与熔核相融，铁砧上留下的已非二者——那是熔铸钢。\n\n"
            + "它比任何已知甲胄都更沉重，也更炽热，却不灼伤穿戴者。披上整套甲胄，人便成了锻炉的一部分——熔铸之躯，无惧火焰与岩浆，心跳如风箱鼓动。\n\n"
            + "『铁愈重，心愈热，』第一个披上它的人这样写道。");
        tb.add("hotsteel.chronicle.chapter_5.title", "熔炉之主");
        tb.add("hotsteel.chronicle.chapter_5.body",
            "最后只剩那古老的仪式。在熔火祭坛之上，以热钢平台相围，放下熔核，唤醒那自第一座炉火燃起便沉睡至今的存在——远古熔铸者。\n\n"
            + "它守望火焰已有万古，绝不会轻易交出宝物。将它击倒，它的心脏——熔炉之心——便归你所有：一枚仍在燃烧的余烬，赐予你熔炉的祝福。\n\n"
            + "熔炉之主既已倒下，熔炉纪元自此开启。");

        // 熔炉纪元成就
        tb.add("advancements.hotsteel.chapter_1.title", "钢铁之始");
        tb.add("advancements.hotsteel.chapter_1.description", "冶炼出第一块钢锭，翻开编年史的第一章。");
        tb.add("advancements.hotsteel.chapter_2.title", "熔火之路");
        tb.add("advancements.hotsteel.chapter_2.description", "在岩浆中淬炼钢锭，铸成热钢锭——火焰再也无法伤你。");
        tb.add("advancements.hotsteel.chapter_3.title", "烈火之魂");
        tb.add("advancements.hotsteel.chapter_3.description", "持有熔核碎片，拾回关于烈火怨灵的低语。");
        tb.add("advancements.hotsteel.chapter_4.title", "熔铸之躯");
        tb.add("advancements.hotsteel.chapter_4.description", "锻造熔铸锭——更沉重的甲胄，更炽热的心。");
        tb.add("advancements.hotsteel.chapter_5.title", "熔炉之主");
        tb.add("advancements.hotsteel.chapter_5.description", "在熔火祭坛击败远古熔铸者，赢取熔炉之心。");

        // 熔炉纪元聊天提示
        tb.add("message.hotsteel.altar_hint", "手持熔核右键祭坛，即可唤醒远古熔铸者。");
        tb.add("message.hotsteel.altar_needs_platform", "祭坛下方需要一整块 3×3 的热钢块平台。");
        tb.add("message.hotsteel.altar_awakened", "熔炉震颤——远古熔铸者已被唤醒！");
        tb.add("message.hotsteel.forgeborn_defeated", "远古熔铸者倒下了。熔炉之心归你所有。");
        tb.add("message.hotsteel.codex_progress", "手册已复原 %s / %s 章。");
        tb.add("message.hotsteel.page_recovered", "焦痕残页复原了章节：%s");
        tb.add("message.hotsteel.page_already_complete", "所有章节都已复原，残页再无文字。");
        tb.add("message.hotsteel.molten_body_on", "熔铸之躯启动——火焰与岩浆皆无法伤你！");
        tb.add("message.hotsteel.molten_body_off", "熔铸之躯失效。");
    }
}

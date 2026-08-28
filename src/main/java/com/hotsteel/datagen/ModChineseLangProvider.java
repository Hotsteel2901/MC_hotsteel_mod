package com.hotsteel.datagen;

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
        tb.add(ModBlocks.CRUDE_STEEL_BLOCK, "粗钢块");
        tb.add(ModBlocks.STEEL_BLOCK, "钢块");
        tb.add(ModBlocks.HOT_STEEL_BLOCK, "热钢块");

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

        tb.add(ModItems.HOT_STEEL_BOW, "热钢弓");
        tb.add(ModItems.HOT_STEEL_CROSSBOW, "热钢弩");
        tb.add(ModItems.HOT_STEEL_TRIDENT, "热钢三叉戟");
        tb.add(ModItems.HOT_STEEL_SHIELD, "热钢盾");
        tb.add(ModItems.HOT_STEEL_ARROW, "热钢箭");

        tb.add(ModEntities.HOT_STEEL_TRIDENT, "热钢三叉戟");
        tb.add(ModEffects.SUPER_FIRE_RESISTANCE.value(), "超级抗火");
        tb.add(ModCreativeTab.HOT_STEEL_TAB_KEY, "热钢");

        // 说明文字
        tb.add("item.hotsteel.hot_steel_pickaxe.lore", "自动熔炼：采掘矿石直接化为锭");
        tb.add("item.hotsteel.melee.lore", "灼热之刃：攻击时引燃目标");
        tb.add("item.hotsteel.hot_steel_arrow.lore", "灼热之矢：命中时引燃目标");
        tb.add("item.hotsteel.armor.lore", "套装加成：2件免疫火焰，4件触发超级抗火");

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

        // 聊天提示
        tb.add("message.hotsteel.flame_ward_on", "火焰结界启动——火焰伤害免疫！");
        tb.add("message.hotsteel.flame_ward_off", "火焰结界失效。");
        tb.add("message.hotsteel.super_fire_on", "超级抗火启动！");
        tb.add("message.hotsteel.super_fire_off", "超级抗火失效！");
    }
}

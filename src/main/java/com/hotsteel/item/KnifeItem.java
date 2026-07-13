package com.hotsteel.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/** A light, fast blade: lower damage than a sword but quicker swing. */
public class KnifeItem extends SwordItem {
    public KnifeItem(Tier tier, Properties properties) {
        super(tier, properties);
    }
}

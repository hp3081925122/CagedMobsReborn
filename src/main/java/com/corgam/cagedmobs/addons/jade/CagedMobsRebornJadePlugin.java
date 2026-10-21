package com.corgam.cagedmobsreborn.addons.jade;

import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlock;
import com.corgam.cagedmobsreborn.blocks.mob_cage.MobCageBlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class CagedMobsRebornJadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(new CagedMobsRebornComponentProvider(), MobCageBlock.class);
    }

    @Override
    public void register(IWailaCommonRegistration registration){
        registration.registerItemStorage(new HideContainerItemsProvider(), MobCageBlockEntity.class);
    }
}
package com.eriox.pvpaddon;

import com.eriox.pvpaddon.modules.*;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.item.ItemGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErioxPVPAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("eriox-pvp-addon");

    @Override
    public void onInitialize() {
        LOG.info("Eriox PVP Addon loaded.");

        Modules.get().add(new MaceCombo());
        Modules.get().add(new SpearPVP());
        Modules.get().add(new SwordCombo());
        Modules.get().add(new AutoTotem());
        Modules.get().add(new CrystalAura());
        Modules.get().add(new AntiKnockback());
        Modules.get().add(new VelocityControl());
        Modules.get().add(new ArmorAlert());
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(new ItemGroup.Key("Eriox PVP", "eriox-pvp"));
    }

    @Override
    public String getPackage() {
        return "com.eriox.pvpaddon";
    }
}

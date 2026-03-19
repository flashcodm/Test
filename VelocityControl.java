package com.eriox.pvpaddon.modules;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;

public class VelocityControl extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal")
        .description("Multiplier for horizontal velocity from hits (0 = none, 1 = normal).")
        .defaultValue(0.0)
        .min(0.0)
        .sliderMax(2.0)
        .build()
    );

    private final Setting<Double> vertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical")
        .description("Multiplier for vertical velocity from hits (0 = none, 1 = normal).")
        .defaultValue(0.0)
        .min(0.0)
        .sliderMax(2.0)
        .build()
    );

    private final Setting<Boolean> onlyInCombat = sgGeneral.add(new BoolSetting.Builder()
        .name("only-in-combat")
        .description("Only apply velocity control when near an enemy player.")
        .defaultValue(false)
        .build()
    );

    public VelocityControl() {
        super(ErioxCategory.CATEGORY, "VelocityControl", "Fine-tune how much velocity you take from enemy hits.");
    }

    public double getHorizontal() { return horizontal.get(); }
    public double getVertical() { return vertical.get(); }
    public boolean onlyInCombat() { return onlyInCombat.get(); }
}

package com.eriox.pvpaddon.modules;

import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.client.MinecraftClient;

public class AntiKnockback extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> horizontal = sgGeneral.add(new DoubleSetting.Builder()
        .name("horizontal")
        .description("Horizontal knockback reduction (0 = full KB, 1 = no KB).")
        .defaultValue(1.0)
        .min(0.0)
        .sliderMax(1.0)
        .build()
    );

    private final Setting<Double> vertical = sgGeneral.add(new DoubleSetting.Builder()
        .name("vertical")
        .description("Vertical knockback reduction (0 = full KB, 1 = no KB).")
        .defaultValue(1.0)
        .min(0.0)
        .sliderMax(1.0)
        .build()
    );

    public AntiKnockback() {
        super(ErioxCategory.CATEGORY, "AntiKnockback", "Reduces or negates knockback received from attacks.");
    }

    public double getHorizontal() { return horizontal.get(); }
    public double getVertical() { return vertical.get(); }
}

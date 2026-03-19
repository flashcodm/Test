package com.eriox.pvpaddon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ArmorAlert extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> durabilityThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("durability-threshold")
        .description("Warn when armor durability drops below this percentage.")
        .defaultValue(20)
        .min(1)
        .sliderMax(100)
        .build()
    );

    private final Setting<Boolean> chatAlert = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-alert")
        .description("Show warning in chat when armor is low.")
        .defaultValue(true)
        .build()
    );

    private boolean[] warned = new boolean[4];

    public ArmorAlert() {
        super(ErioxCategory.CATEGORY, "ArmorAlert", "Warns you when your armor durability is critically low.");
    }

    @Override
    public void onActivate() {
        warned = new boolean[4];
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;

        ItemStack[] armor = new ItemStack[]{
            mc.player.getInventory().getArmorStack(3), // helmet
            mc.player.getInventory().getArmorStack(2), // chestplate
            mc.player.getInventory().getArmorStack(1), // leggings
            mc.player.getInventory().getArmorStack(0)  // boots
        };

        String[] names = {"Helmet", "Chestplate", "Leggings", "Boots"};

        for (int i = 0; i < armor.length; i++) {
            ItemStack stack = armor[i];
            if (stack.isEmpty()) { warned[i] = false; continue; }
            if (!(stack.getItem() instanceof ArmorItem)) continue;

            int maxDur = stack.getMaxDamage();
            int curDur = maxDur - stack.getDamage();
            int pct = (int) ((curDur / (float) maxDur) * 100);

            if (pct <= durabilityThreshold.get() && !warned[i]) {
                warned[i] = true;
                if (chatAlert.get()) {
                    mc.player.sendMessage(
                        Text.literal("[ArmorAlert] ⚠ " + names[i] + " at " + pct + "% durability!").formatted(Formatting.RED),
                        false
                    );
                }
            } else if (pct > durabilityThreshold.get()) {
                warned[i] = false;
            }
        }
    }
}

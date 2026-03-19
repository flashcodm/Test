package com.eriox.pvpaddon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.client.MinecraftClient;

public class SwordCombo extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> attackRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("attack-range")
        .description("Range to attack target.")
        .defaultValue(3.5)
        .min(1.0)
        .sliderMax(6.0)
        .build()
    );

    private final Setting<Boolean> wtap = sgGeneral.add(new BoolSetting.Builder()
        .name("w-tap")
        .description("Auto W-tap to boost knockback on hits.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> sprintReset = sgGeneral.add(new BoolSetting.Builder()
        .name("sprint-reset")
        .description("Briefly stop sprinting before each hit for crit consistency.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> autoSprint = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-sprint")
        .description("Auto sprint toward nearest target.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> attackDelay = sgGeneral.add(new IntSetting.Builder()
        .name("attack-delay")
        .description("Ticks between sword attacks.")
        .defaultValue(8)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private int attackTimer = 0;
    private boolean wtapState = false;

    public SwordCombo() {
        super(ErioxCategory.CATEGORY, "SwordCombo", "Auto sword combo with W-tap and sprint reset for max PVP efficiency.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (!holdingSword()) return;

        attackTimer++;
        PlayerEntity target = getNearestPlayer(attackRange.get());
        if (target == null) return;

        if (autoSprint.get()) {
            mc.player.setSprinting(true);
        }

        if (attackTimer >= attackDelay.get()) {
            if (sprintReset.get()) {
                mc.player.setSprinting(false);
            }

            if (wtap.get()) {
                wtapState = !wtapState;
                mc.options.forwardKey.setPressed(wtapState);
            }

            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.attackEntity(mc.player, target);
            attackTimer = 0;

            if (sprintReset.get()) {
                mc.player.setSprinting(true);
            }
        }
    }

    private boolean holdingSword() {
        return mc.player.getMainHandStack().getItem() instanceof SwordItem
            || mc.player.getMainHandStack().getItem() == Items.STICK; // fallback for modded
    }

    private PlayerEntity getNearestPlayer(double range) {
        PlayerEntity closest = null;
        double closestDist = range;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            double dist = mc.player.distanceTo(p);
            if (dist < closestDist) {
                closestDist = dist;
                closest = p;
            }
        }
        return closest;
    }
}

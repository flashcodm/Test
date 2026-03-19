package com.eriox.pvpaddon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.client.MinecraftClient;

public class SpearPVP extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> throwRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("throw-range")
        .description("Range to auto-throw trident at target.")
        .defaultValue(12.0)
        .min(2.0)
        .sliderMax(20.0)
        .build()
    );

    private final Setting<Boolean> autoRiptide = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-riptide")
        .description("Auto use riptide to dash toward target.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> throwCooldown = sgGeneral.add(new IntSetting.Builder()
        .name("throw-cooldown")
        .description("Ticks between auto-throws.")
        .defaultValue(20)
        .min(5)
        .sliderMax(60)
        .build()
    );

    private final Setting<Boolean> autoRecall = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-recall")
        .description("Switch to empty hand to catch the trident faster (loyalty).")
        .defaultValue(true)
        .build()
    );

    private int cooldownTimer = 0;
    private boolean thrown = false;

    public SpearPVP() {
        super(ErioxCategory.CATEGORY, "SpearPVP", "Automates trident throwing and riptide timing for PVP.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (!holdingTrident()) return;

        cooldownTimer++;

        PlayerEntity target = getNearestPlayer(throwRange.get());
        if (target == null) return;

        if (!thrown && cooldownTimer >= throwCooldown.get()) {
            lookAt(target);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            thrown = true;
            cooldownTimer = 0;
        }

        if (thrown && autoRecall.get()) {
            if (cooldownTimer > 5) thrown = false;
        }
    }

    private boolean holdingTrident() {
        return mc.player.getMainHandStack().getItem() == Items.TRIDENT;
    }

    private void lookAt(PlayerEntity target) {
        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + target.getHeight() / 2) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = target.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
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

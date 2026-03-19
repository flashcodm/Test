package com.eriox.pvpaddon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.client.MinecraftClient;

import java.util.List;

public class MaceCombo extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> fallHeight = sgGeneral.add(new IntSetting.Builder()
        .name("fall-height")
        .description("Minimum fall height (in blocks) to trigger mace crit.")
        .defaultValue(5)
        .min(2)
        .sliderMax(20)
        .build()
    );

    private final Setting<Double> targetRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("target-range")
        .description("Range to look for targets.")
        .defaultValue(5.0)
        .min(1.0)
        .sliderMax(10.0)
        .build()
    );

    private final Setting<Boolean> autoWindCharge = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-wind-charge")
        .description("Automatically use Wind Charge to boost before mace hit.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> windChargeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("wind-charge-delay")
        .description("Ticks to wait after wind charge before swinging mace.")
        .defaultValue(3)
        .min(1)
        .sliderMax(10)
        .build()
    );

    private int windChargeTimer = 0;
    private boolean windChargeUsed = false;

    public MaceCombo() {
        super(ErioxCategory.CATEGORY, "MaceCombo", "Automates mace crit combos with Wind Charge timing.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;
        if (!holdingMace()) return;

        PlayerEntity target = getNearestPlayer();
        if (target == null) return;

        double fallDist = mc.player.fallDistance;

        if (autoWindCharge.get() && !windChargeUsed) {
            if (hasWindCharge() && fallDist < 2) {
                useWindCharge();
                windChargeUsed = true;
                windChargeTimer = 0;
                return;
            }
        }

        if (windChargeUsed) {
            windChargeTimer++;
            if (windChargeTimer < windChargeDelay.get()) return;
        }

        if (fallDist >= fallHeight.get()) {
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.attackEntity(mc.player, target);
            windChargeUsed = false;
            windChargeTimer = 0;
        }
    }

    private boolean holdingMace() {
        return mc.player.getMainHandStack().getItem() == Items.MACE;
    }

    private boolean hasWindCharge() {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.WIND_CHARGE) return true;
        }
        return false;
    }

    private void useWindCharge() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.WIND_CHARGE) {
                slot = i;
                break;
            }
        }
        if (slot == -1) return;
        int prevSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = slot;
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.getInventory().selectedSlot = prevSlot;
    }

    private PlayerEntity getNearestPlayer() {
        PlayerEntity closest = null;
        double closestDist = targetRange.get();
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

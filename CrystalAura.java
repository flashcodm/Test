package com.eriox.pvpaddon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.MinecraftClient;

public class CrystalAura extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> placeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("place-range")
        .description("Range to place end crystals.")
        .defaultValue(5.0)
        .min(1.0)
        .sliderMax(8.0)
        .build()
    );

    private final Setting<Double> breakRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("break-range")
        .description("Range to break end crystals.")
        .defaultValue(5.0)
        .min(1.0)
        .sliderMax(8.0)
        .build()
    );

    private final Setting<Integer> placeDelay = sgGeneral.add(new IntSetting.Builder()
        .name("place-delay")
        .description("Ticks between crystal placements.")
        .defaultValue(4)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Double> minDamage = sgGeneral.add(new DoubleSetting.Builder()
        .name("min-damage")
        .description("Minimum damage to target before placing.")
        .defaultValue(4.0)
        .min(0.0)
        .sliderMax(20.0)
        .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Auto switch to end crystals in hotbar.")
        .defaultValue(true)
        .build()
    );

    private int placeTimer = 0;

    public CrystalAura() {
        super(ErioxCategory.CATEGORY, "CrystalAura", "Auto places and breaks end crystals for massive PVP damage.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        // Break nearby crystals first
        for (EndCrystalEntity crystal : mc.world.getEntitiesByClass(EndCrystalEntity.class,
            mc.player.getBoundingBox().expand(breakRange.get()), e -> true)) {
            if (mc.player.distanceTo(crystal) <= breakRange.get()) {
                mc.interactionManager.attackEntity(mc.player, crystal);
                mc.player.swingHand(Hand.MAIN_HAND);
                return;
            }
        }

        // Place crystals
        placeTimer++;
        if (placeTimer < placeDelay.get()) return;
        placeTimer = 0;

        PlayerEntity target = getNearestPlayer(placeRange.get());
        if (target == null) return;

        if (autoSwitch.get()) switchToCrystals();
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) return;

        BlockPos targetPos = BlockPos.ofFloored(target.getX(), target.getY() - 1, target.getZ());

        for (BlockPos pos : new BlockPos[]{targetPos, targetPos.north(), targetPos.south(), targetPos.east(), targetPos.west()}) {
            if (!mc.world.getBlockState(pos).isOf(Blocks.OBSIDIAN) && !mc.world.getBlockState(pos).isOf(Blocks.BEDROCK)) continue;
            BlockPos above = pos.up();
            if (!mc.world.getBlockState(above).isAir()) continue;
            if (mc.player.squaredDistanceTo(above.getX(), above.getY(), above.getZ()) > placeRange.get() * placeRange.get()) continue;

            BlockHitResult hit = new BlockHitResult(new Vec3d(above.getX() + 0.5, above.getY(), above.getZ() + 0.5), Direction.UP, pos, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            break;
        }
    }

    private void switchToCrystals() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                mc.player.getInventory().selectedSlot = i;
                return;
            }
        }
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

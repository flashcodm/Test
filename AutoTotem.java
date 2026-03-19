package com.eriox.pvpaddon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.MinecraftClient;

public class AutoTotem extends Module {
    private final MinecraftClient mc = MinecraftClient.getInstance();

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> healthThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("health-threshold")
        .description("HP below which to force totem in offhand.")
        .defaultValue(10)
        .min(1)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> alwaysTotem = sgGeneral.add(new BoolSetting.Builder()
        .name("always-totem")
        .description("Always keep totem in offhand regardless of HP.")
        .defaultValue(true)
        .build()
    );

    public AutoTotem() {
        super(ErioxCategory.CATEGORY, "AutoTotem", "Keeps a totem of undying in your offhand automatically.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        boolean offhandHasTotem = mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING;
        if (offhandHasTotem) return;

        float hp = mc.player.getHealth();
        boolean shouldEquip = alwaysTotem.get() || hp <= healthThreshold.get();
        if (!shouldEquip) return;

        int totemSlot = -1;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }

        if (totemSlot == -1) return;

        // Move totem to offhand via inventory screen click
        if (mc.currentScreen == null) {
            mc.player.getInventory().selectedSlot = totemSlot < 9 ? totemSlot : mc.player.getInventory().selectedSlot;
        }

        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            totemSlot < 9 ? totemSlot + 36 : totemSlot,
            0,
            SlotActionType.PICKUP,
            mc.player
        );
        mc.interactionManager.clickSlot(
            mc.player.currentScreenHandler.syncId,
            45, // offhand slot
            0,
            SlotActionType.PICKUP,
            mc.player
        );
    }
}

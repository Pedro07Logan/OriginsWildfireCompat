package com.logan.originswildfirecompat;

import com.wildfire.main.GenderPlayer;
import com.wildfire.main.WildfireGender;
import com.wildfire.main.networking.WildfireSync;
import io.github.edwinmindcraft.origins.api.capabilities.IOriginContainer;
import io.github.edwinmindcraft.origins.api.registry.OriginsDynamicRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

@Mod(OriginsWildfireCompat.MOD_ID)
public class OriginsWildfireCompat {
    public static final String MOD_ID = "originswildfirecompat";
    private static final ResourceLocation GENDER_LAYER_ID = new ResourceLocation(MOD_ID, "gender");
    private static final ResourceLocation FEMALE_ID = new ResourceLocation(MOD_ID, "female");
    private static final ResourceLocation MALE_ID = new ResourceLocation(MOD_ID, "male");

    public OriginsWildfireCompat() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!event.side.isServer()) {
            return;
        }
        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        IOriginContainer.get(serverPlayer).ifPresent(container -> {
            ResourceKey<io.github.edwinmindcraft.origins.api.origin.OriginLayer> layerKey = ResourceKey.create(OriginsDynamicRegistries.LAYERS_REGISTRY, GENDER_LAYER_ID);
            ResourceKey<io.github.edwinmindcraft.origins.api.origin.Origin> current = container.getOrigin(layerKey);
            if (current == null) {
                resetScale(serverPlayer);
                return;
            }
            ResourceLocation currentId = current.location();
            if (FEMALE_ID.equals(currentId)) {
                applyScale(serverPlayer, 0.93F);
                applyGender(serverPlayer, GenderPlayer.Gender.FEMALE);
            } else if (MALE_ID.equals(currentId)) {
                applyScale(serverPlayer, 1.05F);
                applyGender(serverPlayer, GenderPlayer.Gender.MALE);
            } else {
                resetScale(serverPlayer);
            }
        });
    }

    private static void applyScale(ServerPlayer player, float target) {
        ScaleData data = ScaleTypes.HEIGHT.getScaleData(player);
        if (Math.abs(data.getTargetScale() - target) > 0.001F) {
            data.setTargetScale(target);
            data.setScale(target);
            data.markForSync(true);
        }
    }

    private static void resetScale(ServerPlayer player) {
        ScaleData data = ScaleTypes.HEIGHT.getScaleData(player);
        if (Math.abs(data.getTargetScale() - 1.0F) > 0.001F) {
            data.resetScale();
            data.markForSync(true);
        }
    }

    private static void applyGender(ServerPlayer player, GenderPlayer.Gender desired) {
        GenderPlayer gp = WildfireGender.getOrAddPlayerById(player.getUUID());
        if (gp.getGender() != desired) {
            gp.updateGender(desired);
            if (desired == GenderPlayer.Gender.FEMALE) {
                gp.updateHurtSounds(true);
                if (gp.getBustSize() < 0.1F) {
                    gp.updateBustSize(0.6F);
                }
            }
            WildfireSync.sendToClient(player, gp);
            WildfireSync.sendToOtherClients(player, gp);
        }
    }
}

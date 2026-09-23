package com.logan.originswildfirecompat;

import com.wildfire.api.WildfireAPI;
import com.wildfire.main.GenderPlayer;
import com.wildfire.main.networking.PacketSync;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class OriginsWildfireCompatFabric implements ModInitializer {
    public static final String MOD_ID = "originswildfirecompat";
    private static final Identifier GENDER_LAYER_ID = new Identifier(MOD_ID, "gender");
    private static final Identifier FEMALE_ID = new Identifier(MOD_ID, "female");
    private static final Identifier MALE_ID = new Identifier(MOD_ID, "male");

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        OriginComponent component = ModComponents.ORIGIN.get(player);
        OriginLayer layer = null;
        for (OriginLayer candidate : component.getOrigins().keySet()) {
            if (GENDER_LAYER_ID.equals(candidate.getIdentifier())) {
                layer = candidate;
                break;
            }
        }
        if (layer == null) {
            resetScale(player);
            return;
        }
        Origin current = component.getOrigin(layer);
        if (current == null || current == Origin.EMPTY) {
            resetScale(player);
            return;
        }
        Identifier currentId = current.getIdentifier();
        if (FEMALE_ID.equals(currentId)) {
            applyScale(player, 0.93F);
            applyGender(player, GenderPlayer.Gender.FEMALE);
        } else if (MALE_ID.equals(currentId)) {
            applyScale(player, 1.05F);
            applyGender(player, GenderPlayer.Gender.MALE);
        } else {
            resetScale(player);
        }
    }

    private static void applyScale(ServerPlayerEntity player, float target) {
        ScaleData data = ScaleTypes.HEIGHT.getScaleData(player);
        if (Math.abs(data.getTargetScale() - target) > 0.001F) {
            data.setTargetScale(target);
            data.setScale(target);
            data.markForSync(true);
        }
    }

    private static void resetScale(ServerPlayerEntity player) {
        ScaleData data = ScaleTypes.HEIGHT.getScaleData(player);
        if (Math.abs(data.getTargetScale() - 1.0F) > 0.001F) {
            data.resetScale();
            data.markForSync(true);
        }
    }

    private static void applyGender(ServerPlayerEntity player, GenderPlayer.Gender desired) {
        GenderPlayer gp = WildfireAPI.getPlayerById(player.getUuid());
        if (gp == null) {
            return;
        }
        if (gp.getGender() != desired) {
            gp.updateGender(desired);
            if (desired == GenderPlayer.Gender.FEMALE) {
                gp.updateHurtSounds(true);
                if (gp.getBustSize() < 0.1F) {
                    gp.updateBustSize(0.6F);
                }
            }
            PacketSync.sendToOthers(player, gp);
        }
    }
}

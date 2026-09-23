package com.logan.originswildfirecompat;

import com.wildfire.api.WildfireAPI;
import com.wildfire.main.Gender;
import com.wildfire.main.entitydata.PlayerConfig;
import com.wildfire.main.networking.WildfireSync;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class OriginsWildfireCompatFabric implements ModInitializer {
    public static final String MOD_ID = "originswildfirecompat";
    private static final Identifier GENDER_LAYER_ID = Identifier.of(MOD_ID, "gender");
    private static final Identifier FEMALE_ID = Identifier.of(MOD_ID, "female");
    private static final Identifier MALE_ID = Identifier.of(MOD_ID, "male");

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        Origin current = null;
        for (java.util.Map.Entry<OriginLayer, Origin> entry : Origin.get(player).entrySet()) {
            if (GENDER_LAYER_ID.equals(entry.getKey().getId())) {
                current = entry.getValue();
                break;
            }
        }
        if (current == null || current == Origin.EMPTY) {
            resetScale(player);
            return;
        }
        Identifier currentId = current.getId();
        if (FEMALE_ID.equals(currentId)) {
            applyScale(player, 0.93F);
            applyGender(player, Gender.FEMALE);
        } else if (MALE_ID.equals(currentId)) {
            applyScale(player, 1.05F);
            applyGender(player, Gender.MALE);
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

    private static void applyGender(ServerPlayerEntity player, Gender desired) {
        PlayerConfig cfg = WildfireAPI.getPlayerById(player.getUuid());
        if (cfg == null) {
            return;
        }
        if (cfg.getGender() != desired) {
            cfg.updateGender(desired);
            if (desired == Gender.FEMALE) {
                cfg.updateHurtSounds(true);
                if (cfg.getBustSize() < 0.1F) {
                    cfg.updateBustSize(0.6F);
                }
            }
            WildfireSync.sendToAllClients(player, cfg);
        }
    }
}

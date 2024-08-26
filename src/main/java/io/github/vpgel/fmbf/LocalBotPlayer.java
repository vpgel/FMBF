package io.github.vpgel.fmbf;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.stats.StatsCounter;

public class LocalBotPlayer extends LocalPlayer {
    private FMBF mod;

    public LocalBotPlayer(FMBF mod, Minecraft instance, ClientLevel level, ClientPacketListener connection,
            StatsCounter stats, ClientRecipeBook recipeBook, boolean wasShiftKeyDown, boolean wasSprinting) {
        super(instance, level, connection, stats, recipeBook, wasShiftKeyDown, wasSprinting);
        this.mod = mod;
    }

    @Override
    public void tick() {
        if (mod.session != null) {
            if (mod.session.isReady()) {
                
            }
        }
    }
    
}

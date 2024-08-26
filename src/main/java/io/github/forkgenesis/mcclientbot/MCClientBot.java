package io.github.forkgenesis.mcclientbot;

import java.io.IOException;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(MCClientBot.modid)
public class MCClientBot {
    public static final String modid = "mcclientbot";
    public static final Logger logger = LogUtils.getLogger();
    Session session = null;

    public MCClientBot() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onLogIn);
        MinecraftForge.EVENT_BUS.addListener(this::onLogOut);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public void onLogIn(PlayerLoggedInEvent event) {
        try {
            session = new Session();
            session.start();
        } catch (IOException e) {
            logger.error(String.format("ClientBot can't reach the server at %s:%d, continuing without it. If you believe the issue was resolved, reconnect to the world/server.", Config.host, Config.port));
            session = null;
        }
    }

    public void onLogOut(PlayerLoggedOutEvent event) {
        if (session != null)
            session.interrupt();
    }
}

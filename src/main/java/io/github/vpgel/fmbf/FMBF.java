package io.github.vpgel.fmbf;

import java.io.IOException;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(FMBF.modid)
public class FMBF {
    public static final String modid = "fmbf";
    public static final Logger logger = LogUtils.getLogger();
    public Session session = null;
    public static Minecraft instance = Minecraft.getInstance();
    private static Mode mode = Mode.OFFLINE;

    public FMBF() {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.addListener(this::onLogIn);
        MinecraftForge.EVENT_BUS.addListener(this::onLogOut);
        MinecraftForge.EVENT_BUS.addListener(this::onTick);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public void onLogIn(PlayerLoggedInEvent event) {
        mode = Mode.START;
    }

    public void onLogOut(PlayerLoggedOutEvent event) {
        mode = Mode.STOP;
    }

    public void onTick(TickEvent.ClientTickEvent event) {
        if (instance.player != null && mode == Mode.OFFLINE) {
            mode = Mode.START;
        }
        if (instance.player == null && mode == Mode.ONLINE) {
            mode = Mode.STOP;
        }

        if (mode == Mode.START && instance.player != null) {
            try {
                session = new Session();
                session.start();
            } catch (IOException e) {
                logger.error(String.format("FMBF can't reach the server at %s:%d, continuing without it. If you believe the issue was resolved, reconnect to the world/server.", Config.ip, Config.port));
                session = null;
            }
            mode = Mode.ONLINE;
        } else if (mode == Mode.STOP) {
            if (session != null)
                session.interrupt();
            mode = Mode.OFFLINE;
        }
    }

    private enum Mode {
        OFFLINE, START, ONLINE, STOP
    };
}

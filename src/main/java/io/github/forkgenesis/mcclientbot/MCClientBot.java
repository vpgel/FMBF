package io.github.forkgenesis.mcclientbot;

import java.io.IOException;
import java.net.UnknownHostException;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(MCClientBot.modid)
public class MCClientBot {
    public static final String modid = "mcclientbot";
    public static final Logger logger = LogUtils.getLogger();
    Session session;

    public MCClientBot() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLogIn(PlayerLoggedInEvent event) throws UnknownHostException, IOException {
        logger.info("starting");
        session = new Session();//event.getEntity().getServer().getPlayerList().getPlayer(event.getEntity().getUUID()));
        session.start();
    }

    @SubscribeEvent
    public void onLogOut(PlayerLoggedOutEvent event) {
        logger.info("ending");
        session.interrupt();
    }
}

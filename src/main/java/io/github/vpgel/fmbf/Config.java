package io.github.vpgel.fmbf;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Конфиг мода: хост (127.0.0.1 по умолчанию) и порт (2323 по умолчанию)
 */
@Mod.EventBusSubscriber(modid=FMBF.modid, bus=Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<String> IP = configBuilder
            .comment("Hostname (IP) for the socket server")
            .define("ip", "127.0.0.1");

    private static final ForgeConfigSpec.IntValue PORT = configBuilder
            .comment("Port for the socket server")
            .defineInRange("port", 2323, 0, 65535);

    static final ForgeConfigSpec SPEC = configBuilder.build();

    public static String ip;
    public static int port;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        ip = IP.get();
        port = PORT.get();
    }
}

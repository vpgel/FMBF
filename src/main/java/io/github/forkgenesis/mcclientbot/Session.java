package io.github.forkgenesis.mcclientbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Session extends Thread {
    Minecraft instance;
    Socket connection;
    BufferedReader in;
    BufferedWriter out;
    boolean running;
    
    /**
     * Стадия 1: инициализация
     */
    public Session() throws UnknownHostException, IOException {
        instance = Minecraft.getInstance();
        connection = new Socket("127.0.0.1", 2323);
        in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
        out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
        MCClientBot.logger.info("started");
    }

    /**
     * Стадия 2: исполнение и стадия 3: завершение
     */
    @Override
    public void run() {

        String request, response;
        try {
            // Пока поток не попросят прерваться, он выполняет следующие 4 действия на повторе:
            while (!this.isInterrupted()) {
                if (instance.player != null) {
                    request = getSurroundingData();  // 1. Засунуть данные об окружении игрока в строку
                    MCClientBot.logger.info("request "+request);
                    out.write(request);              // 2. Отослать эти данные серверу Python
                    out.flush();
                    response = in.readLine();        // 3. Получить ответную команду сервера Python
                    MCClientBot.logger.info("response "+response);
                    controlPlayer(response);         // 4. Выполнить действия, связанные с командой
                }
            }
            in.close();
            out.close();
            connection.close();
            MCClientBot.logger.info("ended");
        } catch (IOException e) {
            // Сервер Питона неожиданно обрывает соединение
            this.interrupt();
        }

    }

    /**
     * Получение данных с мира
     */
    public String getSurroundingData() {
        String data = "";
        // Здесь получить данные о рядом находящихся блоках и сущностях и записать их в data
        BlockPos playerPos = instance.player.getOnPos();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = playerPos.offset(x, y, z);
                    BlockState blockState = instance.player.level().getBlockState(pos);
                    data += (pos.toString()+":"+blockState.getBlock().getDescriptionId()+"\n");
                }
            }
        }

        for (Entity entity: instance.player.level().getEntitiesOfClass(Entity.class, instance.player.getBoundingBox().inflate(5))) {
            data += ("Тип: "+entity.getName().getString()+", координаты: "+entity.getPosition(0)+"\n");
        }

        data += ".\n";

        return data;
    }
    /**
     * Выполнение команды
     * @param command - команда
     */
    public void controlPlayer(String command) {
        switch (command) {
            case "move_forward" -> {
                instance.player.move(MoverType.PLAYER, new Vec3(1, 0, 0));
            }
            case "move_backward" -> {
                instance.player.move(MoverType.PLAYER, new Vec3(-1, 0, 0));
            }
            case "move_left" -> {
                instance.player.move(MoverType.PLAYER, new Vec3(0, 0, -1));
            }
            case "move_right" -> {
                instance.player.move(MoverType.PLAYER, new Vec3(0, 0, 1));
            }
            case "turn_right" -> {
                instance.player.turn(10, 0);
            }
            default -> {
                MCClientBot.logger.error("Unknown command: " + command);
            }
        }
    }
}

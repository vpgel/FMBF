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

/**
 * Разновидность {@link Thread}, управляющая сокет-соединением между клиентской инстанцией Minecraft и сервером.
 */
public class Session extends Thread {
    private Minecraft instance;
    private Socket connection;
    private BufferedReader in;
    private BufferedWriter out;
    
    /**
     * Стадия 1: инициализация
     */
    public Session() throws UnknownHostException, IOException {
        instance = Minecraft.getInstance();
        connection = new Socket(Config.host, Config.port);
        in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-16BE"));
        out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-16BE"));
        MCClientBot.logger.info(String.format("Connected to %s:%d", Config.host, Config.port));
    }

    /**
     * Стадия 2: исполнение и стадия 3: завершение
     */
    @Override
    public void run() {
        try {
            Thread.sleep(1000);

            String request = getSurroundingData(), response = null;
            // Отправить первые данные
            request = getSurroundingData();
            out.write(request.length());
            out.flush();
            out.write(request);
            out.flush();
            System.out.println(String.format("Sending data to server: %s, with the size: %d characters", request, request.length()));
            
            int len = in.read();

            // Пока поток не попросят прерваться, он выполняет следующие 4 действия на повторе:
            while (!this.isInterrupted() && len != -1) {
                // Получить команду
                char[] responseBuffer = new char[len];
                in.read(responseBuffer, 0, len);
                response = String.valueOf(responseBuffer);
                System.out.println(String.format("Received a response from server: %s, with the size: %d characters", request, len));
                
                // Выполнить действия, связанные с командой
                controlPlayer(response);

                // Отправить данные
                request = getSurroundingData();
                out.write(request.length());
                out.flush();
                out.write(request);
                out.flush();
                System.out.println(String.format("Sending data to server: %s, with the size: %d characters", request, request.length()));

                len = in.read();
            }
            MCClientBot.logger.info("Aborting connection");
            connection.close();
        } catch (IOException e) {
            // Сервер Питона неожиданно обрывает соединение
            this.interrupt();
        } catch (InterruptedException e) {
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

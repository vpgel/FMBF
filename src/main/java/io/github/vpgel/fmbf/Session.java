package io.github.vpgel.fmbf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Разновидность {@link Thread}, управляющая сокет-соединением между клиентской инстанцией Minecraft и сервером.
 */
public class Session extends Thread {
    private Socket connection;
    private BufferedReader in;
    private BufferedWriter out;
    //public FMBF mod;
    private String request, response;
    private boolean ready;

    public LocalPlayer player() {
        return FMBF.instance.player;
    }
    public boolean isReady() {
        this.ready = !this.ready;
        return !this.ready;
    }
    public String getResponse() {
        return this.response;
    }
    
    /**
     * Стадия 1: инициализация
     */
    public Session() throws UnknownHostException, IOException {
        connection = new Socket(Config.ip, Config.port);
        in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-16BE"));
        out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-16BE"));
        FMBF.logger.info(String.format("Connected to %s:%d", Config.ip, Config.port));
        ready = false;
    }

    /**
     * Стадия 2: исполнение и стадия 3: завершение
     */
    @Override
    public void run() {
        try {
            Thread.sleep(1000);

            request = Json.createObjectBuilder().add("name", player().getName().getString()).build().toString();

            // Отправить первые данные
            out.write(request.length());// длинна сообщения
            out.flush();
            out.write(request);//само сообщение
            out.flush();
            System.out.println(String.format("Sending data to server: %s, with the size: %d characters", request, request.length()));// просто принт
            
            int len = in.read();// читаем ответ (длинну)

            // Пока поток не попросят прерваться, он выполняет следующие 4 действия на повторе:
            while (!this.isInterrupted() && len != -1) {
                // Получить команду
                char[] responseBuffer = new char[len];// создаём массив
                in.read(responseBuffer, 0, len); // читаем и  записываем ответ в массив
                response = String.valueOf(responseBuffer);// перевод массива в строку response (нвдо)
                System.out.println(String.format("Received a response from server: %s, with the size: %d characters", response, len));
                
                // Выполнить действия, связанные с командой
                this.ready = !this.ready;// хз лол
                //нужно действие,например походить

                if (Integer.valueOf(response) == 1) {
                    request = get_playerLook();
                   //request = getSurroundingData_Block(1);// функция получения данных от майна
                }

                /*
                // Отправить данные
                try {
                    request = getSurroundingData();// функция получения данных от майна
                } catch (NullPointerException e) {
                    Thread.sleep(3000);
                    request = getSurroundingData();
                }

                 */
                out.write(request.length());//аналогично отправляем данные на сервак
                out.flush();
                out.write(request);
                out.flush();
                System.out.println(String.format("Sending data to server: %s, with the size: %d characters", request, request.length()));

                len = in.read();// повторяем цикл заново
            }
            FMBF.logger.info("Aborting connection");
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
    public String getSurroundingData_Block(int xi) {

        // Этот JSON-объект - данные об окружающем мире и состоянии бота, которые сконвертируются в String в конце функции.
        JsonObjectBuilder data = Json.createObjectBuilder();
        JsonArrayBuilder blocksData = Json.createArrayBuilder();

        // Здесь получить данные о рядом находящихся блоках и сущностях и записать их в data
        BlockPos playerPos = player().getOnPos();

        for (int x = -xi; x <= xi; x++) {
            for (int y = -xi; y <= xi; y++) {
                for (int z = -xi; z <= xi; z++) {
                    JsonObjectBuilder block = Json.createObjectBuilder();
                    BlockPos pos = playerPos.offset(x, y, z);
                    String id = player().level().getBlockState(pos).getBlock().getDescriptionId();
                    //data += (pos.toString()+":"+blockState.getBlock().getDescriptionId()+"\n");
                    block.add("id", id).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ());
                    blocksData.add(block.build());
                }
            }
        }
        data.add("blocks", blocksData.build());

        return data.build().toString();
    }
    // получаем координаты
    public String get_playerPos() {
        // Этот JSON-объект - данные об окружающем мире и состоянии бота, которые сконвертируются в String в конце функции.
        JsonObjectBuilder blocksData = Json.createObjectBuilder();


        BlockPos playerPos = player().getOnPos();// получаем позицию игрока
        blocksData.add("x", playerPos.getX()).add("y", playerPos.getY()).add("z", playerPos.getZ());
        return blocksData.build().toString();
    }

    // получаем направление взгляда
    public String get_playerDirection() {
        // Этот JSON-объект - данные об окружающем мире и состоянии бота, которые сконвертируются в String в конце функции.
        JsonObjectBuilder blocksData = Json.createObjectBuilder();


        float playerPosX = player().getViewXRot(0);// получаем направление взгляда
        float playerPosY = player().getViewYRot(0);
        blocksData.add("x", playerPosX).add("y", playerPosY);
        return blocksData.build().toString();
    }

    // получаем блок/энтити по направлению взгляда
    public String get_playerLook() {
        // Этот JSON-объект - данные об окружающем мире и состоянии бота, которые сконвертируются в String в конце функции.
        JsonObjectBuilder blocksData = Json.createObjectBuilder();

        Entity lookat_entity = Minecraft.getInstance().crosshairPickEntity;
        HitResult hitResult = Minecraft.getInstance().getCameraEntity().pick(20.0D, 0.0F, false);


        if(lookat_entity != null) {
            blocksData.add("Object", "entity").add("Type", String.valueOf((Object) BuiltInRegistries.ENTITY_TYPE.getKey(lookat_entity.getType())))
                    .add("distance", lookat_entity.distanceTo(player()));
        }


        else if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockState lookat_block = Minecraft.getInstance().level.getBlockState(((BlockHitResult) hitResult).getBlockPos());

            blocksData.add("Object", "block").add("Type", String.valueOf((Object)BuiltInRegistries.BLOCK.getKey(lookat_block.getBlock())) )
                    .add("distance",Math.sqrt(player().distanceToSqr(((BlockHitResult)hitResult).getBlockPos().getCenter())));
        }


        else {
        blocksData.add("Object", "air");
    }
        return blocksData.build().toString();
    }

    public String getSurroundingData() {

        // Этот JSON-объект - данные об окружающем мире и состоянии бота, которые сконвертируются в String в конце функции.
        JsonObjectBuilder data = Json.createObjectBuilder();
        JsonArrayBuilder blocksData = Json.createArrayBuilder();

        // Здесь получить данные о рядом находящихся блоках и сущностях и записать их в data
        BlockPos playerPos = player().getOnPos();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    JsonObjectBuilder block = Json.createObjectBuilder();
                    BlockPos pos = playerPos.offset(x, y, z);
                    String id = player().level().getBlockState(pos).getBlock().getDescriptionId();
                    //data += (pos.toString()+":"+blockState.getBlock().getDescriptionId()+"\n");
                    block.add("id", id).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ());
                    blocksData.add(block.build());
                }
            }
        }
        data.add("blocks", blocksData.build());

        JsonArrayBuilder entitiesData = Json.createArrayBuilder();

        for (Entity entity: player().level().getEntitiesOfClass(Entity.class, player().getBoundingBox().inflate(5))) {
            Vec3 pos = entity.getPosition(0);
            JsonObjectBuilder entityData = Json.createObjectBuilder();
            entityData.add("id", entity.getName().getString()).add("x", pos.x).add("y", pos.y).add("z", pos.z);
            entitiesData.add(entityData.build());
        }
        data.add("entities", entitiesData.build());

        return data.build().toString();
    }
    
    /**
     * Выполнение команды
     * @param message - команда и аргументы
     */
    public void controlPlayer(String message) {
        String command = message.split(" ")[0];
        String[] args = new String[0];
        if (message.split(" ").length == 2) {
            args = message.split(" ")[1].split(",");
        }
        switch (command) {
            case "walk_forward" -> {
                //player().setDeltaMovement(player().getEyePosition());
                player().zza = 1.0F;
            }
            case "turn_right" -> {
                player().turn(Integer.valueOf(args[0]), 0);
            }
            default -> {
                FMBF.logger.error("Unknown command: " + command);
            }
        }
    }
}

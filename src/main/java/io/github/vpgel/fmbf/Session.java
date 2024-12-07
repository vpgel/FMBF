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

/*import baritone.api.BaritoneAPI;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalGetToBlock;
import net.minecraft.ChatFormatting;*/
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Разновидность {@link Thread}, управляющая сокет-соединением между клиентской инстанцией Minecraft и сервером.
 * Работа с классом вот так выглядит:
 * <pre> {@code
 * Session session = new Session(); session.start();
 * } </pre>
 */
public class Session extends Thread {
    /** Сетевой сокет - обличие соединения между Minecraft и Python. */
    private Socket connection;
    /** Поток получения данных из сервера Python в клиент Minecraft. */
    private BufferedReader in;
    /** Поток отправки данных из клиента Minecraft в сервер Python. */
    private BufferedWriter out; 
    private String request, response;

    /** Сам клиент Minecraft. */
    private Minecraft minecraft = Minecraft.getInstance();

    /** Эта функция возвращает объект игрока, к которому привязана текущая инстанция Minecraft. */
    private LocalPlayer player() {
        return FMBF.instance.player;
    }


    /**
     * Инициализирует разновидность {@link Thread}, т.е. отдельный поток, управляющий сокет-соединением между клиентской инстанцией Minecraft и сервером. Эта функция инициализирует поток, соединение и открывает порт сервера на прослушивание и на чтение.
     * <br></br>
     * Чтобы запустить поток, выполните метод {@code start()} созданного объекта.
     */
    public Session() throws UnknownHostException, IOException {
        connection = new Socket(Config.ip, Config.port);
        in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-16BE"));
        out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-16BE"));
        FMBF.logger.info(String.format("Connected to %s:%d", Config.ip, Config.port));
    }

    /**
     * Отправляет строку в сервер Python.
     * @param data - строка на отправку
     */
    private void sendResponse(String data) throws IOException {
        out.write(data.length()); // Отправка длины сообщения
        out.flush();
        out.write(data);          // Отправка самого сообщения
        out.flush();
    }

    /**
     * Жизнь потока - всё, что в нём происходит.
     */
    @Override
    public void run() {
        try {
            // Хз зачем задержка перед началом работы, потом удалю, наверно.
            Thread.sleep(1000);

            // Отправка первых данных - никнейма игрока. Это обязательно, так как сервер ожидает именно получить строку вида {"name":"<username>"}, где <username> - сам никнейм.
            String name = Json.createObjectBuilder().add("name", player().getName().getString()).build().toString();
            sendResponse(name);

            if (Config.debug)
                FMBF.logger.debug(String.format("Sending data to server: %s, with the size: %d characters", name, name.length())); // Просто принт.
            
            /** Длина запроса. */
            int len = in.read();

            // Это главный цикл работы программы - <b>цикл запроса-ответа</b>.
            // Он бесконечно пытается получить с сервера Python запрос, 
            // потом формирует в зависимости от него ответ,
            // потом отправляет его.

            // Он прервётся, если а) игрок выйдет из мира, б) закроется соединение с сервером Python.
            while (!this.isInterrupted() && len != -1) {
                
                // Получение запроса, в трёх частях:
                char[] requestBuffer = new char[len];    // Создание массива для запроса.
                in.read(requestBuffer, 0, len);      // Чтение и запись запроса в массив.
                request = String.valueOf(requestBuffer); // Конвертация массива в строку.

                if (Config.debug)
                    FMBF.logger.debug(String.format("Received a request from server: %s, with the size: %d characters", request, len));
                
                // На этом этапе в переменной request находится запрос. В зависимости от него формируется ответ в переменной response.
                response = switch (request) {
                    case "спи" -> {
                        yield idle();
                    }
                    case "какие блоки рядом" -> {
                        yield getNearestBlocks(4);
                    }
                    case "какие сущности рядом" -> {
                        yield getNearestEntities(5);
                    }
                    case "на что смотришь" -> {
                        yield getWhatImLookingAt(50.0D, true);
                    }
                    case "где ты" -> {
                        yield getPlayerPos();
                    }
                    case "куда смотришь" -> {
                        yield getPlayerDirection();
                    }
                    default -> {
                        yield defaultData(request);
                    }
                };

                // Аналогично, отправляем данные на сервак.
                sendResponse(response);
                if (Config.debug)
                    FMBF.logger.debug(String.format("Sending data to server: %s, with the size: %d characters", response, response.length()));

                len = in.read(); // Повторяем цикл заново.
            }
            // Эта часть происходит при нормальном завершении работы сервера.
            FMBF.logger.info("Aborting connection");
            connection.close();

        // А дальше - обработка ошибок, которые могут выйти при работе мода.
        } catch (IOException e) {
            // Эта ошибка возникает при обрыве соединения. Решение? Просто завершить работу этой сессии.
            // Перезапустить сессию можно, просто перезайдя на мир/сервер.
            this.interrupt();
        } catch (InterruptedException e) {
            // Та ж фигня, как и с предыдущей ошибкой.
            this.interrupt();
        }

    }

    /**
     * Пустая команда
     * @return
     */
    private String idle() {
        JsonObjectBuilder data = Json.createObjectBuilder();
        data.add("ctx","спи");
        data.add("code", 0);
        return data.build().toString();
    }

    /**
     * Получение блоков вокруг игрока
     * 
     * @param r - кубический радиус получения блоков, т.е. от точки (-r, -r, -r) до точки (r, r, r)
     */
    private String getNearestBlocks(int r) {

        // Это JSON-объект - словарь вида "ключ-значение", который можно сконвертировать в строку.
        JsonObjectBuilder data = Json.createObjectBuilder();
        // data == {}

        // Это JSON-массив - массив значений, который можно добавить как значение в JSON-объект.
        JsonArrayBuilder blocksData = Json.createArrayBuilder();
        // blocksData = []

        // playerPos - координаты блока, на котором стоит игрок (или над которым летает).
        BlockPos playerPos = player().getOnPos();

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {
                    // Этот словарь тоже создаётся пустым: {}
                    JsonObjectBuilder block = Json.createObjectBuilder(); 

                    // Далее мы получаем позицию какого-то блока.
                    BlockPos pos = playerPos.offset(x, y, z);
                    // Его название.
                    String id = player().level().getBlockState(pos).getBlock().getDescriptionId();

                    // И добавляем их как данные в словарь block.
                    block.add("id", id).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ());
                    // block == {"id": "minecraft:stone", "x": 10, "y": 60, "z": 10}

                    blocksData.add(block.build());
                    // Этот массив хранит все словари block, что были созданы в циклах for.
                    // blocksData == [block, block2, ...]
                }
            }
        }
        data.add("blocks", blocksData.build());
        // Теперь data выглядит так:
        // data == {"blocks": blocksData}
        // То есть,
        // data == {"blocks": [block, block2, ...]}
        // То есть,
        // data == {"blocks": [{"id": "minecraft:stone", "x": 10, "y": 60, "z": 10},
        //                     {"id": "minecraft:stone", "x": 11, "y": 60, "z": 10},
        //                     ...]}

        data.add("ctx","какие блоки рядом");
        data.add("code", 0);
        return data.build().toString();
    }

    /**
     * Получение сущностей вокруг игрока
     * 
     * @param r - параллелепипедный радиус получения сущностей, т.е. от точки (-r, -r, -r) до точки (r, r+2, r)
     */
    private String getNearestEntities(int r) {
        JsonObjectBuilder data = Json.createObjectBuilder();
        // data == {}
        JsonArrayBuilder entitiesData = Json.createArrayBuilder();
        // entitiesData == []

        for (Entity entity: player().level().getEntitiesOfClass(Entity.class, player().getBoundingBox().expandTowards(r, r, r))) {
            JsonObjectBuilder entityData = Json.createObjectBuilder();
            // entityData == {}

            /* Координаты сущности. */
            Vec3 pos = entity.getPosition(0);
            entityData.add("id", entity.getName().getString()).add("x", pos.x).add("y", pos.y).add("z", pos.z);
            // entityData == {"id": "minecraft:creeper", "x": 10, "y": 60, "z": 10}

            entitiesData.add(entityData.build());
            // entitiesData == [entityData, entityData2, ...]
        }
        data.add("entities", entitiesData.build());
        // Теперь data выглядит так:
        // data == {"entitites": entitiesData}
        // То есть,
        // data == {"entitites": [entityData, entityData2, ...]}
        // То есть,
        // data == {"entitites": [{"id": "minecraft:creeper", "x": 10, "y": 60, "z": 10},
        //                     {"id": "minecraft:creeper", "x": 13, "y": 61, "z": 7},
        //                     ...]}

        data.add("ctx","какие сущности рядом");
        data.add("code", 0);
        return data.build().toString();
    }

    /**
     * Получаем блок/жидкость/энтити по направлению взгляда.
     * @param distance - максимальное расстояние, на которое игрок может увидеть что-то. Чем выше, тем больше лагов.
     * @param enableFluids - 
     */
    private String getWhatImLookingAt(double distance, boolean enableFluids) {
        JsonObjectBuilder data = Json.createObjectBuilder();

        // Сущность, на которую смотрит курсор игрока. Важно! Эта переменная равна null, если игрок не смотрит на сущность.
        Entity entity = minecraft.crosshairPickEntity;

        // Из глаз игрока выходит луч длиной в несколько блоков, а в переменные block и liquid записывается то, во что первым попал этот луч. Может быть жидкостью или блоком.
        HitResult block = minecraft.getCameraEntity().pick(distance, 0.0F, false);
        HitResult liquid = minecraft.getCameraEntity().pick(distance, 0.0F, true);

        // Ну теперь есть четыре варианта событий.

        // Первый: мы попали в сущность.
        if(entity != null) {
            data.add("type", "entity")
                .add("id", String.valueOf((Object)ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())))
                .add("distance", entity.distanceTo(player()));

        // Второй: мы попали в жидкость. Если опция жидкостей выключена, то мы забиваем на это.
        } else if (liquid.getType() == HitResult.Type.BLOCK && enableFluids) {
            BlockPos blockPos = ((BlockHitResult)liquid).getBlockPos();

            FluidState fluidstate = minecraft.level.getFluidState(blockPos);
            data.add("type", "fluid")
                .add("id", String.valueOf((Object)ForgeRegistries.FLUIDS.getKey(fluidstate.getType())))
                .add("distance",Math.sqrt(player().distanceToSqr(blockPos.getCenter())));
    
        // Третий: мы попали в блок.
        } else if (block.getType() == HitResult.Type.BLOCK) {
            BlockPos blockPos = ((BlockHitResult)liquid).getBlockPos();
            BlockState blockState = minecraft.level.getBlockState(blockPos);

            data.add("type", "block")
                .add("id", String.valueOf((Object)ForgeRegistries.BLOCKS.getKey(blockState.getBlock())))
                .add("distance",Math.sqrt(player().distanceToSqr(blockPos.getCenter())));

        // Четвёртый: мы попали в воздух.
        } else {
            data.add("object", "air");
        }

        data.add("ctx","на что смотришь");
        data.add("code", 0);
        return data.build().toString();
    }

    /**
     * Получение координат блока под игроком.
     */
    private String getPlayerPos() {
        JsonObjectBuilder posData = Json.createObjectBuilder();

        BlockPos playerPos = player().getOnPos(); // Это и есть координаты блока под игроком.

        posData.add("x", playerPos.getX()).add("y", playerPos.getY()).add("z", playerPos.getZ());
        // posData == {"x": 10, "y": 60, "z": 10}
        posData.add("ctx","где ты");
        posData.add("code", 0);
        return posData.build().toString();
    }

    /**
     * Получение направления взгляда игрока.
     */
    private String getPlayerDirection() {
        JsonObjectBuilder directionData = Json.createObjectBuilder();

        float playerPosX = player().getViewXRot(0); // Это и есть направление взгляда.
        float playerPosY = player().getViewYRot(0);

        directionData.add("x", playerPosX).add("y", playerPosY);
        // directionData == {"x": 10.23, "y": 23.15}
        directionData.add("ctx","куда смотришь");
        directionData.add("code", 0);
        return directionData.build().toString();
    }

    private String defaultData(String command) {
        JsonObjectBuilder data = Json.createObjectBuilder();
        data.add("ctx",command);
        data.add("code", 1);
        return data.build().toString();
    }

    // TODO
    /*private String walk(int x, int y, int z) {
        player().move(MoverType.PLAYER, player().getEyePosition());
        BaritoneAPI.getProvider().getBaritoneForPlayer(player()).getCustomGoalProcess().setGoal(new GoalGetToBlock(player().getOnPos().offset(x, y, z)));

        JsonObjectBuilder walkData = Json.createObjectBuilder();
        return walkData.build().toString();
    }*/

    /**
     * Отправление сообщения в игровой чат. Эта функция реально тут просто так
     */
    private String chat(String message) {
        JsonObjectBuilder data = Json.createObjectBuilder();
        data.add("ctx","напиши");
        data.add("code", 1);
        return data.build().toString();
    }
}

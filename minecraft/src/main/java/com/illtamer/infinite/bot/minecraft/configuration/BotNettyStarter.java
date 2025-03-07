package com.illtamer.infinite.bot.minecraft.configuration;

import com.illtamer.infinite.bot.api.config.CQHttpWebSocketConfiguration;
import com.illtamer.infinite.bot.minecraft.api.EventExecutor;
import com.illtamer.infinite.bot.minecraft.configuration.config.BotConfiguration;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BotNettyStarter extends JavaPlugin {

    @Getter
    protected static BotNettyStarter instance;

    protected ThreadPoolExecutor websocketExecutor = new ThreadPoolExecutor(1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>());

    private final Consumer<ThreadPoolExecutor> interruptConsumer;

    @Override
    public void onLoad() {
        instance = this;
        BotConfiguration.load(instance);
        connect();
    }

    /**
     * 开启 WebSocket 连接
     * */
    public void connect() {
        interruptConsumer.accept(websocketExecutor);
        final BotConfiguration.ConnectionConfig connection = BotConfiguration.connection;
        websocketExecutor.execute(new WebSocketRunner(connection));
    }

    protected void checkConnection() {
        if (CQHttpWebSocketConfiguration.isRunning()) {
            getLogger().info("账号连接成功");
        } else {
            getLogger().warning("账号连接失败，请检查控制台输出处理，或等待自动重连");
        }
    }

    protected void close() {
        websocketExecutor.shutdown();
        getLogger().info("WebSocket 连接关闭 " + (websocketExecutor.isShutdown() ? "成功" : "失败"));
    }

    {
        Consumer<ThreadPoolExecutor> consumer;
        try {
            final Field field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            field.setAccessible(true);
            MethodHandles.Lookup IMPL_LOOKUP = (MethodHandles.Lookup) field.get(null);
            final MethodHandle methodHandle = IMPL_LOOKUP
                    .findVirtual(ThreadPoolExecutor.class, "interruptWorkers", MethodType.methodType(void.class));
            consumer = object -> {
                try {
                    methodHandle.invoke(object);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            };
        } catch (Exception e) {
            consumer = object -> {}; // do nothing
            e.printStackTrace();
        }
        this.interruptConsumer = consumer;
    }

    private class WebSocketRunner implements Runnable {

        private final BotConfiguration.ConnectionConfig connection;

        private WebSocketRunner(BotConfiguration.ConnectionConfig connection) {
            this.connection = connection;
        }

        @Override
        public void run() {
            try {
                CQHttpWebSocketConfiguration.start(
                        String.format("http://%s:%d", connection.host, connection.httpPort),
                        String.format("ws://%s:%d", connection.host, connection.websocketPort),
                        connection.authorization,
                        EventExecutor::dispatchListener
                );
            } catch (InterruptedException ignore) {
                // do nothing
            } catch (Exception e) {
                e.printStackTrace();
            }
            getLogger().info("WebSocket 连接已关闭");
        }

    }

}

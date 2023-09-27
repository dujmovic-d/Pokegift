package io.github.polymeta.pokegift;

import io.github.polymeta.pokegift.commands.Gift;
import io.github.polymeta.pokegift.configuration.BaseConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


public class Pokegift {
    public static final String MOD_ID = "pokegift";
    public static final MiniMessage miniMessage = MiniMessage.miniMessage();

    public static BaseConfig config;
    public static ScheduledThreadPoolExecutor scheduler;
    public static ForkJoinPool worker;

    private static final Logger logger = LogManager.getLogger();

    public static void init() {
        logger.info("Pokegift by Polymeta starting up!");
        scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName("Pokegift Thread");
            return thread;
        });
        scheduler.setRemoveOnCancelPolicy(true);
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        worker = new ForkJoinPool(16, new WorkerThreadFactory(), new ExceptionHandler(), false);
        //load config and pool and message configuration
        loadConfig();

        CommandRegistrationCallback.EVENT.register((dispatcher, registry, selection) -> {
            Gift.register(dispatcher);
        });
    }

    private static void loadConfig() {
        var configFile = new File("config/pokegift/main.json");
        configFile.getParentFile().mkdirs();

        // Check config existence and load if it exists, otherwise create default.
        if (configFile.exists()) {
            try {
                var fileReader = new FileReader(configFile);
                config = BaseConfig.GSON.fromJson(fileReader, BaseConfig.class);
                fileReader.close();
            } catch (Exception e) {
                logger.error("Failed to load the config! Using default config as fallback");
                e.printStackTrace();
                config = new BaseConfig();
            }

        } else {
            config = new BaseConfig();
        }

        saveConfig();
    }

    private static void saveConfig() {
        try {
            var configFile = new File("config/pokegift/main.json");
            var fileWriter = new FileWriter(configFile);
            BaseConfig.GSON.toJson(config, fileWriter);
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            logger.error("Failed to save the config!");
            e.printStackTrace();
        }
    }

    private static final class WorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private static final AtomicInteger COUNT = new AtomicInteger(0);

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setDaemon(true);
            thread.setName("Pokegift Worker - " + COUNT.getAndIncrement());
            thread.setContextClassLoader(Pokegift.class.getClassLoader());
            return thread;
        }
    }

    private static final class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            logger.error("Thread " + t.getName() + " threw an uncaught exception");
            e.printStackTrace();
        }
    }
}

package de.derioo;


import de.derioo.bot.DiscordBot;
import de.derioo.config.local.LangConfig;
import de.derioo.config.local.LocalConfig;
import de.derioo.status.StatusHandler;
import eu.koboo.en2do.Credentials;
import eu.koboo.en2do.MongoManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        File file = new File(".", "support.mp3");
        try (FileOutputStream stream = new FileOutputStream(file);
             InputStream resource = Main.class.getClassLoader().getResourceAsStream("support.mp3")
        ) {
            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = resource.read(buffer)) > 0) {
                stream.write(buffer, 0, readBytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        File configFile = new File(".", "credentials.json");
        File langFile = new File(".", "lang.json");
        LocalConfig config = configFile.exists() ? LocalConfig.load(configFile) : LocalConfig.loadByENV();
        LangConfig langConfig = langFile.exists() ? LangConfig.load(langFile) : LangConfig.load(ClassLoader.getSystemClassLoader().getResourceAsStream("lang.json"));
        MongoManager mongoManager = new MongoManager(Credentials.of(config.getConnectionString(), config.getDb()));


        DiscordBot bot = new DiscordBot(config, mongoManager, langConfig);
        new StatusHandler(bot);
    }
}
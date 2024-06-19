package de.derioo;


import de.derioo.bot.DiscordBot;
import de.derioo.config.local.LangConfig;
import de.derioo.config.local.LocalConfig;
import eu.koboo.en2do.Credentials;
import eu.koboo.en2do.MongoManager;

import java.io.File;
import java.io.IOException;
import java.util.TimeZone;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        File configFile = new File(".", "credentials.json");
        File langFile = new File(".", "lang.json");
        LocalConfig config = configFile.exists() ? LocalConfig.load(configFile) : LocalConfig.loadByENV();
        LangConfig langConfig = langFile.exists() ? LangConfig.load(langFile) : LangConfig.load(ClassLoader.getSystemClassLoader().getResourceAsStream("lang.json"));
        MongoManager mongoManager = new MongoManager(Credentials.of(config.getConnectionString(), config.getDb()));

        new DiscordBot(config, mongoManager, langConfig);
    }
}
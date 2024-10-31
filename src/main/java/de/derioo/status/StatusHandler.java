package de.derioo.status;

import de.derioo.bot.DiscordBot;
import io.javalin.Javalin;

public class StatusHandler {

    public StatusHandler(DiscordBot bot) {
        new Thread(() -> {
            String port = System.getenv("API_PORT");
            Javalin.create()
                    .get("/ping", ctx -> {
                        if (bot == null) {
                            ctx.result("Pong! (Sadge)");
                            return;
                        }
                        Long time = bot.getJda().getRestPing().complete();
                        ctx.result("Pong! (" + time + "ms)");
                    })
                    .start(port != null ? Integer.parseInt(port) : 8080);
        }).start();
    }
}

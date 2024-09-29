package de.derioo.module.predefined.statuschanger;

import com.mongodb.client.model.geojson.LineString;
import de.derioo.bot.DiscordBot;
import de.derioo.javautils.common.MathUtility;
import de.derioo.module.Module;
import net.dv8tion.jda.api.entities.Activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class StatusChangerModule extends Module {

    private static final List<String> statuses = new ArrayList<>(List.of("Bedrock & Java", "Varilx.de Botsystem", "Tube-Hosting.DE", "Developed by Dario :)", "Varilx.de"));

    private final DiscordBot bot;

    public StatusChangerModule(DiscordBot bot) {
        super(bot, "statuschanger");
        this.bot = bot;
        setTimers(3_000L, this::changeStatus);
    }

    public void changeStatus() {
        Collections.shuffle(statuses);
        bot.getJda().getPresence().setActivity(Activity.of(Activity.ActivityType.PLAYING, statuses.getFirst()));
    }



}

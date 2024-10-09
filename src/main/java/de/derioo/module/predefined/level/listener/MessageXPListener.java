package de.derioo.module.predefined.level.listener;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.module.predefined.level.LevelModule;
import de.derioo.module.predefined.level.db.LevelPlayerData;
import de.derioo.module.predefined.level.db.LevelPlayerDataRepo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageXPListener {

    private final Map<User, List<Message>> lastMessages = new HashMap<>();

    private final LevelPlayerDataRepo repo;
    private final LevelModule module;


    public MessageXPListener(LevelPlayerDataRepo repo, LevelModule module) {
        this.repo = repo;
        this.module = module;

    }

    @ModuleListener
    public void onMessage(MessageReceivedEvent event) {
        if (!event.getChannelType().isGuild()) return;
        if (event.getAuthor().isBot()) return;
        lastMessages.putIfAbsent(event.getAuthor(), new ArrayList<>());

        LevelPlayerData data = module.getPlayerData(event.getGuild(), event.getAuthor());
        LevelPlayerData.Stats stats = data.getStats();
        String rawContent = event.getMessage().getContentRaw().toLowerCase().trim().strip();
        String message = event.getMessage().getContentDisplay().strip();
        int words = message.split("\\s").length;
        int chars = message.trim().replace(" ", "").length();
        int level = module.getLevelCount(data);

        List<Message> messages = lastMessages.get(event.getAuthor());

        boolean isSimilar = false;

        for (Message value : messages) {
            String currentRawContent = value.getContentRaw().toLowerCase().trim().strip();
            if (currentRawContent.equalsIgnoreCase(rawContent)) isSimilar = true;
            if (currentRawContent.startsWith(rawContent)) isSimilar = true;
            if (rawContent.startsWith(currentRawContent)) isSimilar = true;
        }

        if (lastMessages.get(event.getAuthor()).size() > 6) {
            lastMessages.get(event.getAuthor()).clear();
        }

        lastMessages.get(event.getAuthor()).add(event.getMessage());


        if (isSimilar) {
            return;
        }


        int xp = 45;
        xp += Math.min(words * 5, 30);
        xp += Math.min(chars, 50);
        stats.setXp(stats.getXp() + xp);

        int newLevel = module.getLevelCount(data);
        if (level != newLevel) {
            this.module.sendNewLevelMessage(event.getMember(), event.getGuild(), newLevel);
        }

        this.repo.save(data);
    }


}

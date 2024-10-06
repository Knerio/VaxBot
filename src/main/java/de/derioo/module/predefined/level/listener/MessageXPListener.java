package de.derioo.module.predefined.level.listener;

import de.derioo.annotations.ModuleListener;
import de.derioo.module.predefined.level.LevelModule;
import de.derioo.module.predefined.level.db.LevelPlayerData;
import de.derioo.module.predefined.level.db.LevelPlayerDataRepo;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageXPListener {

    private final LevelPlayerDataRepo repo;
    private final LevelModule module;


    public MessageXPListener(LevelPlayerDataRepo repo, LevelModule module) {
        this.repo = repo;
        this.module = module;

    }

    @ModuleListener
    public void onMessage(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        LevelPlayerData data = module.getPlayerData(event.getGuild(), event.getAuthor());
        LevelPlayerData.Stats.MessageStats stats = data.getStats().getMessageStats();
        String message = event.getMessage().getContentDisplay().strip();
        int words = message.split("\\s").length;
        int chars = message.trim().replace(" ", "").length();

        stats.setMessageCount(stats.getMessageCount() + 1);
        stats.setWords(stats.getMessageCount() + words);
        stats.setChars(stats.getChars() + chars);

        int xp = 45;
        xp += Math.min(words * 5, 30);
        xp += Math.min(chars, 50);
        stats.setXp(stats.getXp() + xp);


        this.repo.save(data);
    }


}

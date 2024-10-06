package de.derioo.module.predefined.level;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.module.Module;
import de.derioo.module.predefined.level.db.LevelPlayerData;
import de.derioo.module.predefined.level.db.LevelPlayerDataRepo;
import de.derioo.module.predefined.level.listener.MessageXPListener;
import de.derioo.module.predefined.level.listener.VoiceXPListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.util.logging.Level;

public class LevelModule extends Module {

    private final LevelPlayerDataRepo repo;

    public LevelModule(DiscordBot bot) {
        super(bot, "level-system");
        this.repo = (LevelPlayerDataRepo) bot.getRepo(LevelPlayerDataRepo.class);
        this.registerListener(new VoiceXPListener(this.repo, this));
        this.registerListener(new MessageXPListener(this.repo, this));
    }

    public LevelPlayerData getPlayerData(GenericGuildVoiceEvent event) {
        return getPlayerData(event.getGuild(), event.getMember());
    }

    public LevelPlayerData getPlayerData(Guild guild, Member member) {
        return getPlayerData(guild, member.getUser());
    }

    public LevelPlayerData getPlayerData(Guild guild, User user) {
        if (!this.repo.existsById(user.getId() + ":" + guild.getId())) {
            this.repo.save(LevelPlayerData.builder()
                    .id(user.getId() + ":" + guild.getId())
                    .stats(LevelPlayerData.Stats.builder()
                            .messageStats(LevelPlayerData.Stats.MessageStats.builder()
                                    .chars(0)
                                    .words(0)
                                    .messageCount(0)
                                    .build())
                            .voiceStats(LevelPlayerData.Stats.VoiceStats.builder()
                                    .activeVoiceChannelId(-1)
                                    .totalTime(0)
                                    .voiceJoins(0)
                                    .voiceChannelJoinTimestamp(-1)
                                    .build())
                            .build())
                    .build());
        }
        return this.repo.findFirstById(user.getId() + ":" + guild.getId());
    }




}

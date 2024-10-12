package de.derioo.module.predefined.level.listener;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.module.predefined.level.LevelModule;
import de.derioo.module.predefined.level.db.LevelPlayerData;
import de.derioo.module.predefined.level.db.LevelPlayerDataRepo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class VoiceXPListener {

    private final LevelPlayerDataRepo repo;
    private final LevelModule module;

    public VoiceXPListener(LevelPlayerDataRepo repo, LevelModule module) {
        this.repo = repo;
        this.module = module;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (LevelPlayerData data : repo.findAll()) {
                    Guild guild = module.getBot().getJda().getGuildById(data.getId().split(":")[1]);
                    Member member = guild.getMemberById(data.getId().split(":")[0]);
                    if (member == null) return;
                    GuildVoiceState state = member.getVoiceState();
                    if (!state.inAudioChannel() || state.isDeafened() || state.isMuted()) {
                        stopVoiceMeasuring(data, data.getStats().getVoiceStats(), member);
                    }
                    if (!state.isMuted() && !state.isDeafened() && state.inAudioChannel()) {
                        startVoiceMeasuring(data, data.getStats().getVoiceStats(), member);
                    }
                }
            }
        }, 0, 25_000);
    }


    @ModuleListener
    public void voiceChangedListener(GuildVoiceUpdateEvent event) {
        LevelPlayerData data = module.getPlayerData(event);
        LevelPlayerData.Stats.VoiceStats stats = data.getStats().getVoiceStats();
        if (event.getChannelJoined() != null && event.getChannelLeft() != null) {
            startVoiceMeasuring(data, stats, event.getMember());
            return;
        }
        if (event.getChannelJoined() != null) {
            startVoiceMeasuring(data, stats, event.getMember());
            return;
        }
        if (event.getChannelLeft() != null) {
            stopVoiceMeasuring(data, stats, event.getMember());
        }
    }

    @ModuleListener
    public void deafenListener(GuildVoiceDeafenEvent event) {
        LevelPlayerData data = module.getPlayerData(event);
        LevelPlayerData.Stats.VoiceStats stats = data.getStats().getVoiceStats();
        if (event.isDeafened()) {
            stopVoiceMeasuring(data, stats, event.getMember());
            return;
        }
        startVoiceMeasuring(data, stats, event.getMember());
    }

    @ModuleListener
    public void muteListener(GuildVoiceMuteEvent event) {
        LevelPlayerData data = module.getPlayerData(event);
        LevelPlayerData.Stats.VoiceStats stats = data.getStats().getVoiceStats();
        if (event.isMuted()) {
            stopVoiceMeasuring(data, stats, event.getMember());
            return;
        }
        startVoiceMeasuring(data, stats, event.getMember());
    }


    public void startVoiceMeasuring(LevelPlayerData data, LevelPlayerData.Stats.VoiceStats stats, Member member) {
        if (stats.getVoiceChannelJoinTimestamp() != -1) {
            stats.setTotalTime(System.currentTimeMillis() - stats.getVoiceChannelJoinTimestamp() + stats.getTotalTime());
            saveVoiceStats(data, stats, member);
        }
        stats.setVoiceChannelJoinTimestamp(System.currentTimeMillis());
        this.repo.save(data);
    }

    private void saveVoiceStats(LevelPlayerData data, LevelPlayerData.Stats.VoiceStats stats, Member member) {
        if (stats.getVoiceChannelJoinTimestamp() == -1) return;
        long time = System.currentTimeMillis() - stats.getVoiceChannelJoinTimestamp();

        int level = this.module.getLevelCount(data);
        int xp = (int) (((double) time / 60_000D) * 30);

        data.getStats().setXp(data.getStats().getXp() + xp);

        int newLevel = this.module.getLevelCount(data);
        if (level != newLevel) {
            this.module.sendNewLevelMessage(member, member.getGuild(), newLevel);
        }

        this.repo.save(data);
    }

    public void stopVoiceMeasuring(LevelPlayerData data, LevelPlayerData.Stats.VoiceStats stats, Member member) {
        if (stats.getVoiceChannelJoinTimestamp() != -1) {
            stats.setTotalTime(System.currentTimeMillis() - stats.getVoiceChannelJoinTimestamp() + stats.getTotalTime());
            saveVoiceStats(data, stats, member);
        }
        stats.setVoiceChannelJoinTimestamp(-1);
        this.repo.save(data);
    }

}

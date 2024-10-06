package de.derioo.module.predefined.level.listener;

import de.derioo.annotations.ModuleListener;
import de.derioo.module.predefined.level.LevelModule;
import de.derioo.module.predefined.level.db.LevelPlayerData;
import de.derioo.module.predefined.level.db.LevelPlayerDataRepo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

import java.util.Timer;
import java.util.TimerTask;

public class VoiceXPListener {

    private final LevelPlayerDataRepo repo;
    private final LevelModule module;

    public VoiceXPListener(LevelPlayerDataRepo repo, LevelModule module) {
        this.repo = repo;
        this.module = module;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                for (Guild guild : module.getBot().getJda().getGuilds()) {
                    for (VoiceChannel voiceChannel : guild.getVoiceChannels()) {
                        for (Member member : voiceChannel.getMembers()) {
                            LevelPlayerData playerData = module.getPlayerData(member.getGuild(), member);
                            onChannelJoin(playerData, playerData.getStats().getVoiceStats(), voiceChannel.getIdLong(), false);
                        }
                    }
                }
            }
        }, 25_000, 25_000);
    }


    @ModuleListener
    public void voiceChangedListener(GuildVoiceUpdateEvent event) {
        LevelPlayerData data = module.getPlayerData(event);
        LevelPlayerData.Stats.VoiceStats stats = data.getStats().getVoiceStats();
        if (event.getChannelJoined() != null && event.getChannelLeft() != null) {
            onChannelJoin(data, stats, event.getChannelJoined().getIdLong(), true);
            return;
        }
        if (event.getChannelJoined() != null) {
            onChannelJoin(data, stats, event.getChannelJoined().getIdLong(), true);
            return;
        }
        if (event.getChannelLeft() != null) {
            onChannelLeave(data, stats, event);
        }

    }

    private void onChannelLeave(LevelPlayerData data, LevelPlayerData.Stats.VoiceStats stats, GuildVoiceUpdateEvent event) {
        if (stats.getActiveVoiceChannelId() != -1) {
            stats.setTotalTime(System.currentTimeMillis() - stats.getVoiceChannelJoinTimestamp() + stats.getTotalTime());
        }
        stats.setActiveVoiceChannelId(-1);
        stats.setVoiceChannelJoinTimestamp(-1);
        this.repo.save(data);
    }

    public void onChannelJoin(LevelPlayerData data, LevelPlayerData.Stats.VoiceStats stats, Long channelId, boolean voiceJoin) {
        if (stats.getActiveVoiceChannelId() != -1) {
            stats.setTotalTime(System.currentTimeMillis() - stats.getVoiceChannelJoinTimestamp() + stats.getTotalTime());
            if (voiceJoin) stats.setVoiceJoins(stats.getVoiceJoins() + 1);
        }
        stats.setVoiceChannelJoinTimestamp(System.currentTimeMillis());
        stats.setActiveVoiceChannelId(channelId);
        this.repo.save(data);
    }

}

package de.derioo.module.predefined.support;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import de.derioo.Main;
import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.Module;
import de.derioo.utils.UserUtils;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.sound.midi.VoiceStatus;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.List;

public class SupportModule extends Module {

    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private final TrackSheduler trackScheduler;

    public SupportModule(DiscordBot bot) {
        super(bot, "support");
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        this.player = playerManager.createPlayer();
        this.trackScheduler = new TrackSheduler(player);
        player.addListener(trackScheduler);

        AudioEventAdapter listener = new AudioEventAdapter() {
            @Override
            public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
                for (Guild guild : bot.getJda().getGuilds()) {
                    Long channelId = bot.get(guild).getChannels().get(Config.Id.Channel.SUPPORT_CHANNEL.name());
                    VoiceChannel channel = guild.getVoiceChannelById(channelId);
                    List<Member> members = guild.getVoiceChannelById(channelId).getMembers();
                    if (members.size() == 1) {
                        guild.getAudioManager().closeAudioConnection();
                        return;
                    }
                    startTrack(channel);
                }
            }
        };

        player.addListener(listener);
    }

    private void startTrack(VoiceChannel channel) {
        Guild guild = channel.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        if (!audioManager.isConnected()) {
            audioManager.openAudioConnection(channel);
        }

        audioManager.setSendingHandler(new AudioPlayerSendHandler(player));


        playerManager.loadItem(new File(".", "support.mp3").getPath(), new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                trackScheduler.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                for (AudioTrack track : playlist.getTracks()) {
                    trackScheduler.queue(track);
                }
            }

            @Override
            public void noMatches() {
            }

            @Override
            public void loadFailed(FriendlyException exception) {
            }
        });


    }


    @ModuleListener
    public void onJoin(GuildVoiceUpdateEvent event) {
        ConfigData data = bot.get(event.getGuild());
        AudioChannelUnion channelJoined = event.getChannelJoined();
        AudioChannelUnion channelLeft = event.getChannelLeft();
        Long supportChannelId = data.getChannels().get(Config.Id.Channel.SUPPORT_CHANNEL.name());

        AudioChannelUnion channel = event.getChannelJoined() == null ? event.getChannelLeft() : event.getChannelJoined();
        if (channel.getIdLong() != supportChannelId) return;
        if (event.getMember().getUser().isBot()) return;

        if (channelJoined != null) {
            startTrack(channelJoined.asVoiceChannel());
            Long channelId = data.getChannels().get(Config.Id.Channel.TEAM_CHAT.name());
            event.getGuild().getTextChannelById(channelId).sendMessage(data.getMentions(Config.Id.Role.SUPPORT_PING, event.getGuild())).addEmbeds(new EmbedBuilder()
                    .setTitle("Spieler im Support wartet")
                    .setColor(Color.GREEN)
                    .setDescription(UserUtils.getMention(event.getMember()) + " wartet im " + event.getChannelJoined().getAsMention())
                    .build()).queue();
        }
    }
}

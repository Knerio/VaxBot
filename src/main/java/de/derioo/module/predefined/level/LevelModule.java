package de.derioo.module.predefined.level;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.module.Module;
import de.derioo.module.predefined.level.db.LevelPlayerData;
import de.derioo.module.predefined.level.db.LevelPlayerDataRepo;
import de.derioo.module.predefined.level.listener.MessageXPListener;
import de.derioo.module.predefined.level.listener.VoiceXPListener;
import de.derioo.utils.Emote;
import eu.koboo.en2do.repository.methods.sort.Sort;
import lombok.Getter;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import org.bson.types.ObjectId;

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

public class LevelModule extends Module {

    @Getter
    private final DiscordBot bot;
    private final LevelPlayerDataRepo repo;

    public LevelModule(DiscordBot bot) {
        super(bot, "level-system");
        this.bot = bot;
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
        if (this.repo.existsByUserIdAndGuildId(user.getId(), guild.getId())) {
            return this.repo.findFirstByUserIdAndGuildId(user.getId(), guild.getId());
        }
        LevelPlayerData data = LevelPlayerData.builder()
                .id(new ObjectId())
                .userId(user.getId())
                .guildId(guild.getId())
                .stats(LevelPlayerData.Stats.builder()
                        .xp(0)
                        .voiceStats(LevelPlayerData.Stats.VoiceStats.builder()
                                .totalTime(0)
                                .voiceChannelJoinTimestamp(-1)
                                .build())
                        .build())
                .build();
        this.repo.save(data);

        return data;
    }

    public Integer getVoiceRank(LevelPlayerData data, Guild guild) {
        List<LevelPlayerData> list = this.repo.findManyByGuildId(guild.getId())
                .stream()
                .sorted(Comparator.comparingLong(o -> ((LevelPlayerData) o).getStats().getVoiceStats().getLifeTotalTime()).reversed())
                .toList();

        return list.indexOf(data) + 1;
    }

    public Integer getMessageRank(LevelPlayerData data, Guild guild) {
        List<LevelPlayerData> list = this.repo.findManyByGuildId(guild.getId())
                .stream()
                .sorted(Comparator.comparingLong(o -> ((LevelPlayerData) o).getStats().getXp()).reversed())
                .toList();

        return list.indexOf(data) + 1;
    }

    public String getPercentage(LevelPlayerData data) {
        long xp = getXP(data);
        int maxXP = getMaxXP(data);

        if (maxXP == 0) return "0%";

        double percentage = ((double) xp / maxXP) * 100;
        return String.format("%.2f%%", percentage);
    }

    public Integer getMaxXP(LevelPlayerData data) {
        int level = getLevelCount(data);
        return calculateMaxXPForLevel(level);
    }

    public Long getXP(LevelPlayerData data) {
        long xp = data.getStats().getXp();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            long neededXP = calculateMaxXPForLevel(i);
            xp -= neededXP;
            if (xp < 0) return xp + neededXP;
        }
        return -1L;
    }

    public String getProgressBar(LevelPlayerData data) {
        long xp = getXP(data);
        int maxXP = getMaxXP(data);

        int totalBars = 12;
        int filledBars = (int) ((double) xp / maxXP * totalBars);

        StringBuilder bar = new StringBuilder();

        if (filledBars == 0) {
            bar.append(Emote.PROGRESS_LEFT_0.getData());
        } else {
            bar.append(Emote.PROGRESS_LEFT_1.getData());
        }
        bar.append(Emote.PROGRESS_MID_1.getData().repeat(Math.max(filledBars - 2, 0)))
                .append(Emote.PROGRESS_MID_0.getData().repeat(Math.max(totalBars - filledBars - 2, 0)));
        if (filledBars == totalBars) {
            bar.append(Emote.PROGRESS_RIGHT_1.getData());
        } else {
            bar.append(Emote.PROGRESS_RIGHT_0.getData());
        }


        return bar.toString();
    }

    public Integer getLevelCount(LevelPlayerData data) {
        long xp = data.getStats().getXp();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            int neededXP = calculateMaxXPForLevel(i);
            xp -= neededXP;

            if (xp < 0) return i;
        }
        return 1;
    }

    public Integer calculateMaxXPForLevel(int level) {
        return (int) (1000 * Math.pow(1.2, level));
    }


    public void sendNewLevelMessage(Member member, Guild guild, int newLevel) {
        if (member.equals(guild.getSelfMember())) return;
        if (newLevel > 20) {
            for (Role roleObject : bot.get(guild).getRoleObjects(Config.Id.Role.LEVEL_20_ROLE, guild)) {
                guild.addRoleToMember(member, roleObject);
            }
        }
        if (newLevel > 30) {
            for (Role roleObject : bot.get(guild).getRoleObjects(Config.Id.Role.LEVEL_30_ROLE, guild)) {
                guild.addRoleToMember(member, roleObject);
            }
        }
        if (newLevel > 50) {
            for (Role roleObject : bot.get(guild).getRoleObjects(Config.Id.Role.LEVEL_50_ROLE, guild)) {
                guild.addRoleToMember(member, roleObject);
            }
        }
        member.getUser().openPrivateChannel().complete().sendMessage(member.getUser().getAsMention())
                .addEmbeds(DiscordBot.Default.builder()
                        .setColor(Color.GREEN)
                        .setTitle("Du bist nun Level " + newLevel + " auf " + guild.getName())
                        .setDescription("Nutze /level auf " + guild.getName() + " für mehr Infos")
                        .setImage("https://cdn.discordapp.com/attachments/1067809862744019025/1295256040627245148/Level_p.png?ex=670dfcb0&is=670cab30&hm=fb09796c5d1dfcbc7607cbf4dafd4ef14b5b5eac08b77712fe4afa6bcc0bd403&")
                        .build()).queue();
    }

    public Long getUserAmount() {
        return this.repo.countAll();
    }
}

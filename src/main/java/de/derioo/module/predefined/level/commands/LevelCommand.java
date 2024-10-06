package de.derioo.module.predefined.level.commands;

import de.derioo.annotations.NeedsAdmin;
import de.derioo.bot.DiscordBot;
import de.derioo.module.predefined.level.LevelModule;
import de.derioo.module.predefined.level.db.LevelPlayerData;
import de.derioo.module.predefined.level.db.LevelPlayerDataRepo;
import de.derioo.utils.Emote;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.logging.Level;

@Command(name = "level")
@Description("Give info about a user´s level")
public class LevelCommand {

    private final DiscordBot bot;
    private final LevelModule module;
    private final LevelPlayerDataRepo repo;

    public LevelCommand(DiscordBot bot, LevelModule module) {
        this.bot = bot;
        this.module = module;
        this.repo = (LevelPlayerDataRepo) bot.getRepo(LevelPlayerDataRepo.class);
    }

    @Execute
    public void getLevel(@Arg("nutzer") @Description("Nutzer (oder du)") Optional<User> optionalUser,
                         @Context SlashCommandInteractionEvent event) {
        Timer timer = new Timer();

        if (optionalUser.isEmpty()) optionalUser = Optional.of(event.getUser());
        Member member = event.getGuild().getMember(optionalUser.get());
        event.replyEmbeds(generateLevelEmbed(member))
                .setEphemeral(true).queue(hook -> {
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (hook.isExpired()) {
                                timer.cancel();
                                return;
                            }
                            MessageEmbed embed = hook.retrieveOriginal().complete().getEmbeds().getFirst();
                            if (embed.equals(generateLevelEmbed(member))) return;

                            hook.editOriginalEmbeds(generateLevelEmbed(member)).queue();
                        }
                    }, 1500, 1500);
                });
    }

    public MessageEmbed generateLevelEmbed(Member member) {
        LevelPlayerData data = this.module.getPlayerData(member.getGuild(), member);

        LevelPlayerData.Stats.VoiceStats stats = data.getStats().getVoiceStats();
        long joinDiff = stats.getTotalTime() + (stats.getVoiceChannelJoinTimestamp() == -1 ? 0 : (System.currentTimeMillis() - stats.getVoiceChannelJoinTimestamp()));


        long seconds = (joinDiff / 1000) % 60;
        long minutes = (joinDiff / (1000 * 60)) % 60;
        long hours = (joinDiff / (1000 * 60 * 60)) % 24;
        long days = joinDiff / (1000 * 60 * 60 * 24);

        return DiscordBot.Default.builder()
                .addField(String.format("Level %s (#%s)", getLevelCount(data), getMessageRank(data, member.getGuild())),
                        String.format("""
                                %s %s [%s xp / %s xp]
                                """, getProgressBar(data), getPercentage(data), getXP(data), getMaxXP(data)),
                        false)
                .addField(String.format("Sprachchat (#%s)", getVoiceRank(data, member.getGuild())), String.format("""
                        Insgesamte Zeit in Sprachchat: `%s` Tage, `%s` Stunden, `%s` Minuten, `%s` Sekunden
                        """, days, hours, minutes, seconds), false)
                .setColor(Color.GREEN)
                .build();

    }

    private Integer getVoiceRank(LevelPlayerData data, Guild guild) {
        List<LevelPlayerData> list = this.repo.findAll()
                .stream().filter(obj -> obj.getId().split(":")[1].equalsIgnoreCase(guild.getId()))
                .sorted(Comparator.comparingLong(o -> ((LevelPlayerData) o).getStats().getVoiceStats().getLifeTotalTime()).reversed())
                .toList();

        return list.indexOf(data) + 1;
    }

    private Integer getMessageRank(LevelPlayerData data, Guild guild) {
        List<LevelPlayerData> list = this.repo.findAll()
                .stream().filter(obj -> obj.getId().split(":")[1].equalsIgnoreCase(guild.getId()))
                .sorted(Comparator.comparingLong(o -> ((LevelPlayerData) o).getStats().getMessageStats().getXp()).reversed())
                .toList();

        return list.indexOf(data) + 1;

    }

    private String getPercentage(LevelPlayerData data) {
        long xp = getXP(data);
        int maxXP = getMaxXP(data);

        if (maxXP == 0) return "0%";

        double percentage = ((double) xp / maxXP) * 100;
        return String.format("%.2f%%", percentage);
    }

    private Integer getMaxXP(LevelPlayerData data) {
        int level = getLevelCount(data);
        return calculateMaxXPForLevel(level);
    }

    private Long getXP(LevelPlayerData data) {
        long xp = data.getStats().getMessageStats().getXp();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            long neededXP = calculateMaxXPForLevel(i);
            xp -= neededXP;
            if (xp < 0) return xp + neededXP;
        }
        return -1L;
    }

    private String getProgressBar(LevelPlayerData data) {
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

    private Integer getLevelCount(LevelPlayerData data) {
        long xp = data.getStats().getMessageStats().getXp();
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            int neededXP = calculateMaxXPForLevel(i);
            xp -= neededXP;

            if (xp < 0) return i;
        }
        return 1;
    }

    private Integer calculateMaxXPForLevel(int level) {
        return (int) (1000 * Math.pow(1.2, level));
    }


}

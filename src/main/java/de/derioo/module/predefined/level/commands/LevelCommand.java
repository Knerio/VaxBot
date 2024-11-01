package de.derioo.module.predefined.level.commands;

import de.derioo.bot.DiscordBot;
import de.derioo.module.predefined.level.LevelModule;
import de.derioo.module.predefined.level.db.LevelPlayerData;
import de.derioo.module.predefined.level.db.LevelPlayerDataRepo;
import de.derioo.utils.UserUtils;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

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
    public void getLevel(@Arg(value = "nutzer", nullable = true) @Description("Nutzer (oder du)") Optional<Member> optionalMember,
                         @Context SlashCommandInteractionEvent event) {
        Timer timer = new Timer();

        if (optionalMember.isEmpty()) optionalMember = Optional.of(event.getMember());
        Member member = optionalMember.get();
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



        return DiscordBot.Default.builder()
                .addField(String.format("Level %s (#%s / %s)", module.getLevelCount(data), module.getMessageRank(data, member.getGuild()), module.getUserAmount()),
                        String.format("""
                                %s %s [%s xp / %s xp]
                                """, module.getProgressBar(data), module.getPercentage(data), module.getXP(data), module.getMaxXP(data)),
                        false)
                .addField(String.format("Sprachchat (#%s / %s)", module.getVoiceRank(data, member.getGuild()), module.getUserAmount()), String.format("""
                        Insgesamte Zeit in Sprachchat %s:
                        """, stats.getLifeTotalTimeFormatted()), false)
                .setColor(Color.GREEN)
                .build();

    }


}

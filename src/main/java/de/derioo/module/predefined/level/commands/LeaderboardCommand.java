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
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "leaderboard")
@Description("Zeigt dir das aktuelle Leaderboard der Level (/level)")
public class LeaderboardCommand {

    private final DiscordBot bot;
    private final LevelPlayerDataRepo repo;
    private final LevelModule module;


    public LeaderboardCommand(DiscordBot bot, LevelModule module) {
        this.bot = bot;
        this.module = module;
        this.repo = (LevelPlayerDataRepo) this.bot.getRepo(LevelPlayerDataRepo.class);
    }

    @Execute
    public void leaderboard(@Arg("type") @Description("Der Typ des Leaderboards") Type type, @Context SlashCommandInteractionEvent event) {
        LevelPlayerData data = this.repo.findFirstById(event.getUser().getId() + ":" + event.getGuild().getId());
        List<LevelPlayerData> list = this.repo.findAll()
                .stream().filter(obj -> obj.getId().split(":")[1].equalsIgnoreCase(event.getGuild().getId()))
                .sorted(Comparator.comparingLong(o -> switch (type) {
                    case VOICE -> ((LevelPlayerData) o).getStats().getVoiceStats().getLifeTotalTime();
                    case LEVEL -> ((LevelPlayerData) o).getStats().getXp();
                }).reversed())
                .toList();
        EmbedBuilder builder = DiscordBot.Default.builder()
                .setColor(Color.GREEN)
                .setTitle("Leaderboard (" + type.toString().toLowerCase() + ")");

        List<String> leaderboard = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            LevelPlayerData currentData = list.get(i);
            boolean isHe = currentData.equals(data);
            User user = event.getJDA().getUserById(currentData.getId().split(":")[0]);
            String voiceFormatting = type != Type.VOICE ? "``" : " ";
            String string = (isHe ? "**" : "") +
                    (i + 1) + ". " +
                    user.getEffectiveName() +
                    ":" + voiceFormatting + switch (type) {
                case VOICE -> currentData.getStats().getVoiceStats().getLifeTotalTimeFormatted();
                case LEVEL -> "Level " + this.module.getLevelCount(currentData);
            } + voiceFormatting;
            leaderboard.add(string);
        }

        if (list.indexOf(data) > 9) {
            String voiceFormatting = type != Type.VOICE ? "``" : "";
            leaderboard.add("**" + (list.indexOf(data) + 1) + ". " + event.getUser().getEffectiveName() + ": " + voiceFormatting +
                    switch (type) {
                        case VOICE -> data.getStats().getVoiceStats().getLifeTotalTimeFormatted();
                        case LEVEL -> "Level " + this.module.getLevelCount(data);
                    } + voiceFormatting + "**");
        }

        builder.setDescription(String.join("\n", leaderboard));


        event.replyEmbeds(builder.build()).setEphemeral(true).queue();
    }

    public enum Type {

        LEVEL,
        VOICE

    }

}

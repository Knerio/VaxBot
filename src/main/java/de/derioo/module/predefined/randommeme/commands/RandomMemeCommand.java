package de.derioo.module.predefined.randommeme.commands;

import de.derioo.bot.DiscordBot;
import de.derioo.module.predefined.randommeme.db.RandomMeme;
import de.derioo.module.predefined.randommeme.db.RandomMemeRepository;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Command(name = "randommeme")
@Description("Gibt dir ein random Varilx meme")
public class RandomMemeCommand {

    private final DiscordBot bot;
    private final RandomMemeRepository repo;

    public RandomMemeCommand(DiscordBot bot) {
        this.bot = bot;
        this.repo = (RandomMemeRepository) this.bot.getRepo(RandomMemeRepository.class);
    }

    @Execute
    public void randomMeme(@Context SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Pagination pagination = Pagination.of(1).setPage(new Random().nextInt((int) repo.countAll()));
        List<RandomMeme> memes = repo.pageByGuildId(event.getGuild().getIdLong(), pagination);
        Collections.shuffle(memes);

        for (RandomMeme meme : memes) {
            if (!meme.getGuildId().equals(event.getGuild().getIdLong())) continue;
            event.getHook().sendMessageEmbeds(DiscordBot.Default.builder()
                    .setImage("attachment://image." + meme.getFileExtension())
                    .setDescription("Hier ein Random Meme")
                    .setColor(Color.GREEN)
                    .build()).addFiles(FileUpload.fromData(meme.getData().getData(), "image." + meme.getFileExtension())).queue();
            break;
        }
    }
}

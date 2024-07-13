package de.derioo.module.predefined.giveaway.commands;

import de.derioo.annotations.NeedsRole;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.javautils.common.DateUtility;
import de.derioo.module.predefined.giveaway.GiveAwayModule;
import de.derioo.module.predefined.giveaway.db.GiveAway;
import de.derioo.module.predefined.giveaway.db.GiveawayRepo;
import de.derioo.utils.Emote;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bson.types.ObjectId;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Command(name = "giveaway")
public class GiveAwayCommand {

    private final DiscordBot bot;
    private final GiveAwayModule module;


    public GiveAwayCommand(DiscordBot bot, GiveAwayModule module) {
        this.bot = bot;
        this.module = module;
    }

    @Execute
    @NeedsRole(Config.Id.Role.GIVEAWAY_CREATE_ROLE)
    public void execute(@Arg("preis") @Description("Der Preis, welcher verlost wird") String price,
                        @Arg("gewinner") @Description("Wie viele Gewinner soll es geben") Integer winner,
                        @Arg("länge") @Description("Wie lange soll das Giveaway dauern (zb. 12:00 oder 12.03.2024, 12:00)") String duration,
                        @Context SlashCommandInteractionEvent event) throws ParseException {
        Date durationDate = DateUtility.parseDynamic(duration);
        GiveAway giveAway =
                GiveAway.builder()
                        .id(new ObjectId())
                        .creatorId(event.getUser().getIdLong())
                        .guildId(event.getGuild().getIdLong())
                        .channelId(event.getChannel().getIdLong())
                        .winners(new ArrayList<>())
                        .winnersCount(winner)
                        .participants(new ArrayList<>())
                        .reward(price)
                        .duration(durationDate.getTime())
                        .build();

        event.getChannel().sendMessage(bot.get(event.getGuild()).getMentions(Config.Id.Role.GIVEAWAY_PING_ROLE, event.getGuild())
                ).addEmbeds(module.getEmbed(giveAway))
                .addActionRow(Button.success("giveaway-participate", Emote.PARTY_EMOTE.unicode()))
                .queue(message -> {
                    giveAway.setMessageId(message.getIdLong());
                    bot.getRepo(GiveawayRepo.class).save(giveAway);
                });
        event.reply("Erfolgreich erstellt!").setEphemeral(true).queue();

    }

}

package de.derioo.module.predefined.giveaway;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.module.Module;
import de.derioo.module.predefined.giveaway.db.GiveAway;
import de.derioo.module.predefined.giveaway.db.GiveawayRepo;
import de.derioo.utils.Emote;
import eu.koboo.en2do.repository.Repository;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class GiveAwayModule extends Module {

    private final DiscordBot bot;

    public GiveAwayModule(DiscordBot bot) {
        super(bot, "giveaway");
        this.bot = bot;
    }

    @Override
    public void timer() {
        Repository<GiveAway, ObjectId> repo = bot.getRepo(GiveawayRepo.class);
        for (GiveAway giveAway : repo.findAll()) {
            if (System.currentTimeMillis() < giveAway.getDuration()) continue;
            Guild guild = Objects.requireNonNull(bot.getJda().getGuildById(giveAway.getGuildId()));
            TextChannel channel = Objects.requireNonNull(bot.getJda().getTextChannelById(giveAway.getChannelId()));
            Message message = channel.getHistoryAround(giveAway.getMessageId(), 2).complete().getMessageById(giveAway.getMessageId());
            if (message == null) continue;

            for (Long participant : new ArrayList<>(giveAway.getParticipants())) {
                Member member = guild.getMemberById(participant);
                if (member == null) giveAway.getParticipants().remove(participant);
            }

            List<Member> winners = new ArrayList<>();
            Collections.shuffle(giveAway.getParticipants());
            for (int i = 0; i < giveAway.getWinnersCount(); i++) {
                if (giveAway.getParticipants().size() <= i) break;
                winners.add(guild.getMemberById(giveAway.getParticipants().get(i)));
            }
            for (Member winner : winners) {
                winner.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(DiscordBot.Default.builder()
                            .setTitle("Du hast gewonnen")
                            .setDescription("Du hast am Giveaway von " + giveAway.getReward() + " gewonnen! \n" +
                                    "Schau hier " + guild.getTextChannelById(giveAway.getChannelId()).getAsMention() + " für mehr Informationen")
                            .setColor(Color.GREEN)
                            .build()).queue();
                });
            }
            giveAway.setWinners(winners.stream().map(Member::getIdLong).toList());
            message.editMessageEmbeds(getEmbed(giveAway))
                    .setActionRow(new ButtonImpl("id", "", ButtonStyle.SUCCESS, true, Emote.PARTY_EMOTE.unicode()))
                    .queue();
            repo.delete(giveAway);
        }
    }

    @ModuleListener
    public void onButton(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId() == null) return;
        if (!event.getButton().getId().equals("giveaway-participate")) return;
        for (GiveAway giveAway : bot.getRepo(GiveawayRepo.class).findAll()) {
            if (giveAway.getMessageId() != event.getMessageIdLong()) continue;
            if (giveAway.getParticipants().contains(event.getUser().getIdLong())) {
                giveAway.getParticipants().remove(event.getUser().getIdLong());
                event.reply("Du nimmst nun nicht mehr teil").setEphemeral(true).queue();
                event.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(DiscordBot.Default.builder()
                            .setTitle("Du nimmst nun nicht mehr teil")
                            .setDescription("Du nimmst nun nicht mehr am Giveaway von " + giveAway.getReward() + " teil")
                            .setColor(Color.GREEN)
                            .build()).queue();
                });
            } else {
                giveAway.getParticipants().add(event.getUser().getIdLong());
                event.reply("Du nimmst nun teil").setEphemeral(true).queue();
                event.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(DiscordBot.Default.builder()
                            .setTitle("Du nimmst nun teil")
                            .setDescription("Du nimmst nun am Giveaway von " + giveAway.getReward() + " teil")
                            .setColor(Color.GREEN)
                            .build()).queue();
                });
            }
            bot.getRepo(GiveawayRepo.class).save(giveAway);
            event.getMessage().editMessageEmbeds(getEmbed(giveAway)).queue();
            break;
        }
    }

    public @NotNull MessageEmbed getEmbed(@NotNull GiveAway giveAway) {
        StringBuilder builder = new StringBuilder();
        builder.append("- Gestartet von: ").append("<@").append(giveAway.getCreatorId()).append(">").append("\n")
                .append("- Was wird verlost: **").append(giveAway.getReward()).append("**\n");
        if (!giveAway.getWinners().isEmpty()) {
            builder.append("- Gewinner: ").append(giveAway.getWinners().stream().map(id -> "<@" + id + ">").collect(Collectors.joining(","))).append("\n");
        }
        builder.append("- Restzeit: ").append(giveAway.getDuration() < System.currentTimeMillis() ? "**ABGELAUFEN**" : "**<t:" + getUnix(new Date(giveAway.getDuration()).getTime()) + ":R>**").append("\n");
        if (!giveAway.getWinners().isEmpty()) {
            builder.append("- Wurde beendet am: **").append("<t:").append(getUnix(new Date(giveAway.getDuration()).getTime())).append(":R>**").append("\n");
        }
        builder.append("- Teilnehmer: ").append(giveAway.getParticipants().size());
        return DiscordBot.Default.builder()
                .setTitle(Emote.PARTY_EMOTE.getData() + " Giveaway " + Emote.PARTY_EMOTE.getData())
                .setDescription(builder.toString())
                .setColor(Color.GRAY)
                .build();
    }

    @Contract(pure = true)
    private @NotNull Long getUnix(Long l) {
        return l / 1000L;
    }
}

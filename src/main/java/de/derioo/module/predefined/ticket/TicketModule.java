package de.derioo.module.predefined.ticket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.config.local.LangConfig;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.module.Module;
import eu.koboo.en2do.repository.Repository;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.derioo.config.Config.Id.Data.TEAM_ROLE;

public class TicketModule extends Module {

    private final DiscordBot bot;
    private final ConfigRepo repo;

    private final LangConfig langConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TicketManager ticketManager;

    public TicketModule(@NotNull DiscordBot bot, LangConfig langConfig) {
        super(bot, "Ticket");
        this.ticketManager = new TicketManager(bot);
        this.langConfig = langConfig;
        this.bot = bot;
        this.repo = (ConfigRepo) bot.getRepo(ConfigRepo.class);
    }

    @Override
    public void once() throws JsonProcessingException, ExecutionException, InterruptedException {
        Config config = Config.get(repo);
        for (Guild guild : bot.getJda().getGuilds()) {
            if (!config.getData().get(guild.getId()).getChannels().containsKey(Config.Id.Channel.TICKET_CREATION_CHANNEL.name()))
                continue;
            Long channelId = config.getData().get(guild.getId()).getChannels().get(Config.Id.Channel.TICKET_CREATION_CHANNEL.name());
            TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
            List<Message> messages = channel.getHistory().retrievePast(1).complete();
            if (messages.isEmpty()) {
                sendNewTicketMessage(channel);
            } else {
                Message message = messages.getFirst();
                if (message.getAuthor().getIdLong() == bot.getJda().getSelfUser().getIdLong()) {
                    sendNewTicketMessage(channel);
                    message.delete().queue();
                }
            }
        }
    }

    @NotNull
    private MessageEmbed getEmbed() throws JsonProcessingException {
        return EmbedBuilder.fromData(DataObject.fromJson(objectMapper.writeValueAsString(langConfig.getTicket().getTicketCreationEmbed()))).build();
    }

    private void sendNewTicketMessage(@NotNull TextChannel channel) throws JsonProcessingException, ExecutionException, InterruptedException {
        StringSelectMenu.Builder builder = StringSelectMenu.create("new-ticket");

        builder.addOption("Ticket", "ticket", "Ein normales Ticket für normale Supportangelegenheiten!");
        builder.addOption("BugReport", "bug", "Ein Ticket, welches nur für Bugs gedacht ist (Minecraft, Discord, Website,...)");
        builder.setMaxValues(1);
        builder.setMinValues(1);
        channel.sendMessageEmbeds(getEmbed())
                .addActionRow(
                        Button.link("https://tube-hosting.com/pricing", "Partner").withEmoji(Emoji.fromFormatted("<:TubehostingVarilx:1101657813794693120>"))
                )
                .addActionRow(
                        builder.build()
                ).submit().get();
    }

    @ModuleListener
    public void onMenuSelection(StringSelectInteractionEvent event) {
        switch (event.getSelectMenu().getId()) {
            case "new-ticket" -> {
                String choice = event.getInteraction().getValues().getFirst();
                TextInput name =
                        TextInput.create("name", "Dein Ingame Name", TextInputStyle.SHORT)
                                .setPlaceholder("z.B. \"Knerio\"")
                                .setMinLength(3)
                                .setMaxLength(10)
                                .setRequired(true)
                                .build();
                TextInput issue =
                        TextInput.create("issue", choice.equals("bug") ? "Kurze Beschreibung deines Bugs" : "Kurze Beschreibung des Problems", TextInputStyle.PARAGRAPH)
                                .setRequired(true)
                                .setPlaceholder(choice.equals("bug") ? "z.B. \"Ich habe eine Fehler im System x gefunden\"" : "z.B. \"Ich kann kein /help machen\"")
                                .setMinLength(10)
                                .setMaxLength(999)
                                .build();
                Modal modal = Modal.create("new-ticket-" + choice, "Ticket").addActionRows(ActionRow.of(issue), ActionRow.of(name)).build();
                event.replyModal(modal).queue();
            }
            case null, default -> {
            }
        }
    }

    @ModuleListener
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        switch (event.getButton().getId()) {
            case "cancel-close" -> ticketManager.cancelTicketDeletion(event.getChannel().asTextChannel(), event);
            case "ticket-close" -> ticketManager.closeTicket(event.getChannel().asTextChannel(), event);
            case "new-ticket" -> {

            }
            case "ticket-claim" -> ticketManager.claimTicket(event.getChannel().asTextChannel(), event);
            case null, default -> {
            }
        }

    }

    @ModuleListener
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("new-ticket")) return;
        Ticket ticket = ticketManager.createTicket(event.getGuild(), event.getUser(), event, Arrays.stream(event.getModalId().split("-")).toList().getLast());
        if (ticket == null) {
            event.reply("Du hast bereits ein Ticket offen!").setEphemeral(true).queue();
            return;
        }
        bot.getRepo(TicketRepo.class).save(ticket);
        event.reply("Dein Ticket wurde erstellt " + event.getGuild().getChannelById(TextChannel.class, ticket.getChannelId()).getAsMention()).setEphemeral(true).queue();
    }

    @ModuleListener
    public void onMessage(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) return;
        if (!event.getChannel().getType().isMessage()) return;
        if (!event.getChannel().getName().contains("-")) return;
        TicketRepo ticketRepo = (TicketRepo) bot.getRepo(TicketRepo.class);
        ticketRepo.asyncFindFirstById(new ObjectId(List.of(event.getChannel().asTextChannel().getName().split("-")).getLast())).thenAcceptAsync(ticket -> {
            if (ticket == null) return;
            ticket.getHistory()
                    .add(Ticket.HistoryItem.builder()
                            .id(new ObjectId())
                            .senderId(event.getAuthor().getIdLong())
                            .content(event.getMessage().getContentDisplay())
                            .build());
            ticketRepo.save(ticket);
        });
    }


}

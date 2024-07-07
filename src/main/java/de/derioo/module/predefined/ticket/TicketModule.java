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
import static de.derioo.module.predefined.ticket.Ticket.Type.BUG;
import static de.derioo.module.predefined.ticket.Ticket.Type.QUESTIONS;

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

        for (Ticket.Type value : Ticket.Type.values()) {
            builder.addOption(value.getTag(), value.name(), value.getDesc());
        }

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
                Ticket.Type choice = Ticket.Type.valueOf(event.getInteraction().getValues().getFirst());
                List<TextInput> inputs = new ArrayList<>();
                switch (choice) {
                    case PARTNER -> {
                        inputs.add(TextInput.create("website", "Websiten oder Discord Link", TextInputStyle.SHORT)
                                .setRequired(true)
                                .setPlaceholder("\"discord.gg/varilx\"")
                                .build());
                        inputs.add(TextInput.create("contact", "Kontakt", TextInputStyle.SHORT)
                                .setRequired(true)
                                .setPlaceholder("\"email@example.com\"")
                                .build());
                        inputs.add(TextInput.create("text", "Bewerbung", TextInputStyle.PARAGRAPH)
                                .setRequired(true)
                                .setPlaceholder("Hallo liebes Varilx.DE Team, \n...")
                                .setMinLength(15)
                                .build());
                    }
                    case HELP_AND_SUPPORT -> {
                        inputs.add(TextInput.create("issue", "Kurze Beschreibung deines Anliegens", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z.B. \"Ich will das und das machen und habe deshalb ein Problem\"")
                                .setRequired(true)
                                .setMinLength(10)
                                .build());
                        inputs.add(TextInput.create("name", "Dein Ingame Name", TextInputStyle.SHORT)
                                .setPlaceholder("z.B. \"Knerio\"")
                                .setMinLength(3)
                                .setMaxLength(10)
                                .setRequired(true)
                                .build());
                        inputs.add(TextInput.create("picture", "Bilder getrennt mit Leerzeichen (optional)", TextInputStyle.SHORT)
                                .setRequired(false)
                                .build());
                    }
                    case BUG -> {
                        inputs.add(TextInput.create("issue", "Kurze Beschreibung des Bugs / Problems", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z.B. \"Ich habe eine Fehler im System x gefunden\"")
                                .setRequired(true)
                                .setMinLength(10)
                                .build());
                        inputs.add(TextInput.create("name", "Dein Ingame Name", TextInputStyle.SHORT)
                                .setPlaceholder("z.B. \"Knerio\"")
                                .setMinLength(3)
                                .setMaxLength(10)
                                .setRequired(true)
                                .build());
                        inputs.add(TextInput.create("picture", "Bilder getrennt mit Leerzeichen (optional)", TextInputStyle.SHORT)
                                .setRequired(false)
                                .build());
                    }
                    case QUESTIONS -> {
                        inputs.add(TextInput.create("question", "Kurze Beschreibung deiner Frage", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z.B. \"Ich habe eine Frage zum System x\"")
                                .setRequired(true)
                                .setMinLength(10)
                                .build());
                    }
                }
                Modal modal = Modal.create("new-ticket-" + choice, "Ticket").addActionRows(inputs.stream().map(ActionRow::of).toList()).build();
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
            case "ticket-claim" -> ticketManager.claimTicket(event.getChannel().asTextChannel(), event);
            case null, default -> {
            }
        }

    }

    @ModuleListener
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().startsWith("new-ticket")) return;
        Ticket ticket = ticketManager.createTicket(event.getGuild(), event.getUser(), event, Ticket.Type.valueOf(Arrays.stream(event.getModalId().split("-")).toList().getLast()));
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
        String id = List.of(event.getChannel().asTextChannel().getName().split("-")).getLast();
        if (!ObjectId.isValid(id)) return;
        TicketRepo ticketRepo = (TicketRepo) bot.getRepo(TicketRepo.class);
        ticketRepo.asyncFindFirstById(new ObjectId(id)).thenAcceptAsync(ticket -> {
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

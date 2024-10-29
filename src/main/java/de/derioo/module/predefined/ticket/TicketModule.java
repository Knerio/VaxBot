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
import net.dv8tion.jda.api.entities.channel.ChannelType;
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
import static de.derioo.module.predefined.ticket.Ticket.Type.*;

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
        StringSelectMenu.Builder builder = StringSelectMenu.create("new-ticket");

        for (Ticket.Type value : Ticket.Type.values()) {
            if (value == EVENT_TOKEN) continue;
            builder.addOption(value.getTag(), value.name(), value.getDesc());
        }

        builder.setMaxValues(1);
        builder.setMinValues(1);


        updateOrSendEmbed(Config.Id.Channel.TICKET_CREATION_CHANNEL, getEmbed(),
                ActionRow.of(Button.link("https://tube-hosting.com/pricing", "Partner").withEmoji(Emoji.fromFormatted("<:TubehostingVarilx:1101657813794693120>"))),
                ActionRow.of(builder.build())
        );
        StringSelectMenu.Builder eventTokenMenu = StringSelectMenu.create("new-ticket");

        eventTokenMenu.addOption(EVENT_TOKEN.getTag(), EVENT_TOKEN.name(), EVENT_TOKEN.getDesc());
        eventTokenMenu.setMaxValues(1);
        eventTokenMenu.setMinValues(1);

        updateOrSendEmbed(Config.Id.Channel.EVENT_TOKEN_CHANNEL, DiscordBot.Default.builder()
                .setTitle("Event Tokens können unten eingelöst werden")
                .addField("— Crate Keys —", """
                        ```1x Vote Crate Key  —  1 Event Token``` ```1x Nether Crate Key  —  2 Event Tokens``` ```1x End Crate Key  —  3 Event Tokens``` ```1x Warden Crate Key  —  4 Event Tokens``` ```1x Eternal Crate Key  —  5 Event Tokens```
                        """, false)
                .addField("— Erze —", """
                        ```16x Netherite Barren  —  2 Event Tokens``` ```32x Netherite Barren  —  3 Event Tokens``` ```48x Netherite Barren  —  4 Event Tokens``` ```64x Netherite Barren  —  5 Event Tokens```
                        ```2x Netherite Block  —  2 Event Tokens``` ```4x Netherite Block  —  3 Event Tokens``` ```8x Netherite Block  —  5 Event Tokens``` ```16x Netherite Block  —  9 Event Tokens```
                        ```4x Diamond Block  —  1 Event Tokens``` ```8x Diamond Block  —  2 Event Tokens``` ```16x Diamond Block  —  3 Event Tokens``` ```32x Diamond Block  —  5 Event Tokens``` ```64x Diamond Block  —  8 Event Tokens```
                        """, false)
                .addField("— Re:Create Shards —", """
                        ```1x Re:Create Shard  —  15 Event Tokens``` ```5x Re:Create Shard  —  70 Event Tokens``` ```10x Re:Create Shard  —  125 Event Tokens```
                        """, false)
                .addField("— Gutscheine —", """
                        ```1x 1.000 XP Gutschein (Skill wählbar)  —  10 Event Tokens``` ```1x 2.000 XP Gutschein (Skill wählbar)  —  19 Event Tokens``` ```1x 4.000 XP Gutschein (Skill wählbar)  —  35 Event Tokens``` ```1x 8.000 XP Gutschein (Skill wählbar)  —  60 Event Tokens``` ```1x 16.000 XP Gutschein (Skill wählbar)  —  110 Event Tokens```
                        ```5.000 Coins  —  1 Event Token``` ```11.000 Coins  —  2 Event Tokens``` ```24.000 Coins  —  4 Event Tokens``` ```55.000 Coins  —  8 Event Tokens``` ```120.000 Coins  —  16 Event Tokens``` ```250.000 Coins  —  32 Event Tokens``` ```600.000 Coins  —  64 Event Tokens```
                        ```1x Emerald Rang Gutschein  —  55 Event Tokens``` ```1x Demon Rang Gutschein  —  80 Event Tokens``` ```1x Reaper Rang Gutschein  —  120 Event Tokens``` ```1x DIVINE Rang Gutschein  —  200 Event Tokens```
                        """, false)
                .addField("— Spawn Eggs —", """
                        ```1x Zombie Spawn Egg  —  5 Event Tokens``` ```1x Skeleton Spawn Egg  —  7 Event Tokens``` ```1x Blaze Spawn Egg  —  4 Event Tokens``` ```1x Creeper Spawn Egg  —  50 Event Tokens``` ```1x Shulker Spawn Egg  —  135 Event Tokens``` ```1x Enderman Spawn Egg  —  70 Event Tokens``` ```1x Iron Golem Spawn Egg  —  105 Event Tokens``` ```1x Spider Spawn Egg  —  7 Event Tokens``` ```1x Endermite Spawn Egg  —  3 Event Tokens``` ```1x Guardian Egg  —  16 Event Tokens``` ```1x Elder Guardian Spawn Egg  —  23 Event Tokens``` ```1x Slime Spawn Egg  —  17 Event Tokens``` ```1x Glow Squid Spawn Egg  —  3 Event Tokens``` ```1x Witch Spawn Egg  —  145 Event Tokens``` ```1x Drowned Spawn Egg  —  22 Event Tokens``` ```1x Mooshroom Spawn Egg  —  9 Event Tokens``` ```1x Cow Spawn Egg  —  7 Event Tokens``` ```1x Sheep Spawn Egg  —  11 Event Tokens``` ```1x Pig Spawn Egg  —  5 Event Tokens``` ```1x Panda Spawn Egg  —  3 Event Tokens```
                        """, false)
                .setColor(Color.GREEN)
                .build(), ActionRow.of(eventTokenMenu.build()));
    }

    @NotNull
    private MessageEmbed getEmbed() throws JsonProcessingException {
        return EmbedBuilder.fromData(DataObject.fromJson(objectMapper.writeValueAsString(langConfig.getTicket().getTicketCreationEmbed()))).build();
    }


    @ModuleListener
    public void onMenuSelection(StringSelectInteractionEvent event) {
        switch (event.getSelectMenu().getId()) {
            case "new-ticket" -> {
                Ticket.Type choice = Ticket.Type.valueOf(event.getInteraction().getValues().getFirst());
                List<TextInput.Builder> inputs = new ArrayList<>();
                switch (choice) {
                    case PARTNER -> {
                        inputs.add(TextInput.create("website", "Websiten oder Discord Link", TextInputStyle.SHORT)
                                .setRequired(true).setMaxLength(1000)
                                .setPlaceholder("\"discord.gg/varilx\"")
                        );
                        inputs.add(TextInput.create("contact", "Kontakt", TextInputStyle.SHORT)
                                .setRequired(true).setMaxLength(1000)
                                .setPlaceholder("\"email@example.com\"")
                        );
                        inputs.add(TextInput.create("text", "Bewerbung", TextInputStyle.PARAGRAPH)
                                .setRequired(true).setMaxLength(1000)
                                .setPlaceholder("Hallo liebes Varilx.DE Team, \n...")
                                .setMinLength(15)
                        );
                    }
                    case EVENT_TOKEN -> {
                        inputs.add(TextInput.create("token-count", "Wie viele Tokens möchtest du einlösen", TextInputStyle.SHORT)
                                .setPlaceholder("z.B. \"12\"")
                                .setRequired(true).setMaxLength(1000)
                        );
                        inputs.add(TextInput.create("token-item", "Wie möchtest du bekommen?", TextInputStyle.SHORT)
                                .setPlaceholder("z.B. \"64x Netherite Barren\"")
                                .setRequired(true).setMaxLength(1000)
                        );
                        addIngameNameInput(inputs);
                    }
                    case HELP_AND_SUPPORT -> {
                        inputs.add(TextInput.create("issue", "Kurze Beschreibung deines Anliegens", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z.B. \"Ich will das und das machen und habe deshalb ein Problem\"")
                                .setRequired(true).setMaxLength(1000)
                                .setMinLength(10)
                        );
                        addIngameNameInput(inputs);
                        addPictureInput(inputs);
                    }
                    case BUG -> {
                        inputs.add(TextInput.create("reproduce", "Schritte zum Reproduzieren", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z.B. \"Du musst zuerst das machen, damit x Fehler passiert\"")
                                .setRequired(true).setMaxLength(1000)
                                .setMinLength(10)
                        );
                        inputs.add(TextInput.create("issue", "Kurze Beschreibung des Bugs / Problems", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z.B. \"Ich habe eine Fehler im System x gefunden\"")
                                .setRequired(true).setMaxLength(1000)
                                .setMinLength(10)
                        );
                        addIngameNameInput(inputs);
                        addPictureInput(inputs);
                    }
                    case QUESTIONS -> {
                        inputs.add(TextInput.create("question", "Kurze Beschreibung deiner Frage", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z.B. \"Ich habe eine Frage zum System x\"")
                                .setRequired(true).setMaxLength(1000)
                                .setMinLength(10)
                        );
                    }
                }
                inputs.forEach(textInput -> {
                    textInput.setMaxLength(1000);
                });
                Modal modal = Modal.create("new-ticket-" + choice, "Ticket").addComponents(inputs.stream().map(TextInput.Builder::build).map(ActionRow::of).toList()).build();
                event.replyModal(modal).queue();
            }
            case null, default -> {
            }
        }
    }

    private static void addPictureInput(@NotNull List<TextInput.Builder> inputs) {
        inputs.add(TextInput.create("picture", "Bilder getrennt mit Leerzeichen (optional)", TextInputStyle.SHORT)
                .setRequired(false)
        );
    }

    private static void addIngameNameInput(@NotNull List<TextInput.Builder> inputs) {
        inputs.add(TextInput.create("name", "Dein Ingame Name", TextInputStyle.SHORT)
                .setPlaceholder("z.B. \"Knerio\"")
                .setMinLength(3)
                .setMaxLength(15)
                .setRequired(true).setMaxLength(1000));
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
        if (!event.getChannel().getType().equals(ChannelType.TEXT)) return;
        Ticket ticket = getTicket(event.getChannel().getIdLong());
        if (ticket == null) return;
        ticket.getHistory()
                .add(Ticket.HistoryItem.builder()
                        .id(new ObjectId())
                        .senderId(event.getAuthor().getIdLong())
                        .content(event.getMessage().getContentDisplay())
                        .build());
        bot.getRepo(TicketRepo.class).save(ticket);
    }


    private Ticket getTicket(Long channelId) {
        for (Ticket ticket : bot.getRepo(TicketRepo.class).findAll()) {
            if (Objects.equals(ticket.getChannelId(), channelId)) return ticket;
        }
        return null;
    }

}

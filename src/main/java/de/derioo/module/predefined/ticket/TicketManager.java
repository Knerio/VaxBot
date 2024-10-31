package de.derioo.module.predefined.ticket;

import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.module.Module;
import de.derioo.utils.Emote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.managers.channel.concrete.TextChannelManager;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static de.derioo.utils.UserUtils.getMention;
import static net.dv8tion.jda.api.Permission.VIEW_CHANNEL;

public class TicketManager {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Map<ObjectId, List<ScheduledFuture<?>>> scheduledTasks = new HashMap<>();

    private final DiscordBot bot;

    public TicketManager(DiscordBot bot) {
        this.bot = bot;
    }

    public Ticket createTicket(Guild guild, User user, ModalInteractionEvent event, Ticket.Type type) {
        for (Ticket ticket : bot.getRepo(TicketRepo.class).findAll()) {
            if (!Objects.equals(ticket.getType(), type)) continue;
            if (ticket.getUserId().equals(user.getIdLong())) return null;
        }
        ObjectId objectId = new ObjectId();
        TextChannel ticketChannel = guild.createTextChannel(type + "-" + user.getName() + "-" + objectId, guild.getCategoryById(bot.get(guild).getChannels().get(type.getCategory().name()))).complete();
        List<Role> roles = bot.get(guild).getRoleObjects(type.getRole(), guild);
        Member creator = guild.getMember(user);
        TextChannelManager manager = ticketChannel.getManager();

        for (Role role : roles) {
            manager.putPermissionOverride(role, EnumSet.of(VIEW_CHANNEL), null);
        }
        manager.putPermissionOverride(creator, EnumSet.of(VIEW_CHANNEL), null).queue();


        Map<String, String> values = new HashMap<>();
        switch (type) {
            case EVENT_TOKEN -> {
                values.put("Token Anzahl", event.getValue("token-count").getAsString());
                values.put("Token Einlösewert", event.getValue("token-item").getAsString());
                values.put("Ingame Name", event.getValue("name").getAsString());
            }

            case PARTNER -> {
                values.put("Website/Discord", event.getValue("website").getAsString());
                values.put("Kontakt", event.getValue("contact").getAsString());
                values.put("Bewerbung", event.getValue("text").getAsString());
            }
            case HELP_AND_SUPPORT -> {
                values.put("Problembeschreibung", event.getValue("issue").getAsString());
                values.put("Ingame Name", event.getValue("name").getAsString());
                ModalMapping picture = event.getValue("picture");
                if (picture != null && !picture.getAsString().isBlank()) {
                    values.put("Bilder", picture.getAsString());
                }
            }
            case BUG -> {
                values.put("Problembeschreibung", event.getValue("issue").getAsString());
                values.put("Schritte zum Reproduzieren", event.getValue("reproduce").getAsString());
                values.put("Ingame Name", event.getValue("name").getAsString());
                ModalMapping picture = event.getValue("picture");
                if (picture != null && !picture.getAsString().isBlank()) {
                    values.put("Bilder", picture.getAsString());
                }
            }
            case QUESTIONS -> {
                values.put("Frage", event.getValue("question").getAsString());
            }
        }
        values.put("Typ des Tickets", type.getTag());

        EmbedBuilder embedBuilder = DiscordBot.Default.builder()
                .setTitle("Varilx Tickets")
                .setDescription("""
                        · Bitte gedulde dich ein bisschen, es wird sich bald jemand um dich kümmern.
                        · Sollten wir nicht erreichbar sein, melde dich bitte im Forum!
                        https://forum.varilx.de/forum/view/8-support/"""
                )
                .setColor(Color.GREEN);

        values.forEach((s, s2) -> {
            embedBuilder.addField(s, s2, false);
        });

        ticketChannel.sendMessage(event.getUser().getAsMention() + bot.get(guild).getMentions(type.getRole(), guild))
                .addEmbeds(embedBuilder
                        .build())
                .addActionRow(
                        Button.danger("ticket-close", "Ticket schließen -> " + Emote.TRASH.getData()),
                        Button.primary("ticket-claim", "Ticket claimen -> " + Emote.LOCK.getData())
                )
                .queue();

        if (values.containsKey("Bilder")) {
            for (String s : values.get("Bilder").split(" ")) {
                if (s.isBlank()) continue;
                ticketChannel.sendMessage(s).queue();
            }
        }

        return Ticket.builder()
                .id(objectId)
                .type(type)
                .history(new ArrayList<>())
                .guildId(guild.getIdLong())
                .userId(user.getIdLong())
                .channelId(ticketChannel.getIdLong())
                .history(new ArrayList<>(List.of(Ticket.HistoryItem.builder()
                        .id(new ObjectId())
                        .content(values.entrySet().stream().map(entry -> entry.getKey() + ": " + entry.getValue()).collect(Collectors.joining("\n")))
                        .build())))
                .build();
    }

    public Ticket claimTicket(@NotNull TextChannel channel, @NotNull ButtonInteractionEvent event) {
        Ticket ticket = getTicket(channel.getIdLong());

        if (ticket.getUserId() == event.getUser().getIdLong()) {
            event.reply("Du darfst dieses Ticket nicht claimen").setEphemeral(true).queue();
            return null;
        }

        if (ticket.getClaimerId() != null) {
            event.reply("Dieses Ticket ist bereits geclaimed worden").setEphemeral(true).queue();
            return null;
        }

        ticket.setClaimerId(event.getUser().getIdLong());
        ticket.getHistory()
                .add(Ticket.HistoryItem.builder()
                        .id(new ObjectId())
                        .content("Das Ticket wurde von " + event.getUser().getAsMention() + " geclaimed")
                        .build());

        event.replyEmbeds(DiscordBot.Default.builder()
                .setTitle("Ticket wurde geclaimed")
                .setDescription("Das Ticket wurde von " + event.getUser().getAsMention() + " geclaimed")
                .setColor(Color.GREEN)
                .build()
        ).queue();

        Guild guild = event.getGuild();
        List<Role> roles = bot.get(guild).getRoleObjects(ticket.getType().getRole(), guild);
        TextChannelManager manager = channel.getManager();
        for (Role role : roles) {
            manager = manager.putPermissionOverride(role, null, EnumSet.of(VIEW_CHANNEL));
        }

        manager.putPermissionOverride(guild.getMember(event.getUser()), EnumSet.of(VIEW_CHANNEL), null).queue();

        bot.getRepo(TicketRepo.class).save(ticket);

        return ticket;
    }

    public void cancelTicketDeletion(@NotNull TextChannel channel, ButtonInteractionEvent event) {
        Ticket ticket = getTicket(channel.getIdLong());
        List<ScheduledFuture<?>> scheduledFutures = scheduledTasks.get(ticket.getId());
        for (ScheduledFuture<?> task : new ArrayList<>(scheduledFutures)) {
            task.cancel(true);
            scheduledFutures.remove(task);
        }
        event.reply("Ticket schließen wurde abgebrochen").setEphemeral(true).queue();
        event.getMessage().delete().queue();

    }

    public void closeTicket(@NotNull TextChannel channel, ButtonInteractionEvent event) {
        Ticket ticket = getTicket(channel.getIdLong());
        scheduledTasks.putIfAbsent(ticket.getId(), new ArrayList<>());
        if (!scheduledTasks.get(ticket.getId()).isEmpty()) {
            event.reply("Das Ticket schließt schon!").setEphemeral(true).queue();
            return;
        }
        AtomicReference<InteractionHook> current = new AtomicReference<>();
        EmbedBuilder builder = DiscordBot.Default.builder()
                .setColor(Color.RED)
                .setTitle("Ticket schließt...")
                .setDescription("Das Ticket schließt **10** in Sekunden");
        event.replyEmbeds(
                builder.build()
        ).addActionRow(Button.danger("cancel-close", "Abbrechen")).queue(current::set);
        for (int i = 0; i < 10; i++) {
            int delay = i;
            ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
                current.get().editOriginalEmbeds(builder.setDescription("Das Ticket schließt **" + (10 - delay) + "** in Sekunden").build()).queue();
            }, i, TimeUnit.SECONDS);
            scheduledTasks.get(ticket.getId()).add(scheduledTask);
        }

        ScheduledFuture<?> deleteTask = scheduler.schedule(() -> {
            try {
                channel.delete().queue();
                bot.getRepo(TicketRepo.class).delete(ticket);
                Guild guild = event.getGuild();
                TextChannel logs = guild.getTextChannelById(bot.get(guild).getChannels().get(Config.Id.Channel.TICKET_LOGS_CHANNEL.name()));
                if (logs == null) return;
                sendLog(logs, ticket, event);

                Set<User> transcriptions = new HashSet<>();
                transcriptions.add(event.getJDA().retrieveUserById(ticket.getUserId()).complete());
                transcriptions.add(event.getUser());
                if (ticket.getClaimerId() != null)
                    transcriptions.add(event.getJDA().retrieveUserById(ticket.getClaimerId()).complete());
                transcriptions.addAll(ticket.getParticipantUsers(guild));
                for (User user : transcriptions) {
                    user.openPrivateChannel().queue(pc -> {
                        sendLog(pc, ticket, event);
                    });
                }

            } catch (Exception e) {
                Module.logThrowable(bot, e);
            }
        }, 11, TimeUnit.SECONDS);
        scheduledTasks.get(ticket.getId()).add(deleteTask);


    }

    public void sendLog(MessageChannel channel, Ticket ticket, ButtonInteractionEvent event) {
        final Guild guild = event.getGuild();
        final String informations = ticket.getInformations(guild);
        final int length = informations.length();

        EmbedBuilder embed = DiscordBot.Default.builder()
                .setColor(Color.GREEN)
                .setTitle("Varilx.de | Ticket")
                .addField(new MessageEmbed.Field(Emote.TEXT_CHANNEL.getData() + "Ticket Name", channel.getName(), false))
                .addField(new MessageEmbed.Field(Emote.CALENDAR.getData() + "Geschlossen von:", getMention(event.getUser()), true))
                .addField(new MessageEmbed.Field(Emote.USER.getData() + "Claimer:", ticket.getClaimerId() == null ? "**Nicht geclaimed**" : (getMention(guild.getMemberById(ticket.getClaimerId()))), true))
                .addField(new MessageEmbed.Field(Emote.USER.getData() + "Teilnehmer:", ticket.getParticipants(guild), true));

        if (length < MessageEmbed.VALUE_MAX_LENGTH) {
            embed.addField(new MessageEmbed.Field("Ticket Informationen", informations, false));
            channel.sendMessageEmbeds(embed.build()).queue();
            return;
        } else if (length < MessageEmbed.DESCRIPTION_MAX_LENGTH) {
            embed.setDescription(informations);
            channel.sendMessageEmbeds(embed.build()).queue();
            return;
        }
        int messages = length / Message.MAX_CONTENT_LENGTH + 1;
        for (int i = 0; i < messages; i++) {
            String currentMessage = informations.substring(i == 0 ? 0 : (i - 1) * Message.MAX_CONTENT_LENGTH, i * Message.MAX_CONTENT_LENGTH);
            channel.sendMessage(currentMessage).queue();
        }


    }

    private Ticket getTicket(Long channelId) {
        for (Ticket ticket : bot.getRepo(TicketRepo.class).findAll()) {
            if (Objects.equals(ticket.getChannelId(), channelId)) return ticket;
        }
        return null;
    }


}

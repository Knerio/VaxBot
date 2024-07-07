package de.derioo.module.predefined.ticket;

import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
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
        TextChannel ticketChannel = guild.createTextChannel(type + "-" + user.getName() + "-" + objectId, guild.getCategoryById(bot.get(guild).getChannels().get(Config.Id.Category.TICKET_CATEGORY.name()))).complete();
        List<Role> roles = bot.get(guild).getRoleObjects(type.getRole(), guild);
        List<Member> membersWithRoles = roles.stream().map(guild::getMembersWithRoles).flatMap(Collection::stream).toList();
        Member creator = guild.getMemberById(user.getIdLong());

        for (Role role : roles) {
            ticketChannel.getManager()
                    .putPermissionOverride(role, EnumSet.of(VIEW_CHANNEL), null).complete();
        }

        ticketChannel.getManager()
                .putPermissionOverride(creator, EnumSet.of(VIEW_CHANNEL), null).complete();

        List<Long> sent = new ArrayList<>();

        for (Member member : membersWithRoles) {
            if (member.getUser().isBot()) continue;
            if (sent.contains(member.getIdLong())) continue;
            member.getUser().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessageEmbeds(DiscordBot.Default.builder().setDescription("Du kannst nun das Ticket " + ticketChannel.getAsMention() + " sehen").build()).queue();
            });
            sent.add(member.getIdLong());
        }
        if (!sent.contains(creator.getIdLong())) {
            creator.getUser().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessageEmbeds(DiscordBot.Default.builder().setDescription("Du kannst nun das Ticket " + ticketChannel.getAsMention() + " sehen").build()).queue();
            });
        }

        Map<String, String> values = new HashMap<>();
        switch (type) {
            default -> {
                values.put("Problem", event.getValue("issue").getAsString());
                values.put("Ingame Name", event.getValue("name").getAsString());
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

        ticketChannel.sendMessage(event.getUser().getAsMention() + bot.get(guild).getMentions(Config.Id.Role.TICKET_EDIT, guild))
                .addEmbeds(embedBuilder
                        .build())
                .addActionRow(Button.danger("ticket-close", "Ticket schließen -> \uD83D\uDDD1"), Button.primary("ticket-claim", "Ticket claimen -> \uD83D\uDD12"))
                .queue();

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
        Ticket ticket = bot.getRepo(TicketRepo.class).findFirstById(new ObjectId(List.of(channel.getName().split("-")).getLast()));

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
        for (Role role : roles) {
            channel.getManager()
                    .putPermissionOverride(role, EnumSet.of(VIEW_CHANNEL), null).complete();
        }

        channel.getManager()
                .putPermissionOverride(guild.getMember(event.getUser()), EnumSet.of(VIEW_CHANNEL), null)
                .queue();

        bot.getRepo(TicketRepo.class).save(ticket);

        return ticket;
    }

    public void cancelTicketDeletion(@NotNull TextChannel channel, ButtonInteractionEvent event) {
        Ticket ticket = bot.getRepo(TicketRepo.class).findFirstById(new ObjectId(List.of(channel.getName().split("-")).getLast()));
        List<ScheduledFuture<?>> scheduledFutures = scheduledTasks.get(ticket.getId());
        for (ScheduledFuture<?> task : new ArrayList<>(scheduledFutures)) {
            task.cancel(true);
            scheduledFutures.remove(task);
        }
        event.reply("Ticket schließen wurde abgebrochen").setEphemeral(true).queue();
        event.getMessage().delete().queue();

    }

    public void closeTicket(@NotNull TextChannel channel, ButtonInteractionEvent event) {
        Ticket ticket = bot.getRepo(TicketRepo.class).findFirstById(new ObjectId(List.of(channel.getName().split("-")).getLast()));
        scheduledTasks.putIfAbsent(ticket.getId(), new ArrayList<>());
        if (!scheduledTasks.get(ticket.getId()).isEmpty()) {
            event.reply("Das Ticket schließt schon!").setEphemeral(true).queue();
            return;
        }
        AtomicReference<InteractionHook> current = new AtomicReference<>();
        EmbedBuilder builder = DiscordBot.Default.builder()
                .setTitle("Ticket schließt...")
                .setDescription("Das Ticket schließt **10** in Sekunden");
        event.replyEmbeds(
                builder.build()
        ).addActionRow(Button.primary("cancel-close", "Abbrechen")).queue(current::set);
        for (int i = 0; i < 10; i++) {
            int delay = i;
            ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
                current.get().editOriginalEmbeds(builder.setDescription("Das Ticket schließt **" + (10 - delay) + "** in Sekunden").build()).queue();
            }, i, TimeUnit.SECONDS);
            scheduledTasks.get(ticket.getId()).add(scheduledTask);
        }

        ScheduledFuture<?> deleteTask = scheduler.schedule(() -> {
            channel.delete().queue();
            bot.getRepo(TicketRepo.class).delete(ticket);
            Guild guild = event.getGuild();
            TextChannel logs = guild.getTextChannelById(bot.get(guild).getChannels().get(Config.Id.Channel.TICKET_LOGS_CHANNEL.name()));
            if (logs == null) return;
            MessageEmbed embed = DiscordBot.Default.builder()
                    .setColor(Color.GREEN)
                    .setTitle("Varilx.de | Ticket")
                    .addField(new MessageEmbed.Field("Ticket Informationen", getTicketInformations(ticket, event.getGuild()), false))
                    .addField(new MessageEmbed.Field("<:varilx_textchannel:1139957022696157294> Ticket Name", channel.getName(), false))
                    .addField(new MessageEmbed.Field("<:varilx_clendar:1139956980576960653> Geschlossen von:", getMention(event.getUser()), true))
                    .addField(new MessageEmbed.Field("<:varilx_user:1139957321196376107> Claimer:", ticket.getClaimerId() == null ? "**Nicht geclaimed**" : (getMention(guild.getMemberById(ticket.getClaimerId()))), true))
                    .addField(new MessageEmbed.Field("<:varilx_user:1139957321196376107> Teilnehmer:", getSupporters(ticket, guild), true))
                    .build();
            logs.sendMessageEmbeds(embed).queue();
            event.getJDA().getUserById(ticket.getUserId()).openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue());
            if (ticket.getUserId() != event.getUser().getIdLong())
                event.getUser().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue());
            if (ticket.getClaimerId() != null)
                guild.getMemberById(ticket.getClaimerId()).getUser().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue());

        }, 11, TimeUnit.SECONDS);
        scheduledTasks.get(ticket.getId()).add(deleteTask);


    }

    private @NotNull String getTicketInformations(@NotNull Ticket ticket, Guild guild) {
        StringBuilder builder = new StringBuilder();
        for (Ticket.HistoryItem historyItem : ticket.getHistory()) {
            if (historyItem.getSenderId() == null) {
                builder.append("*").append(historyItem.getContent()).append("*").append("\n");
                continue;
            }
            builder.append(getMention(guild.getMemberById(historyItem.getSenderId())))
                    .append(" -> ")
                    .append(historyItem.getContent())
                    .append("\n");
        }
        builder.append("~~**---»-----------------------------------------«---**~~");
        return builder.toString();
    }

    private @NotNull String getSupporters(@NotNull Ticket ticket, Guild guild) {
        Set<String> set = new HashSet<>();
        for (Ticket.HistoryItem historyItem : ticket.getHistory()) {
            if (historyItem.getSenderId() == null) continue;
            Member member = guild.getMemberById(historyItem.getSenderId());
            set.add(getMention(member.getUser()));
        }
        return String.join("\n", set);
    }

}

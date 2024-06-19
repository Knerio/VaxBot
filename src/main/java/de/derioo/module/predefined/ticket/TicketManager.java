package de.derioo.module.predefined.ticket;

import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.utils.UserUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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

    @SuppressWarnings("unchecked")
    public Ticket createTicket(Guild guild, User user, ModalInteractionEvent event) {
        for (Ticket ticket : bot.getRepo(TicketRepo.class).findAll()) {
            if (ticket.getUserId().equals(user.getIdLong())) return null;
        }
        ObjectId objectId = new ObjectId();
        TextChannel ticketChannel = guild.createTextChannel(user.getName() + "-ticket-" + objectId, guild.getCategoryById(bot.get(guild).getChannels().get(Config.Id.Category.TICKET_CATEGORY.name()))).complete();
        Long ticketSeeId = bot.get(guild).getRoles().get(Config.Id.Role.TICKET_EDIT.name());
        Role roleById = guild.getRoleById(ticketSeeId);
        List<Member> membersWithRoles = guild.getMembersWithRoles(roleById);
        Member creator = guild.getMemberById(user.getIdLong());

        ticketChannel.getManager()
                .putPermissionOverride(roleById, EnumSet.of(VIEW_CHANNEL), null)
                .putPermissionOverride(creator, EnumSet.of(VIEW_CHANNEL), null).complete();

        for (Member member : membersWithRoles) {
            if (member.getUser().isBot()) continue;
            member.getUser().openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessage("Du kannst nun das Ticket " + ticketChannel.getAsMention() + " sehen").queue();
            });
        }
        creator.getUser().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessage("Du kannst nun das Ticket " + ticketChannel.getAsMention() + " sehen").queue();
        });

        ticketChannel.sendMessage(event.getUser().getAsMention() + guild.getRoleById(bot.get(guild).getRoles().get(Config.Id.Role.TICKET_EDIT.name())).getAsMention())
                .addEmbeds(DiscordBot.Default.builder()
                        .setTitle("Varilx Tickets")
                        .setDescription("""
                                · Bitte gedulde dich ein bisschen, es wird sich bald jemand um dich kümmern.
                                · Sollten wir nicht erreichbar sein, melde dich bitte im Forum!
                                https://forum.varilx.de/forum/view/8-support/"""
                        )
                        .addField(new MessageEmbed.Field("Ingame-Name", event.getValue("name").getAsString(), false))
                        .addField(new MessageEmbed.Field("Beschreibung des Problems", event.getValue("issue").getAsString(), false))

                        .setColor(Color.GREEN)
                        .build())
                .addActionRow(Button.danger("ticket-close", "Ticket schließen -> \uD83D\uDDD1"), Button.primary("ticket-claim", "Ticket claimen -> \uD83D\uDD12"))
                .queue();

        return Ticket.builder()
                .id(objectId)
                .history(new ArrayList<>())
                .guildId(guild.getIdLong())
                .userId(user.getIdLong())
                .channelId(ticketChannel.getIdLong())
                .history(new ArrayList<>(List.of(Ticket.HistoryItem.builder()
                        .id(new ObjectId())
                        .senderId(user.getIdLong())
                        .content("Mein Ingame name ist `" + event.getValue("name").getAsString() + "`. \n" +
                                "Mein Problem ist: " + event.getValue("issue").getAsString())
                        .build())))
                .build();
    }

    public Ticket claimTicket(@NotNull TextChannel channel, @NotNull ButtonInteractionEvent event) {
        Ticket ticket = bot.getRepo(TicketRepo.class).findFirstById(new ObjectId(List.of(channel.getName().split("-")).getLast()));

        ConfigData configData = bot.get(event.getGuild());

        List<Long> data = configData.getData(Config.Id.Data.TEAM_ROLE.name(), List.class);
        if (data == null) data = new ArrayList<>();

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
                        .content(ticket.getClaimerId() + " hat das Ticket geclaimed")
                        .build());

        event.replyEmbeds(DiscordBot.Default.builder()
                .setTitle("Ticket wurde geclaimed")
                .setDescription("Das Ticket wurde von " + event.getUser().getAsMention() + " geclaimed")
                .setColor(Color.GREEN)
                .build()
        ).queue();

        Guild guild = event.getGuild();
        Long ticketSeeId = bot.get(guild).getRoles().get(Config.Id.Role.TICKET_EDIT.name());
        Role roleById = guild.getRoleById(ticketSeeId);

        channel.getManager()
                .putPermissionOverride(roleById, null, EnumSet.of(VIEW_CHANNEL))
                .putPermissionOverride(guild.getMember(event.getUser()), EnumSet.of(VIEW_CHANNEL), null)
                .queue();

        bot.getRepo(TicketRepo.class).save(ticket);

        return ticket;
    }

    public Ticket cancelTicketDeletion(@NotNull TextChannel channel, ButtonInteractionEvent event) {
        Ticket ticket = bot.getRepo(TicketRepo.class).findFirstById(new ObjectId(List.of(channel.getName().split("-")).getLast()));
        for (ScheduledFuture<?> task : scheduledTasks.get(ticket.getId())) {
            task.cancel(false);
        }
        event.reply("Ticket schließen wurde abgebrochen").setEphemeral(true).queue();
        event.getMessage().delete().queue();

        return ticket;
    }

    public Ticket closeTicket(@NotNull TextChannel channel, ButtonInteractionEvent event) {
        Ticket ticket = bot.getRepo(TicketRepo.class).findFirstById(new ObjectId(List.of(channel.getName().split("-")).getLast()));
        scheduledTasks.putIfAbsent(ticket.getId(), new ArrayList<>());
        if (scheduledTasks.get(ticket.getId()).stream().anyMatch(scheduledFuture -> scheduledFuture.isCancelled() || scheduledFuture.isDone())) {
            event.reply("Das Ticket schließt schon!").setEphemeral(true).queue();
            return null;
        }
        AtomicReference<InteractionHook> current = new AtomicReference<>();
        EmbedBuilder builder = DiscordBot.Default.builder()
                .setTitle("Ticket schließt...")
                .setDescription("Das Ticket schließt 10 in Sekunden");
        event.replyEmbeds(
                builder.build()
        ).addActionRow(Button.primary("cancel-close", "Abbrechen")).queue(current::set);
        for (int i = 0; i < 10; i++) {
            int delay = i;
            ScheduledFuture<?> scheduledTask = scheduler.schedule(() -> {
                current.get().editOriginalEmbeds(builder.setDescription("Das Ticket schließt " + (10 - delay) + " in Sekunden").build()).queue();
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
                    .addField(new MessageEmbed.Field(":mountain_snow: Ticket Name", channel.getName(), false))
                    .addField(new MessageEmbed.Field(":mountain_snow: Geschlossen von:", getMention(event.getUser()), true))
                    .addField(new MessageEmbed.Field(":mountain_snow: Claimer:", ticket.getClaimerId() == null ? "**Nicht geclaimed**" : (getMention(guild.getMemberById(ticket.getClaimerId()))), true))
                    .addField(new MessageEmbed.Field(":mountain_snow: Teilnehmer:", getSupporters(ticket, guild), true))
                    .build();
            logs.sendMessageEmbeds(embed).queue();
            event.getUser().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue());
            if (ticket.getClaimerId() != null)
                guild.getMemberById(ticket.getClaimerId()).getUser().openPrivateChannel().queue(pc -> pc.sendMessageEmbeds(embed).queue());

        }, 11, TimeUnit.SECONDS);
        scheduledTasks.get(ticket.getId()).add(deleteTask);


        return ticket;
    }

    private String getTicketInformations(Ticket ticket, Guild guild) {
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

    private String getSupporters(Ticket ticket, Guild guild) {
        Set<String> set = new HashSet<>();
        for (Ticket.HistoryItem historyItem : ticket.getHistory()) {
            if (historyItem.getSenderId() == null) continue;
            Member member = guild.getMemberById(historyItem.getSenderId());
            set.add(getMention(member.getUser()));
        }
        return String.join("\n", set);
    }

}

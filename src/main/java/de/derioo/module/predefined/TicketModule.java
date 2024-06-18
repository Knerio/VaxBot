package de.derioo.module.predefined;

import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.module.Module;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TicketModule extends Module {

    private final DiscordBot bot;
    private final ConfigRepo repo;

    public TicketModule(@NotNull DiscordBot bot) {
        this.bot = bot;
        this.repo = (ConfigRepo) bot.getRepo(ConfigRepo.class);
    }

    @Override
    public void start() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Config config = Config.get(repo);
                if (!config.getChannels().containsKey(Config.Id.Channel.TICKET_CREATION_CHANNEL.name())) return;
                Long channelId = config.getChannels().get(Config.Id.Channel.TICKET_CREATION_CHANNEL.name());
                for (Guild guild : bot.getJda().getGuilds()) {
                    TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
                    MessageHistory history = channel.getHistory();
                    if (history.isEmpty()) {
                        sendNewTicketMessage(channel);
                    } else {
                        Message message = history.getRetrievedHistory()
                                .get(0);
                        if (message.getAuthor().getIdLong() == bot.getJda().getSelfUser().getIdLong()) {
                            message.delete().queue();
                            sendNewTicketMessage(channel);
                        }
                    }
                }
            }
        }, TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(10));
    }

    private void sendNewTicketMessage(@NotNull TextChannel channel) {
        channel.sendMessageEmbeds(
                DiscordBot.Default
                        .builder()
                        .setTitle("Varilx Ticket-Support")
                        .setDescription("Hast du eine Frage oder brauchst du Hilfe auf Varilx?\n" +
                                "Erstelle einfach ein Ticket, indem du auf den ✉\uFE0F Button klickst.\n" +
                                "Sobald ein Teamler Zeit hat, wird er sich mit dir in Verbindung setzen.")
                        .addField(new MessageEmbed.Field("· Reaktionszeit", "Die Teamler des Varilx-Supports sind keine Roboter, sondern freiwillige Helfer. Es kann etwas dauern, bis du eine Antwort erhältst, also habe bitte Geduld. Du wirst so schnell wie möglich eine Antwort erhalten!\n" +
                                "Kein \"Hallo\"\n" +
                                "https://nohello.net/ ➡\uFE0F Schreibe mehr als nur \"Hallo\" oder \"Ich habe ein Problem\". Wir helfen dir gerne und wissen, dass du offensichtlich ein Problem oder eine Frage hast, wenn du ein Ticket öffnest!", false))
                        .addField(new MessageEmbed.Field("· Informationen bereitstellen", "Bitte gib uns eine detaillierte Beschreibung deines Problems! Wir benötigen möglicherweise einen Minecraft-Namen, eine Transaktions-ID, einen Screenshot oder einen Crash-Log. Wir müssen und möchten dein Problem verstehen, also hilf uns bitte dabei!", false))
                        .addField(new MessageEmbed.Field("· Discord Voice Support", "Wir bieten auch direkte Unterstützung über den Sprachkanal von Discord an. Um von unserem Sprachsupport zu profitieren, öffne einfach ein Ticket, beschreibe dein Problem und frage deinen Supporter nach einem Treffen!", false))
                        .addField(new MessageEmbed.Field("· Hilf dir selbst\n", "Du kannst auch unsere Support-Website (https://forum.varilx.de/forum/view/8-support/) überprüfen, um zu sehen, ob das Problem bereits beantwortet wurde, oder unsere Community um Hilfe bitten.", false))
                        .addField(new MessageEmbed.Field("· Unterstützte Sprachen", "\uD83C\uDDEC\uD83C\uDDE7 Englisch\n" +
                                "\uD83C\uDDE9\uD83C\uDDEA Deutsch", false))
                        .build()
        ).queue();
    }
}

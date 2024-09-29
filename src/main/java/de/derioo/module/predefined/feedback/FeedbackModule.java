package de.derioo.module.predefined.feedback;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.module.Module;
import de.derioo.module.predefined.suggestion.Suggestion;
import de.derioo.utils.Emote;
import de.derioo.utils.UserUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class FeedbackModule extends Module {
    public FeedbackModule(DiscordBot bot) {
        super(bot, "feedback");
    }

    @Override
    public void once() throws ExecutionException, InterruptedException {
        Config config = Config.get(bot.getRepo(ConfigRepo.class));
        for (Guild guild : bot.getJda().getGuilds()) {
            if (!config.getData().get(guild.getId()).getChannels().containsKey(Config.Id.Channel.FEEDBACK_CREATION_CHANNEL.name()))
                continue;
            Long channelId = config.getData().get(guild.getId()).getChannels().get(Config.Id.Channel.FEEDBACK_CREATION_CHANNEL.name());
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

    @ModuleListener
    public void onButton(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId() == null) return;
        switch (event.getButton().getId().toLowerCase()) {
            case "create-feedback" -> {
                TextInput name =
                        TextInput.create("feedback", "Dein Feedback", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z. B. \"Ich liebe diesen Server!!!\"")
                                .setMinLength(10)
                                .setRequired(true)
                                .build();
                Modal modal = Modal.create("new-feedback", "Feedback").addComponents(ActionRow.of(name)).build();
                event.replyModal(modal).queue();
            }
        }
    }


    @ModuleListener
    public void onRealCreation(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("new-feedback")) return;
        String feedback = event.getValue("feedback").getAsString();
        TextChannel textChannelById = event.getGuild().getTextChannelById(bot.get(event.getGuild()).getChannels().get(Config.Id.Channel.FEEDBACK_ADMIN_CHANNEL.name()));
        textChannelById.sendMessageEmbeds(DiscordBot.Default.builder()
                .setDescription(UserUtils.getMention(event.getUser()) + " hat uns Feedback gegeben:\n" + feedback)
                .setColor(Color.GREEN)
                .setThumbnail("https://cdn.discordapp.com/attachments/1055223755909111808/1160508079419424840/Unbenanntdsadasd-2.png?ex=6534ea5f&is=6522755f&hm=00ea7dd8a3fd0c5dfcfccfa6952527b679094abf07d22143fee44b0b7221aa4a&")
                .build()).queue();
        event.reply("Dein Feedback wurde erfolgreich gesesendet.").setEphemeral(true).queue();
    }

    private void sendNewTicketMessage(@NotNull TextChannel channel) {
        EmbedBuilder embed =
                DiscordBot.Default.builder()
                        .setColor(Color.GREEN)
                        .setTitle("Varilx Feedback ")
                        .setDescription(
                                """
                                        Du willst dein Feedback über den Server da lassen?\s
                                        Dann drück einfach auf den Button unter dieser Nachricht.\s
                                        Wir wollen nicht, das dieses Feature ausgenutzt wird, daher bitten wir dich ein ordentliches und konstruktives Feedback zu verfassen!
                                        """)
                        .setThumbnail("https://cdn.discordapp.com/attachments/1055223755909111808/1160508079419424840/Unbenanntdsadasd-2.png?ex=6534ea5f&is=6522755f&hm=00ea7dd8a3fd0c5dfcfccfa6952527b679094abf07d22143fee44b0b7221aa4a&");
        channel.sendMessageEmbeds(embed.build()).setActionRow(Button.secondary("create-feedback", "Feedback").withEmoji(Emote.CHAT_BOX.getFormatted())).queue();

    }
}

package de.derioo.module.predefined.suggestion;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.module.Module;
import de.derioo.utils.Emote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SuggestionModule extends Module {

    private final DiscordBot bot;
    private final SuggestionRepo repo;
    private final ConfigRepo configRepo;


    public SuggestionModule(DiscordBot bot) {
        super(bot, "suggestions");
        this.bot = bot;
        this.repo = (SuggestionRepo) bot.getRepo(SuggestionRepo.class);
        this.configRepo = (ConfigRepo) bot.getRepo(ConfigRepo.class);
    }

    @Override
    public void once() throws Throwable {
        updateOrSendEmbed(Config.Id.Channel.SUGGESTION_CREATE_CHANNEL, getEmbed().build(), ActionRow.of(Button.success("create-suggestion", "Vorschlag -> ⭐")));
    }

    private EmbedBuilder getEmbed() {
        return DiscordBot.Default.builder()
                .setAuthor("")
                .setTitle("Varilx Vorschläge")
                .setColor(Color.GREEN)
                .setDescription("""
                        Du hast eine Idee oder ein Verbesserungsvorschlag für den Server?
                        Dann drück einfach auf den Button unter dieser Nachricht.
                        bitte beachte dabei, dass dein Vorschlag mit sofortiger Wirkung in eine Umfrage umgewandelt wird, und jeder sie lesen kann!""")
                .setImage("https://cdn.discordapp.com/attachments/1055223755909111808/1160507955507101736/Varilx_Tube-hosting_version.png?ex=6534ea41&is=65227541&hm=e2dad9d371a1f8a26f84ab29871fc2a754b0135ccbc76c557d9f7c30dbaf371f&");
    }

    @ModuleListener
    public void onCreation(@NotNull ButtonInteractionEvent event) {
        if (event.getButton().getId() == null) return;
        switch (event.getButton().getId().toLowerCase()) {
            case "create-suggestion" -> {
                TextInput name =
                        TextInput.create("suggestion", "Dein Vorschlag", TextInputStyle.PARAGRAPH)
                                .setPlaceholder("z. B. \"Ich finde das Feature XY sollte so verändert werden\"")
                                .setMinLength(10)
                                .setRequired(true)
                                .build();
                Modal modal = Modal.create("new-suggestion", "Vorschlag").addComponents(ActionRow.of(name)).build();
                event.replyModal(modal).queue();
            }
            default -> {
                if (event.getButton().getId().startsWith("delete-suggestion-")) {
                    ObjectId id = new ObjectId(Arrays.stream(event.getButton().getId().split("-")).toList().getLast());

                    Suggestion suggestion = repo.findFirstById(id);

                    ConfigData config = bot.get(event.getGuild());

                    Long suggestionChannelId = config.getChannels().get(Config.Id.Channel.SUGGESTION_CHANNEL.name());

                    TextChannel suggestionChannel = event.getGuild().getChannelById(TextChannel.class, suggestionChannelId);

                    Message message = suggestionChannel.getHistoryAround(suggestion.getMessageId(), 2).complete().getMessageById(suggestion.getMessageId());

                    message.delete().queue();


                    Message adminMessage = event.getChannel().getHistoryAround(suggestion.getAdminMessageId(), 2).complete().getMessageById(suggestion.getAdminMessageId());

                    adminMessage.delete().queue();

                    repo.delete(suggestion);

                    event.reply("Erfolgreich gelöscht").setEphemeral(true).queue();
                    return;
                }
                if (event.getButton().getId().startsWith("accept-suggestion-") || event.getButton().getId().startsWith("decline-suggestion-")) {
                    String last = Arrays.stream(event.getButton().getId().split("-")).toList().getLast();
                    ObjectId id = new ObjectId(last);
                    Suggestion suggestion = repo.findFirstById(id);
                    suggestion.setStatus(event.getButton().getId().startsWith("accept") ? Suggestion.Status.ACCEPTED : Suggestion.Status.DECLINED);

                    ConfigData config = bot.get(event.getGuild());

                    Long suggestionChannelId = config.getChannels().get(Config.Id.Channel.SUGGESTION_CHANNEL.name());

                    TextChannel suggestionChannel = event.getGuild().getChannelById(TextChannel.class, suggestionChannelId);

                    Message message = suggestionChannel.getHistoryAround(suggestion.getMessageId(), 2).complete().getMessageById(suggestion.getMessageId());

                    message.editMessageEmbeds(
                            new EmbedBuilder(message.getEmbeds().getFirst())
                                    .setTitle("Vorschlag " + (suggestion.getStatus().equals(Suggestion.Status.ACCEPTED) ? "angenommen! :white_check_mark:" : "abgelehnt!   :no_entry_sign:"))
                                    .setColor(suggestion.getStatus().equals(Suggestion.Status.ACCEPTED) ? Color.GREEN : Color.RED)
                                    .build()
                    ).queue();
                    message.clearReactions().queue();


                    repo.save(suggestion);


                    event.reply("Erfolgreich " + (suggestion.getStatus().equals(Suggestion.Status.ACCEPTED) ? "aktzeptiert" : "abgelehnt")).setEphemeral(true).queue();
                }
            }
        }

    }


    @ModuleListener
    public void onRealCreation(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("new-suggestion")) return;
        for (Suggestion suggestion : repo.findAll()) {
            if (!suggestion.getGuildId().equals(event.getGuild().getIdLong())) continue;
            if (!suggestion.getUserId().equals(event.getUser().getIdLong())) continue;
            if (suggestion.getId().getDate().getTime() + TimeUnit.MINUTES.toMillis(30) > System.currentTimeMillis()) {
                event.reply("Du kannst nur alle 30min etwas vorschlagen").setEphemeral(true).queue();
                return;
            }
        }


        Suggestion suggestion = Suggestion.builder()
                .id(new ObjectId())
                .userId(event.getUser().getIdLong())
                .guildId(event.getGuild().getIdLong())
                .status(Suggestion.Status.NONE)
                .suggestion(event.getValue("suggestion").getAsString())
                .build();

        ConfigData config = bot.get(event.getGuild());

        Long suggestionChannelId = config.getChannels().get(Config.Id.Channel.SUGGESTION_CHANNEL.name());
        Long suggestionAdminChannelId = config.getChannels().get(Config.Id.Channel.SUGGESTION_ADMIN_CHANNEL.name());

        TextChannel suggestionChannel = event.getGuild().getChannelById(TextChannel.class, suggestionChannelId);

        MessageEmbed embed = DiscordBot.Default.builder()
                .setAuthor(null)
                .setColor(Color.GREEN)
                .setTitle("Varilx Vorschläge")
                .addField("**`Vorschlag / Idee: `**", suggestion.getSuggestion(), false)
                .addField("Idee kommt von: ", event.getUser().getAsMention(), false)
                .build();
        suggestionChannel.sendMessageEmbeds(embed)
                .queue(message -> {
                    suggestion.setMessageId(message.getIdLong());
                    message.addReaction(Emote.UPVOTE.getFormatted()).queue();
                    message.addReaction(Emote.DOWNVOTE.getFormatted()).queue();
                    repo.save(suggestion);
                });

        TextChannel suggestionAdminChannel = event.getGuild().getChannelById(TextChannel.class, suggestionAdminChannelId);

        suggestionAdminChannel.sendMessageEmbeds(embed)
                .addActionRow(Button.primary("accept-suggestion-" + suggestion.getId().toString(), "Angenommen"),
                        Button.danger("decline-suggestion-" + suggestion.getId().toString(), "Abgelehnt"),
                        Button.primary("delete-suggestion-" + suggestion.getId().toString(), "Löschen"))
                .queue(message -> {
                    suggestion.setAdminMessageId(message.getIdLong());
                    repo.save(suggestion);
                });
        event.reply("Der Vorschlag wurde erfolgreich in " + suggestionChannel.getAsMention() + " gesendet").setEphemeral(true).queue();
    }


}

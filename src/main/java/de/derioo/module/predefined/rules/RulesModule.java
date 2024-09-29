package de.derioo.module.predefined.rules;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.module.Module;
import de.derioo.utils.Emote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class RulesModule extends Module {
    public RulesModule(DiscordBot bot) {
        super(bot, "rules");

    }

    @Override
    public void once() {
        Config config = Config.get(bot.getRepo(ConfigRepo.class));
        for (Guild guild : bot.getJda().getGuilds()) {
            if (!config.getData().get(guild.getId()).getChannels().containsKey(Config.Id.Channel.RULE_CHANNEL.name()))
                continue;
            Long channelId = config.getData().get(guild.getId()).getChannels().get(Config.Id.Channel.RULE_CHANNEL.name());
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

    private void sendNewTicketMessage(@NotNull TextChannel channel) {
        EmbedBuilder embed =
                DiscordBot.Default.builder()
                        .setColor(Color.GREEN)
                        .setTitle("Varilx Feedback ")
                        .setDescription("""
                                **Discord | Richtlinen/Regelwerk**
                                                                
                                Da wir aber Natürlich ein gemütliches Miteinander haben\
                                bitten wir euch das Regelwerk durchzulesen damit es zu\
                                 keinen Konflikten kommt ▸


                                > - §1. Nicknames dürfen keine beleidigenden oder anderen verbotenen oder geschützen Namen oder Namensteile enthalten.

                                > - §2. Das Konsumieren sowie das Verherrlichen von illegalen Substanzen / Drogen, darunter fallen ebenso: Alkohol; Zigaretten und Cannabis. Die Thematik sollte vermieden werden.

                                > - §3. Jegliche Art von Werbung ist auf diesem Server untersagt. Ggf. kann sich an einen zuständigen Admin gewandt werden, um über eine Möglichkeit zur Werbung zu verhandeln.

                                > - §4. Private Daten wie Telefonnummern, Adressen, Passwörter und ähnlichem dürfen nicht öffentlich ausgetauscht werden.

                                > - §5. Alle Regeln sind einzuhalten ansonsten wird es mit Bannen, einem Kick oder anderweitig Bestraft!

                                > - §6. Die Regeln dienen zur Verhaltensregeln und können jenach Situation vom den Teammitgliedern geändert werden.

                                > - §7. Das Mitschneiden von Gesprächen ist auf dem gesamten Server nur nach Absprache mit den anwesenden Benutzern des entsprechenden Channels erlaubt. Willigt ein User nicht der Aufnahme ein, ist die Aufnahme des Gesprächs verboten.

                                > - §8. Behandle alle mit Respekt. Belästigung, Hexenjagd, Sexismus, Rassismus oder Volksverhetzung werden absolut nicht toleriert.

                                > - §9. Keine NSFW- oder obszönen Inhalte. Dazu zählen Texte, Bilder oder Links mit Nacktheit, Sex, schwerer Gewalt oder anderen grafisch verstörenden Inhalten.

                                > - §10. Es dürfen keine Bots mit dem Discord Server verbunden werden. Bots dürfen nur in ausgewiesenen Channels verbunden werden und auch nur dann, wenn kein weiterer Bot in dem Channel aktiv ist.

                                > - §11. Server Admins, Moderatoren oder anderweitig befugte Admins haben volles Weisungsrecht. Das Verweigern einer bestimmten Anweisung kann zu einem Kick oder Bann führen.

                                > - §12. Avatare dürfen keine pornographischen, rassistischen oder beleidigenden Inhalte beinhalten.

                                > - §13. Der Umgang mit anderen Discord Benutzern sollte stets freundlich sein. Verbale Angriffe gegen andere User sind strengstens untersagt.

                                > - §14. Das Einspielen von eigener Musik, oder das Übertragen von anderen nicht erwünschten Tönen ist untersagt.

                                Wir bitten auch die Discord-Terms/Guidelines Durchzulesen!
                                > **https://discord.com/terms**
                                > **https://discord.com/guidelines**

                                **Discord | Minecraft Regelwerk**
                                > - Wir besitzen natürlich auch für unseren Server ein Regelwerk, und bitten dieses auch einmal durchzulesen!
                                > **https://regelwerk.varilx.de/**\s

                                Ich hoffe du hast das **Discord Regelwerk/Richtlinien** gründlich durchgelesen, wenn du das hier liest klicke bitte auf den check Reaktions Button um die Regeln zu **Akzeptieren!**""")
                        .setImage("https://media.discordapp.net/attachments/1067809862744019025/1289691157928083577/Varilx.DE_Background.png?ex=66fa66bd&is=66f9153d&hm=35ba8f092d27ed7a0cddc68ef45d7e0d4ac05102cf849ab85af467d7f7eec8a1&=&format=webp&quality=lossless&width=550&height=309");
        channel.sendMessageEmbeds(embed.build()).setActionRow(
                Button.success("accept-rules", "Regeln aktzeptieren").withEmoji(Emote.YES.getFormatted()),
                Button.link("https://discord.com/terms", "TOS").withEmoji(Emote.DISCORD.getFormatted()),
                Button.link("https://discord.com/guidelines", "Guidelines").withEmoji(Emote.DISCORD_LOGO.getFormatted()),
                Button.link("https://regelwerk.varilx.de/", "Forum").withEmoji(Emote.VARILX.getFormatted()),
                Button.link("https://tube-hosting.com/home", "Partner").withEmoji(Emote.TUBE_HOSTING.getFormatted())).queue();

    }

    @ModuleListener
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getButton().getId().equalsIgnoreCase("accept-rules")) return;
        List<Role> roleObjects = bot.get(event.getGuild()).getRoleObjects(Config.Id.Role.RULE_ACCEPT_ROLES, event.getGuild());
        for (Role roleObject : roleObjects) {
            event.getGuild().addRoleToMember(event.getUser(), roleObject).queue();
        }
        event.reply("Regeln aktzeptiert!").setEphemeral(true).queue();
    }

}

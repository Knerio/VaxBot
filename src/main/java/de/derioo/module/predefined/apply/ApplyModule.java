package de.derioo.module.predefined.apply;

import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.module.Module;
import de.derioo.utils.Emote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class ApplyModule extends Module {
    public ApplyModule(DiscordBot bot) {
        super(bot, "apply");

    }

    @Override
    public void once() {
        updateOrSendEmbed(Config.Id.Channel.APPLY_CHANNEL, getEmbed().build(), ActionRow.of(
                Button.link("https://forum.varilx.de/forum/topic/197-wir-suchen-dich%21-%7C-varilxde-bewerbungsphase/", "Forumsbeitrag")
                        .withEmoji(Emoji.fromUnicode("\uD83D\uDD17")),
                Button.link("https://forum.varilx.de/bewerben/", "Bewerben")
                        .withEmoji(Emote.VARILX.getFormatted()),
                Button.link("https://forum.varilx.de/forum/topic/291-tutorial-%7C-wie-bewirbt-man-sich/", "Bewerbungstutorial")
                        .withEmoji(Emote.VARILX.getFormatted()))
        );
    }

    private EmbedBuilder getEmbed() {
        return DiscordBot.Default.builder()
                .setColor(Color.GREEN)
                .setTitle("Varilx Apply")
                .addField("Unser Netzwerk Varilx.DE braucht Helfende Hände!", """
                        Hallo liebe Varilx Community, Wir von **Varilx.DE** sind derzeit auf der Suche 
                        nach engagierten Bewerbern für unser Server-Team.
                        Falls du Interesse daran hast, uns bei
                        **Support, Entwicklung, Content und Architektur** zu unterstützen,
                        dann bist du bei uns an der richtigen Stelle.
                        Aufgrund von fehlendem Support, unzureichendem Content
                        und vielen anderen Aufgabengebiete haben wir diese Bewerbungsphase ins Leben gerufen.
                        Zögere nicht und **bewirb dich jetzt!**
                        **Wo finde ich alle Informationen?**
                        Lese dir bitte unseren ganzen Forum beitrag zum Bewerben durch, bevor du dich bewerben willst (Erster Button unter dieser Nachricht)!
                        **Wo kann ich mich Bewerben?**
                        Unter https://forum.varilx.de/bewerben/""", false)
                .setImage("https://cdn.discordapp.com/attachments/1104111151937245284/1139017969444339722/ValuniaNET-Thumbddnail-Wiederhergestellt.png?ex=66fab165&is=66f95fe5&hm=21bbc3a0892ab4fe6889a57ef80fbc5b52f6dec8571b9490f70875dda86c07f1&");

    }


}

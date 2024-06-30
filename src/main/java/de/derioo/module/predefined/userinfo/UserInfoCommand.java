package de.derioo.module.predefined.userinfo;

import de.derioo.bot.DiscordBot;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Command(name = "userinfo")
public class UserInfoCommand {

    private static final Map<User.UserFlag, String> BADGE_EMOJIS = new HashMap<>(
            Map.of(
                    User.UserFlag.HYPESQUAD_BALANCE, "<:hype1:1140656206747287603>",
                    User.UserFlag.HYPESQUAD_BRILLIANCE, "<:hype2:1140656191886872646>",
                    User.UserFlag.HYPESQUAD_BRAVERY, "<:hype3:1140656174140756049>",
                    User.UserFlag.ACTIVE_DEVELOPER, "<:active_dev:1140656159662030888>",
                    User.UserFlag.VERIFIED_DEVELOPER, "<:dev:1140656146584178770>",
                    User.UserFlag.PARTNER, "<:partner:1140656134504599552>"
            )

    );

    @Execute
    @Description("Zeigt dir Infos über einen Nutzer")
    public void execute(@Arg("nutzer") @Description("Wessen Infos möchtest du sehen") Optional<User> who,
                        @Context SlashCommandInteractionEvent event) {
        Member toShow = event.getGuild().getMemberById(who.orElse(event.getUser()).getIdLong());
        User user = toShow.getUser();
        event.replyEmbeds(DiscordBot.Default.builder()
                .setColor(Color.GREEN)
                .setTitle("Informationen zu " + user.getGlobalName())
                .setThumbnail("https://cdn.discordapp.com/attachments/1055223755909111808/1160508079419424840/Unbenanntdsadasd-2.png?ex=6534ea5f&is=6522755f&hm=00ea7dd8a3fd0c5dfcfccfa6952527b679094abf07d22143fee44b0b7221aa4a&")
                .setDescription(String.join("\n", List.of(
                        "<:varilx_user:1139957321196376107> Anzeigename: **" + user.getEffectiveName() + "**",
                        "<:varilx_user:1139957321196376107> Name: **" + user.getName() + "**\n",
                        "<:varilx_clendar:1139956980576960653> Account erstellt am: **<t:" + getUnix(user.getTimeCreated().toInstant().toEpochMilli()) + ":D>**",
                        "<:varilx_clock:1139957097522528257> Account erstellt vor: **<t:" + getUnix(user.getTimeCreated().toInstant().toEpochMilli()) + ":R>**\n",
                        "<:varilx_clendar:1139956980576960653> Server betreten am: **<t:" + getUnix(toShow.getTimeJoined().toInstant().toEpochMilli()) + ":D>**",
                        "<:varilx_clock:1139957097522528257>Server betreten vor: **<t:" + getUnix(toShow.getTimeJoined().toInstant().toEpochMilli()) + ":R>**\n",
                        "<:vax_pen:1140659710836621342 Nickname: **" + toShow.getNickname() + "**",
                        "<:vax_bot:1140659750724435979> Bot: **" + (user.isBot() ? "Ja" : "Nein") + "**\n",
                        "<:varilx_star:1139957135707484290> Badges:", getFormattedBadges(user))
                ))
                .build()).queue();
    }

    private @NotNull String getFormattedBadges(@NotNull User user) {
        if (user.getFlags().isEmpty()) return "Keine Badges";
        List<String> list = new ArrayList<>();
        for (User.UserFlag flag : user.getFlags()) {
            if (BADGE_EMOJIS.containsKey(flag)) {
                list.add("- " + BADGE_EMOJIS.get(flag) + " " + getBadgeName(flag));
            }
        }
        return String.join("\n", list);
    }

    @Contract(pure = true)
    private @NotNull String getBadgeName(User.@NotNull UserFlag flag) {
        return switch (flag) {
            case HYPESQUAD_BALANCE -> "**Balance**";
            case HYPESQUAD_BRILLIANCE -> "**Brilliance**";
            case HYPESQUAD_BRAVERY -> "**Bravery**";
            case ACTIVE_DEVELOPER -> "**Aktive Entwickler**";
            case VERIFIED_DEVELOPER -> "**Verifizierter Developer**";
            case PARTNER -> "**Besitzer eines Partnerservers**";
            default -> "**Unbekannte Badge**";
        };
    }

    @Contract(pure = true)
    private @NotNull Long getUnix(Long l) {
        return l / 1000L;
    }
}

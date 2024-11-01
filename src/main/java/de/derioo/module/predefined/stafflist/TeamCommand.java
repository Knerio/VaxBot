package de.derioo.module.predefined.stafflist;

import de.derioo.annotations.NeedsRole;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.MessageFormat;
import java.util.Optional;

@Command(name = "team")
@Description("Ein Command um leute upzuranken oder zum Hinzufügen")
public class TeamCommand {

    private final DiscordBot bot;

    public TeamCommand(DiscordBot bot) {
        this.bot = bot;
    }

    @Execute(name = "add")
    @NeedsRole(Config.Id.Role.TEAM_ADD)
    public void add(
            @Arg("user") @Description("Der Nutzer der geadded werden soll!") User user,
            @Arg("rang") @Description("Der Rang, der der Nutzer nun hat!") Role role,
            @Context SlashCommandInteractionEvent event
    ) {
        Guild guild = event.getGuild();
        Member member = guild.getMemberById(user.getIdLong());
        guild.addRoleToMember(member, role).queue();
        TextChannel channel = guild.getTextChannelById(bot.get(guild).getChannels().get(Config.Id.Channel.PROMOTE_CHANNEL.name()));
        channel.sendMessage(MessageFormat.format("""
                        > {0}\s
                        > **Team Neuzugang**
                        >\s
                        > Wir begrüßen {1} im Bereich {2} und wünschen eine lange und gute Zusammenarbeit!
                        >\s
                        > Mit freundlichen Grüßen,
                        > {3}""", bot.get(guild).getMentions(Config.Id.Role.TEAM_ROLE, guild), user.getAsMention(), role.getAsMention(), bot.getJda().getSelfUser().getAsMention()))
                .queue();
        event.reply("Erfolgreich hinzugefügt").setEphemeral(true).queue();
    }

    @Execute(name = "uprank")
    @NeedsRole(Config.Id.Role.TEAM_ADD)
    public void uprank(
            @Arg("user") @Description("Der Nutzer der geupranked werden soll!") User user,
            @Arg("neu") @Description("Der neue Rang, der der Nutzer nun hat!") Role role,
            @OptionalArg("alt") @Description("Der alte Range, der entfernt werden soll") Role old,
            @Context SlashCommandInteractionEvent event
    ) {
        Guild guild = event.getGuild();
        Member member = guild.getMemberById(user.getIdLong());
        if (old != null) guild.removeRoleFromMember(member, old).queue();
        guild.addRoleToMember(member, role).queue();
        TextChannel channel = guild.getTextChannelById(bot.get(guild).getChannels().get(Config.Id.Channel.PROMOTE_CHANNEL.name()));
        channel.sendMessage(
                        MessageFormat.format("""
                                > {0}\s
                                > **Team Uprank**
                                >\s
                                > {1} wurde auf {2} befördert
                                >\s
                                > Mit freundlichen Grüßen,
                                > {3}""", bot.get(guild).getMentions(Config.Id.Role.TEAM_ROLE, guild), user.getAsMention(), role.getAsMention(), bot.getJda().getSelfUser().getAsMention()))
                .queue();
        event.reply("Erfolgreich geupranked").setEphemeral(true).queue();
    }

}

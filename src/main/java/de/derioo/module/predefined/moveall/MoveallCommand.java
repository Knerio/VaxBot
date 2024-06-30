package de.derioo.module.predefined.moveall;

import de.derioo.annotations.NeedsRole;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.optional.OptionalArg;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Command(name = "moveall")
public class MoveallCommand {

    @Execute(name = "to")
    @NeedsRole(Config.Id.Role.MOVE_ALL)
    @Description("Move alle die in deinem Channel sind wo anders hin!")
    public void moveAllTo(@Arg("wohin") @Description("Wohin sollen alle gemoved werden?") Channel to,
                          @Arg("woher") @Description("Woher sollen die User gemoved werden?") Optional<Channel> from,
                          @Context SlashCommandInteractionEvent event) {
        Member member = Objects.requireNonNull(event.getGuild()).getMemberById(event.getUser().getIdLong());
        GuildVoiceState voiceState = member.getVoiceState();
        if (!to.getType().isAudio() || (from.isPresent() && !from.get().getType().isAudio())) {
            DiscordBot.Default.replyError(event, "Bitte gib einen Voice Channel an!");
        }
        List<Member> members;
        if (from.isEmpty()) {
            if (voiceState == null || !voiceState.inAudioChannel()) {
                DiscordBot.Default.replyError(event, "Du kannst aktuelle nicht moven");
                return;
            }
            members = Objects.requireNonNull(voiceState.getChannel()).getMembers();
        } else {
            members = ((AudioChannel) from.get()).getMembers();
        }

        for (Member voiceMember : members) {
            event.getGuild().moveVoiceMember(voiceMember, (AudioChannel) to).queue();
        }
        DiscordBot.Default.reply(event, "Du hast " + members.size() + " Nutzer gemoved!");
    }


}

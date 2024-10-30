package de.derioo.bot.args;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class MemberArgument extends ArgumentResolver<User, Member> {
    @Override
    protected ParseResult<Member> parse(Invocation<User> invocation, Argument<Member> context, String argument) {
        System.out.println(argument);
        return ParseResult.success(invocation.context().get(SlashCommandInteractionEvent.class).get().getGuild().retrieveMemberById(argument).complete());
    }

}

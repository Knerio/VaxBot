package de.derioo.bot.args;

import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UserArgument extends ArgumentResolver<User, User> {
    @Override
    protected ParseResult<User> parse(Invocation<User> invocation, Argument<User> context, String argument) {
        System.out.println(argument);
        return ParseResult.success(invocation.sender().getJDA().retrieveUserById(argument).complete());
    }

}

package de.derioo.module.predefined.spoof;

import de.derioo.annotations.NeedsAdmin;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.join.Join;
import dev.rollczi.litecommands.jda.permission.DiscordPermission;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Command(name = "spoof")
@Description("Psst...")
public class SpoofCommand {

    private final SpoofModule module;

    public SpoofCommand(SpoofModule module) {
        this.module = module;
    }

    @Execute
    @Description("Du willst das wirklich wissen?")
    @NeedsAdmin
    public void exec(@Arg("user") @Description("...") User user, @Arg("reply") @Description("...") Optional<Integer> optionalReplyId, @Join("message") @Description("...") String message, @Context SlashCommandInteractionEvent event) throws IOException, ExecutionException, InterruptedException {
        if (user.equals(event.getJDA().getSelfUser())) {
            if (optionalReplyId.isPresent()) {
                Message replyMessage = event.getChannel().retrieveMessageById(optionalReplyId.get()).complete();
                replyMessage.reply(message).queue();
            } else {
                event.getChannel().sendMessage(message).queue();
            }

        } else {
            Webhook webhook = module.createWebhook(event.getGuild().getMember(user), event.getChannel().asTextChannel());
            webhook.sendMessage(message).queue();
            webhook.delete().queue();
        }

        event.reply("Executed...").setEphemeral(true).queue();
    }

}

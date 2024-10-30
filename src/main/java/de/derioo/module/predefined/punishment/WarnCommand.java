package de.derioo.module.predefined.punishment;

import de.derioo.annotations.NeedsRole;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.join.Join;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

@Command(name = "warn")
@Description("Ein Warnungscommand")
public class WarnCommand {

    private final DiscordBot bot;
    private final WarnRepo repo;

    public WarnCommand(DiscordBot bot) {
        this.bot = bot;
        this.repo = (WarnRepo) bot.getRepo(WarnRepo.class);
    }

    @NeedsRole(Config.Id.Role.WARN_USER)
    @Execute
    public void execute(@Arg("user") @Description("Der User der gewarned werden soll") User user, @Join("grund") @Description("Der Grund für den timeout") String reason, @Context SlashCommandInteractionEvent event) {
        Member member = event.getGuild().getMember(user);

        Warn warn = this.repo.findFirstById(user.getIdLong());
        if (warn == null) warn = new Warn(user.getIdLong(), new HashMap<>());

        warn.getWarns().putIfAbsent(event.getGuild().getId(), 0);
        warn.getWarns().compute(event.getGuild().getId(), (k, count) -> {

            user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessageEmbeds(DiscordBot.Default.builder()
                                .setColor(Color.RED).setTitle("Du wurdest verwarnt")
                                .setDescription("Du wurdest verwarnt. Grund: \n```\n" + reason + "\n```\n Du hast nun " + (count + 1) + " Verwarnungen " + (count == 2 ? "Du bist nun wegen zu vielen Verwarnungen gebannt!" : ""))
                        .build()).queue();
            });
            if (count == 2) {
                event.getGuild().getMember(user).ban(0, TimeUnit.SECONDS).reason(reason).queue();
            }

            event.reply("Du hast den Nutzer erfolgreich gewarnt " + (count == 2 ? "und gebannt" : "")).setEphemeral(true).queue();

            return count + 1;
        });

        repo.save(warn);



    }


}

package de.derioo.module.predefined.spoof;

import de.derioo.bot.DiscordBot;
import de.derioo.module.Module;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class SpoofModule extends Module {
    public SpoofModule(DiscordBot bot) {
        super(bot, "spoof");
    }

    public Webhook createWebhook(Member member, TextChannel channel) throws ExecutionException, InterruptedException, IOException {
        String nickname = member.getNickname();
        return channel.createWebhook(nickname == null ? member.getEffectiveName() : nickname)
                .setAvatar(Icon.from(URI.create(member.getUser().getAvatarUrl()).toURL().openStream()))
                .complete();
    }


}

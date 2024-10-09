package de.derioo.module;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.javautils.common.MathUtility;
import de.derioo.utils.PasteBinUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public abstract class Module {

    protected final DiscordBot bot;

    private final String name;

    private final Map<Long, MathUtility.ThrowableRunnable> runnableMap = new HashMap<>();

    protected Module(DiscordBot bot, String name) {
        this.bot = bot;
        this.name = name;
        runnableMap.put(10_000L, this::timer);

        registerListener(this);
    }

    public void registerListener(Object instance) {
        bot.getJda().addEventListener(new ListenerAdapter() {

            @Override
            public void onGenericEvent(@NotNull GenericEvent event) {
                for (Method declaredMethod : instance.getClass().getDeclaredMethods()) {
                    if (!declaredMethod.isAnnotationPresent(ModuleListener.class)) continue;
                    if (declaredMethod.getParameters().length != 1) continue;
                    Parameter first = declaredMethod.getParameters()[0];
                    if (first.getType().isAssignableFrom(event.getClass())) {
                        try {
                            declaredMethod.invoke(instance, event);
                        } catch (Exception e) {
                            logThrowable(bot, e);
                        }
                    }
                }
            }
        });
    }


    public final void start() {
        Timer timer = new Timer(name);

        try {
            once();
        } catch (Throwable throwable) {
            logThrowable(bot, throwable);
        }
        runnableMap.forEach((duration, runnable) -> {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } catch (Throwable throwable) {
                        logThrowable(bot, throwable);
                    }
                }
            }, duration + 10, duration);
        });
    }


    public void timer() throws Throwable {

    }

    public static void logThrowable(DiscordBot bot, Throwable throwable) {
        Config config = Config.get(bot.getRepo(ConfigRepo.class));
        for (Guild guild : bot.getJda().getGuilds()) {
            Long l = config.get(guild).getChannels().getOrDefault(Config.Id.Channel.ERROR_CHANNEL.name(), null);
            if (l == null) continue;
            try {
                TextChannel textChannel = guild.getChannelById(TextChannel.class, l);
                textChannel.sendMessageEmbeds(DiscordBot.Default.error(throwable, true)
                                .setDescription("Siehe Link für Stacktrace")
                                .build())
                        .addActionRow(Button.link(PasteBinUtil.createPasteOfThrowable(throwable).toString(), "Paste.gg"))
                        .queue();
                throwable.printStackTrace();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public final void setTimers(Long duration, MathUtility.ThrowableRunnable runnable) {
        runnableMap.put(duration, runnable);
    }


    public void once() throws Throwable {

    }


    public void updateOrSendEmbed(Config.Id.Channel channelConfig, MessageEmbed embed, ActionRow... rows) {
        Config config = Config.get(bot.getRepo(ConfigRepo.class));
        for (Guild guild : bot.getJda().getGuilds()) {
            if (!config.getData().get(guild.getId()).getChannels().containsKey(channelConfig.name()))
                continue;
            Long channelId = config.getData().get(guild.getId()).getChannels().get(channelConfig.name());
            TextChannel channel = guild.getChannelById(TextChannel.class, channelId);
            List<Message> messages = channel.getHistory().retrievePast(1).complete();
            if (messages.isEmpty()) {
                channel.sendMessageEmbeds(embed).setComponents(rows).queue();
            } else {
                Message message = messages.getFirst();
                if (message.getAuthor().getIdLong() == bot.getJda().getSelfUser().getIdLong()) {
                    message.editMessageEmbeds(embed).setComponents(rows).queue();
                }
            }
        }
    }

}

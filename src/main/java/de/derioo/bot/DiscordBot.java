package de.derioo.bot;

import de.derioo.annotations.NeedsAdmin;
import de.derioo.annotations.NeedsRole;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.config.commands.ChannelSetCommand;
import de.derioo.config.local.LangConfig;
import de.derioo.config.local.LocalConfig;
import de.derioo.config.repository.ConfigRepo;
import de.derioo.javautils.common.DateUtility;
import de.derioo.javautils.common.StringUtility;
import de.derioo.module.predefined.apply.ApplyModule;
import de.derioo.module.predefined.boost.BoostModule;
import de.derioo.module.predefined.clear.ClearCommand;
import de.derioo.module.predefined.eightball.EightballCommand;
import de.derioo.module.predefined.feedback.FeedbackModule;
import de.derioo.module.predefined.giveaway.GiveAwayModule;
import de.derioo.module.predefined.giveaway.commands.GiveAwayCommand;
import de.derioo.module.predefined.giveaway.db.GiveawayRepo;
import de.derioo.module.predefined.join.JoinModule;
import de.derioo.module.predefined.moveall.MoveallCommand;
import de.derioo.module.predefined.punishment.*;
import de.derioo.module.predefined.rules.RulesModule;
import de.derioo.module.predefined.stafflist.StafflistModule;
import de.derioo.module.predefined.stafflist.TeamCommand;
import de.derioo.module.predefined.statuschanger.StatusChangerModule;
import de.derioo.module.predefined.suggestion.Suggestion;
import de.derioo.module.predefined.suggestion.SuggestionModule;
import de.derioo.module.predefined.suggestion.SuggestionRepo;
import de.derioo.module.predefined.support.SupportModule;
import de.derioo.module.predefined.ticket.*;
import de.derioo.module.predefined.usercount.UserCountModule;
import de.derioo.module.predefined.userinfo.UserInfoCommand;
import de.derioo.utils.UserUtils;
import dev.rollczi.litecommands.jda.LiteJDAFactory;
import dev.rollczi.litecommands.validator.ValidatorResult;
import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.repository.Repository;
import lombok.Getter;
import lombok.extern.java.Log;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

@Log
public class DiscordBot extends ListenerAdapter {


    @Getter
    private final JDA jda;

    private final LocalConfig config;
    private final MongoManager mongoManager;
    private final LangConfig langConfig;

    private final Map<Class<? extends Repository<?, ?>>, Repository<?, ?>> repositories = new HashMap<>();


    public DiscordBot(@NotNull LocalConfig config, MongoManager mongoManager, LangConfig langConfig) throws InterruptedException {
        this.langConfig = langConfig;
        this.config = config;
        this.mongoManager = mongoManager;

        this.jda = JDABuilder
                .create(config.getToken(), EnumSet.of(GUILD_MEMBERS, GUILD_MESSAGES, GUILD_VOICE_STATES, GUILD_MODERATION, GUILD_MESSAGE_REACTIONS, MESSAGE_CONTENT, DIRECT_MESSAGES))
                .setStatus(OnlineStatus.ONLINE)
                .addEventListeners(this)
                .build();
        jda.awaitReady();

        this.repositories.put(ConfigRepo.class, this.mongoManager.create(ConfigRepo.class));
        this.repositories.put(TicketRepo.class, this.mongoManager.create(TicketRepo.class));
        this.repositories.put(GiveawayRepo.class, this.mongoManager.create(GiveawayRepo.class));
        this.repositories.put(SuggestionRepo.class, this.mongoManager.create(SuggestionRepo.class));
        this.repositories.put(WarnRepo.class, this.mongoManager.create(WarnRepo.class));

        new JoinModule(this).start();
        new StafflistModule(this).start();
        new StatusChangerModule(this).start();
        new TicketModule(this, langConfig).start();
        new SuggestionModule(this).start();
        new UserCountModule(this).start();
        new SupportModule(this).start();
        new FeedbackModule(this).start();
        new BoostModule(this).start();
        new ApplyModule(this).start();
        new RulesModule(this).start();
        GiveAwayModule giveAwayModule = new GiveAwayModule(this);
        giveAwayModule.start();

        LiteJDAFactory.builder(jda)
                .commands(new WarnCommand(this), new TimeoutCommand(), new BanCommand(), new KickCommand(), new ClearCommand(), new UserInfoCommand(), new MoveallCommand(), new GiveAwayCommand(this, giveAwayModule), new ChannelSetCommand(this), new UnclaimCommand(this), new TicketCommand(this), new TeamCommand(this), new EightballCommand())
                .exceptionUnexpected((invocation, throwable, resultHandlerChain) -> {
                    SlashCommandInteractionEvent event = invocation.context().get(SlashCommandInteractionEvent.class).get();
                    String stacktrace = String.join("\n", Arrays.stream(throwable.getStackTrace()).map(StackTraceElement::toString).toList());
                    event.getHook().sendMessageEmbeds(
                            Default.error(throwable)
                                    .setDescription(stacktrace)
                                    .build()
                    ).setEphemeral(true).queue();
                })
                .annotations(configuration -> {
                    configuration.methodValidator(context -> {
                        User sender = context.getInvocation().sender();
                        Member member = context.getInvocation().context().get(SlashCommandInteractionEvent.class).get().getGuild().getMemberById(sender.getIdLong());

                        if (!context.getMethod().isAnnotationPresent(NeedsRole.class) && !context.getMethod().isAnnotationPresent(NeedsAdmin.class)) {
                            logCommand(member, context.getMethod(), context.getArgs());
                            return ValidatorResult.valid();
                        }
                        NeedsRole annotation = context.getMethod().getAnnotation(NeedsRole.class);
                        ConfigData configData = get(member.getGuild());

                        if (context.getMethod().isAnnotationPresent(NeedsAdmin.class) || !configData.getRoles().containsKey(annotation.value().name())) {
                            if (!member.getPermissions().contains(Permission.ADMINISTRATOR))
                                return ValidatorResult.invalid("Dazu hast du keine Rechte!");
                        }
                        if (member.getPermissions().contains(Permission.ADMINISTRATOR)) {
                            logCommand(member, context.getMethod(), context.getArgs());
                            return ValidatorResult.valid();
                        }

                        for (Role role : member.getRoles()) {
                            if (configData.isRoleValid(annotation.value(), role)) {
                                logCommand(member, context.getMethod(), context.getArgs());
                                return ValidatorResult.valid();
                            }
                        }
                        return ValidatorResult.invalid("Dazu hast du keine Rechte!");
                    });
                })
                .build();


        for (Guild guild : jda.getGuilds()) {
            Config configurationObject = Config.get(getRepo(ConfigRepo.class));
            configurationObject.getData().putIfAbsent(guild.getId(), ConfigData.defaultData(guild.getId()));
            getRepo(ConfigRepo.class).save(configurationObject);
        }


    }

    private void logCommand(Member member, Method method, Object[] args) {
        try {
            Long channel = get(member.getGuild()).getChannels().get(Config.Id.Channel.ERROR_CHANNEL.name());
            TextChannel textChannel = member.getGuild().getTextChannelById(channel);
            textChannel.sendMessage(UserUtils.getMention(member) + " is executing \"" + method.getName() + "\" (" + method.getDeclaringClass().getSimpleName() + ", " + String.join(",", Arrays.stream(args).map(Object::toString).toList())).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ConfigData get(Guild guild) {
        return Config.get(getRepo(ConfigRepo.class)).get(guild);
    }

    @SuppressWarnings("unchecked")
    public <E, ID> Repository<E, ID> getRepo(Class<? extends Repository<E, ID>> repositoryClass) {
        return (Repository<E, ID>) this.repositories.get(repositoryClass);
    }


    public static class Default {

        public static void replyError(SlashCommandInteractionEvent event, String msg) {
            event.replyEmbeds(builder().setColor(Color.RED).setDescription(msg).build()).setEphemeral(true).queue();
        }

        public static void reply(SlashCommandInteractionEvent event, String msg) {
            event.replyEmbeds(builder().setDescription(msg).build()).setEphemeral(true).queue();
        }

        public static EmbedBuilder setFooter(EmbedBuilder builder) {
            return new EmbedBuilder().setAuthor("Varilx.de | Bot").setFooter("Gesendet am " + DateUtility.DATE_FORMAT.format(new Date(Calendar.getInstance().getTimeInMillis())));
        }

        public static @NotNull EmbedBuilder builder() {
            return setFooter(new EmbedBuilder());
        }

        public static @NotNull EmbedBuilder changed() {
            return builder()
                    .setColor(Color.GREEN)
                    .setTitle(":white_check_mark: Geändert");
        }

        public static @NotNull EmbedBuilder error(@NotNull Throwable throwable) {
            return error(throwable, false);
        }

        public static @NotNull EmbedBuilder error(@NotNull Throwable throwable, boolean stacktrace) {
            EmbedBuilder builder = builder()
                    .setTitle("Es ist eine Fehler aufgetreten")
                    .setColor(Color.RED)
                    .addField(new MessageEmbed.Field("Fehler", throwable.getClass().getName() + ": " + throwable.getMessage(), false));
            if (stacktrace) {
                builder.addField(new MessageEmbed.Field("StackTrace", "```" +
                        StringUtility.capAtNCharacters(Arrays.stream(throwable.getStackTrace())
                                .map(StackTraceElement::toString).collect(Collectors.joining("\n")), 1018)
                        + "```", false));
                log.log(Level.SEVERE, "Es ist ein Fehler aufgetreten!", stacktrace);
                throwable.printStackTrace();
            }
            return builder;


        }

    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        Config configurationObject = Config.get(getRepo(ConfigRepo.class));
        configurationObject.getData().put(guild.getId(), ConfigData.defaultData(guild.getId()));
        getRepo(ConfigRepo.class).save(configurationObject);
    }
}

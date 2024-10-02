package de.derioo.config.commands;

import de.derioo.annotations.NeedsAdmin;
import de.derioo.annotations.NeedsRole;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.config.repository.ConfigRepo;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.description.Description;
import dev.rollczi.litecommands.annotations.execute.Execute;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TeamMember;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.derioo.config.Config.Id.Data.TEAM_ROLE;

@Command(name = "set")
@Description("Ein Command für Konfigurationen")
public class ChannelSetCommand {

    private final DiscordBot bot;
    private final ConfigRepo repo;

    public ChannelSetCommand(DiscordBot bot) {
        this.bot = bot;
        this.repo = (ConfigRepo) bot.getRepo(ConfigRepo.class);
    }

    @NeedsAdmin
    @Execute(name = "addrole")
    void addRole(@Arg("id")
                     @Description("Die ID, welche neu hinzugefügt werden soll") Config.Id.Role id,
                     @Arg("rolle") @Description("Die Rolle, welcher nun genutzt werden soll") Role role,
                     @Context User sender, @Context SlashCommandInteractionEvent event) {
        set("roles", id.name(), role.getIdLong(), event);
        event
                .replyEmbeds(DiscordBot.Default.changed()
                        .setDescription("Die Rolle " + role.getAsMention() + " wurde nun hinzugefügt")
                        .build()
                )
                .setEphemeral(true).queue();
    }

    @NeedsAdmin
    @Execute(name = "removerole")
    void removeRole(@Arg("id")
                     @Description("Die ID, welche neu hinzugefügt werden soll") Config.Id.Role id,
                     @Arg("rolle") @Description("Die Rolle, welcher nun genutzt werden soll") Role role,
                     @Context User sender, @Context SlashCommandInteractionEvent event) {
        Config config = Config.get(repo);

        Map<String, List<Long>> roles = config.get(event.getGuild()).getRoles();
        roles.putIfAbsent(id.name(), new ArrayList<>());
        roles.get(id.name()).remove(role.getIdLong());
        repo.save(config);

        event
                .replyEmbeds(DiscordBot.Default.changed()
                        .setDescription("Die Rolle " + role.getAsMention() + " wurde nun entfernt")
                        .build()
                )
                .setEphemeral(true).queue();
    }


    @NeedsAdmin
    @Execute(name = "channel")
    void executeChannelSet(@Arg("id")
                           @Description("Die ID, welche neu gesetzt werden soll") Config.Id.Channel id,
                           @Arg("channel") @Description("Der Channel, welcher nun genutzt werden soll") Channel channel,
                           @Context User sender, @Context SlashCommandInteractionEvent event) {
        set(id.name(), channel.getIdLong(), event);
        event
                .replyEmbeds(DiscordBot.Default.changed()
                        .setDescription("Der Channel `" + id + "` ist nun " + channel.getAsMention())
                        .build()
                )
                .setEphemeral(true).queue();
    }

    @NeedsAdmin
    @Execute(name = "category")
    void executeSetCategory(@Arg("id")
                            @Description("Die ID, welche neu gesetzt werden soll") Config.Id.Category id,
                            @Arg("kategorie-id") @Description("Die Kategorie-ID, welcher nun genutzt werden soll") String categoryId, @Context User sender, @Context @NotNull SlashCommandInteractionEvent event) {
        Category category;
        try {
            category = event.getGuild().getCategoryById(categoryId);
        } catch (Exception e) {
            event.getHook().sendMessageEmbeds(DiscordBot
                    .Default.error(e)
                    .setTitle(":x: Fehler")
                    .setDescription("Diese Kategorie exestiert nicht")
                    .build()).queue();
            return;
        }
        set(id.name(), category.getIdLong(), event);
        event.replyEmbeds(DiscordBot.Default.changed()
                        .setDescription("Die Rolle `" + id + "` ist nun " + category.getAsMention())
                        .build()
                )
                .setEphemeral(true).queue();
    }

    @Execute(name = "addteamrole")
    @NeedsAdmin
    public void addTeamRole(@Arg("rolle") @Description("Diese Rolle wird nun als TeamRolle anerkannt") Role teamRole, @Context SlashCommandInteractionEvent event) {
        event.deferReply(true).queue();
        Config config = Config.get(repo);
        ConfigData guildConfig = config.get(event.getGuild());
        List<Long> data = guildConfig.getData(TEAM_ROLE.name(), List.class);
        if (data == null) data = new ArrayList<>();
        data.add(teamRole.getIdLong());
        guildConfig.putData(TEAM_ROLE.name(), data);
        repo.save(config);
        event.getHook().sendMessage("Du hast erfolgreich die Rolle " + teamRole.getAsMention() + " als TeamRolle hinzugefügt").setEphemeral(true).queue();
    }

    @Execute(name = "removeteamrole")
    @NeedsAdmin
    public void removeTeamRole(@Arg("rolle") @Description("Diese Rolle wird nicht mehr als TeamRolle anerkannt") Role teamRole, @Context SlashCommandInteractionEvent event) {
        Config config = Config.get(repo);
        ConfigData guildConfig = config.get(event.getGuild());
        List<Long> data = guildConfig.getData(TEAM_ROLE.name(), List.class);
        if (data == null) data = new ArrayList<>();
        data.remove(teamRole.getIdLong());
        repo.save(config);
        guildConfig.putData(TEAM_ROLE.name(), data);
        event.reply("Du hast erfolgreich die Rolle " + teamRole.getAsMention() + " als TeamRolle entfernt").setEphemeral(true).queue();
    }

    private void set(String id, long longId, SlashCommandInteractionEvent event) {
        set("channels", id, longId, event);
    }

    private void set(@NotNull String type, String id, long longId, SlashCommandInteractionEvent event) {
        Config config = Config.get(repo);

        if (type.equals("roles")) {
            Map<String, List<Long>> roles = config.get(event.getGuild()).getRoles();
            roles.putIfAbsent(id, new ArrayList<>());
            roles.get(id).add(longId);
        } else {
            config.getData().get(event.getGuild().getId()).getChannels().put(id, longId);
        }

        repo.save(config);
    }


}

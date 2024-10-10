package de.derioo.module.predefined.randommeme;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.Module;
import de.derioo.module.predefined.randommeme.db.RandomMeme;
import de.derioo.module.predefined.randommeme.db.RandomMemeRepository;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.commons.io.FileUtils;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class RandomMemeModule extends Module {

    private final Repository<RandomMeme, ObjectId> repo;

    public RandomMemeModule(DiscordBot bot) {
        super(bot, "randommeme");
        this.repo = this.bot.getRepo(RandomMemeRepository.class);
    }

    @ModuleListener
    public void onMessage(MessageReceivedEvent event) throws ExecutionException, InterruptedException, IOException {
        if (event.getChannelType().isGuild()) return;
        switch (event.getMessage().getContentRaw().toLowerCase().split(" ")[0]) {
            case "list" -> {
                list(event);
                return;
            }
            case "find" -> {
                find(event);
                return;
            }
        }
        if (event.getMessage().getAttachments().isEmpty()) return;

        List<RandomMeme> attachments = new ArrayList<>();

        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            File dir = new File("./temp");
            File file = new File("./temp/" + attachment.getId() + ".png");
            dir.mkdirs();
            file.createNewFile();
            attachment.getProxy().downloadToFile(file).get();

            compress(file, attachment.getFileExtension());

            attachments.add(RandomMeme.builder()
                    .id(new ObjectId())
                    .userId(event.getAuthor().getIdLong())
                    .fileExtension(attachment.getFileExtension())
                    .data(new Binary(Files.readAllBytes(file.toPath())))
                    .build());
        }

        for (Guild guild : this.bot.getJda().getGuilds()) {
            if (!hasPermission(event, guild)) continue;
            for (RandomMeme meme : attachments) {
                meme.setGuildId(guild.getIdLong());
                this.repo.save(meme);
                event.getMessage().reply(meme.getId().toString()).addFiles(FileUpload.fromData(meme.getData().getData(), "image." + meme.getFileExtension())).queue();
            }
        }
        FileUtils.deleteDirectory(new File("./temp"));
    }

    private void find(MessageReceivedEvent event) throws IOException, ExecutionException, InterruptedException {
        if (event.getMessage().getAttachments().size() != 1) return;

        byte[] data = getBytes(event.getMessage().getAttachments().getFirst());

        for (Guild guild : this.bot.getJda().getGuilds()) {
            Message waitMessage = event.getMessage().reply("⚙️ Meme wird gesucht ⚙️").complete();
            if (!hasPermission(event, guild)) continue;

            for (RandomMeme randomMeme : ((RandomMemeRepository) this.repo).findManyByData(new Binary(data))) {
                if (!Arrays.equals(randomMeme.getData().getData(), data)) continue;
                waitMessage.editMessage("Das Meme wurde gefunden (" + randomMeme.getId() + ")")
                        .setFiles(FileUpload.fromData(randomMeme.getData().getData(), "image." + randomMeme.getFileExtension()))
                        .queue();
                return;
            }
        }
    }

    private byte[] getBytes(Message.Attachment attachment) throws IOException, ExecutionException, InterruptedException {
        File dir = new File("./temp");
        File file = new File("./temp/" + attachment.getId() + ".png");
        dir.mkdirs();
        file.createNewFile();
        attachment.getProxy().downloadToFile(file).get();

        compress(file, attachment.getFileExtension());

        byte[] bytes = Files.readAllBytes(file.toPath());

        FileUtils.deleteDirectory(new File("./temp"));

        return bytes;
    }

    private void list(MessageReceivedEvent event) {
        for (Guild guild : this.bot.getJda().getGuilds()) {
            Message waitMessage = event.getMessage().reply("⚙️ Memes werden geladen ⚙️").complete();
            if (!hasPermission(event, guild)) continue;
            int page = getPage(event.getMessage());
            List<RandomMeme> list = ((RandomMemeRepository) this.repo).pageByGuildId(guild.getIdLong(), Pagination.of(10).page(page));

            waitMessage.editMessage("Seite " + page + ": " + list.stream().map(RandomMeme::getId).map(ObjectId::toString)
                            .collect(Collectors.joining(" ")))
                    .setFiles(list.stream().map(randomMeme -> FileUpload.fromData(randomMeme.getData().getData(), "image." + randomMeme.getFileExtension())).toList())
                    .queue();
        }
    }

    private boolean hasPermission(MessageReceivedEvent event, Guild guild) {
        ConfigData configData = this.bot.get(guild);
        List<Long> data = configData.getData(Config.Id.User.RANDOM_MEME_USERS.name(), List.class);
        if (data == null) return true;
        return data.contains(event.getAuthor().getIdLong());
    }

    private int getPage(Message message) {
        String[] array = message.getContentRaw().split(" ");
        if (array.length < 2) return 1;
        try {
            return Integer.parseInt(array[1]);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private void compress(File file, String fileExtension) throws IOException {
        if (fileExtension.contains("gif")) return;

        BufferedImage inputImage = ImageIO.read(file);

        int maxWidth = 400;
        int maxHeight = 300;

        int originalWidth = inputImage.getWidth();
        int originalHeight = inputImage.getHeight();

        double aspectRatio = (double) originalWidth / originalHeight;

        int scaledWidth = maxWidth;
        int scaledHeight = (int) (maxWidth / aspectRatio);

        if (scaledHeight > maxHeight) {
            scaledHeight = maxHeight;
            scaledWidth = (int) (maxHeight * aspectRatio);
        }

        BufferedImage outputImage = new BufferedImage(scaledWidth, scaledHeight, inputImage.getType());

        Graphics2D g2d = outputImage.createGraphics();
        g2d.drawImage(inputImage, 0, 0, scaledWidth, scaledHeight, null);
        g2d.dispose();

        ImageIO.write(outputImage, fileExtension, file);
    }


}

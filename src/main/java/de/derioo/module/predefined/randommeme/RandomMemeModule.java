package de.derioo.module.predefined.randommeme;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.Config;
import de.derioo.config.ConfigData;
import de.derioo.module.Module;
import de.derioo.module.predefined.randommeme.db.RandomMeme;
import de.derioo.module.predefined.randommeme.db.RandomMemeRepository;
import eu.koboo.en2do.repository.Repository;
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
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class RandomMemeModule extends Module {

    private final Repository<RandomMeme, ObjectId> repo;

    public RandomMemeModule(DiscordBot bot) {
        super(bot, "randommeme");
        this.repo = this.bot.getRepo(RandomMemeRepository.class);
    }

    @ModuleListener
    public void onMessage(MessageReceivedEvent event) throws ExecutionException, InterruptedException, IOException {
        if (event.getChannelType().isGuild()) return;
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
                    .fileExtension(attachment.getFileExtension())
                    .data(new Binary(Files.readAllBytes(file.toPath())))
                    .build());
        }

        for (Guild guild : this.bot.getJda().getGuilds()) {
            ConfigData configData = this.bot.get(guild);
            List<Long> data = configData.getData(Config.Id.User.RANDOM_MEME_USERS.name(), List.class);
            if (data == null) continue;
            if (!data.contains(event.getAuthor().getIdLong())) continue;
            for (RandomMeme meme : attachments) {
                meme.setGuildId(guild.getIdLong());
                this.repo.save(meme);
            }
            event.getChannel().sendMessage("\uD83D\uDC4D")
                    .addFiles(attachments.stream().map(meme -> FileUpload.fromData(meme.getData().getData(), "image." + meme.getFileExtension())).toList())
                    .queue();
        }
        FileUtils.deleteDirectory(new File("./temp"));
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

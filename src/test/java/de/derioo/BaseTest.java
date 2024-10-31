package de.derioo;

import de.derioo.annotations.ModuleListener;
import de.derioo.bot.DiscordBot;
import de.derioo.config.local.LocalConfig;
import de.derioo.module.Module;
import de.derioo.module.ModuleManager;
import de.derioo.utils.DatabaseUtility;
import dev.rollczi.litecommands.LiteCommandsBuilder;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.jda.LiteJDAFactory;
import dev.rollczi.litecommands.jda.LiteJDASettings;
import eu.koboo.en2do.MongoManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.stubbing.answers.Returns;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;
import static org.mockito.Mockito.*;


import static org.assertj.core.api.Assertions.assertThat;

public abstract class BaseTest {

    @Mock
    public JDA jda;


    @Mock
    public LiteCommandsBuilder<User, LiteJDASettings, ?> commandsBuilder;

    @Mock
    public Guild guild;


    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mockStatic(LocalConfig.class);
        when(LocalConfig.loadByENV()).then(new Returns(
                new LocalConfig(
                        "",
                        "",
                        "test",
                        null,
                        null,
                        null)
                ));


        Main.main(new String[0]);
        Thread.sleep(2000); // wait for startup

        checkAPI();
    }

    public void checkAPI() throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://localhost:8080/ping")
                .get()
                .build();
        String res = client.newCall(request).execute().body().string();
        assertThat(res).contains("Pong! (");
    }

    public Module simulateEvent(GenericEvent event, Class<? extends Module> moduleClazz) {
        ModuleManager manager = Main.getBot().getModuleManager();

        new Thread(() -> {
            try {
                Thread.sleep(200);
                for (Module module : manager.getEnabledModules().values()) {
                    for (Method declaredMethod : module.getClass().getDeclaredMethods()) {
                        if (!declaredMethod.isAnnotationPresent(ModuleListener.class)) continue;
                        if (declaredMethod.getParameters().length != 1) continue;
                        Parameter first = declaredMethod.getParameters()[0];
                        if (first.getType().isAssignableFrom(event.getClass())) declaredMethod.invoke(module, event);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();

        return manager.getModule(moduleClazz);
    }

}

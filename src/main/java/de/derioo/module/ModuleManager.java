package de.derioo.module;

import de.derioo.bot.DiscordBot;
import de.derioo.config.local.LangConfig;
import lombok.Getter;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

public class ModuleManager {

    private final DiscordBot bot;
    private final LangConfig config;

    @Getter
    private final Map<Class<? extends Module>, Module> enabledModules = new HashMap<>();

    public ModuleManager(DiscordBot bot, LangConfig config) {
        this.bot = bot;
        this.config = config;
    }

    public void enableAllModules() {
        Reflections reflections = new Reflections(this.getClass().getPackage().getName() + ".predefined", Scanners.SubTypes);
        Set<Class<? extends Module>> modules = reflections.getSubTypesOf(Module.class);
        modules.forEach(module -> {
            Constructor<?> constructor =  module.getDeclaredConstructors()[0];

            List<Object> availableParams = List.of(bot, config);
            List<Object> params = new ArrayList<>();
            for (Parameter parameter : constructor.getParameters()) {
                for (Object availableParam : availableParams) {
                    if (availableParam.getClass().isAssignableFrom(parameter.getType())) {
                        params.add(availableParam);
                    }
                }
            }

            try {
                enabledModules.put(module, (Module) constructor.newInstance(params.toArray()));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                Module.logThrowable(bot, e);
                return;
            }

        });

        enabledModules.values().forEach(Module::start);
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T getModule(Class<T> clazz) {
        return (T) enabledModules.get(clazz);
    }

}

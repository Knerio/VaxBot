package de.derioo.utils;

import eu.koboo.en2do.MongoManager;
import eu.koboo.en2do.repository.Repository;

import java.util.Map;

public class DatabaseUtility {

    public static void dropAll(Map<Class<? extends Repository<?, ?>>, Repository<?, ?>> repos) {
        repos.forEach((aClass, repository) -> {
            repository.drop();
        });
    }

}

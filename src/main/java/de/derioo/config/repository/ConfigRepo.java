package de.derioo.config.repository;

import de.derioo.config.Config;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import org.bson.types.ObjectId;

@Collection("config")
public interface ConfigRepo extends Repository<Config, ObjectId> {




}

package de.derioo.module.predefined.level.db;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import org.bson.types.ObjectId;

@Collection("level")
public interface LevelPlayerDataRepo extends Repository<LevelPlayerData, String> {
}

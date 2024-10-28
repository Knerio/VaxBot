package de.derioo.module.predefined.level.db;

import de.derioo.module.predefined.randommeme.db.RandomMeme;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.sort.Sort;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.List;

@Collection("level")
public interface LevelPlayerDataRepo extends Repository<LevelPlayerData, ObjectId> {

    List<LevelPlayerData> findManyByGuildId(String guildId);

    LevelPlayerData findFirstByUserIdAndGuildId(String userId, String guildId);

    boolean existsByUserIdAndGuildId(String userId, String guildId);

}

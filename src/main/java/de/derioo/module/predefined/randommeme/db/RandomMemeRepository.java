package de.derioo.module.predefined.randommeme.db;

import de.derioo.config.Config;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import eu.koboo.en2do.repository.methods.pagination.Pagination;
import org.bson.types.Binary;
import org.bson.types.ObjectId;

import java.util.List;

@Collection("randommeme")
public interface RandomMemeRepository extends Repository<RandomMeme, ObjectId> {

    List<RandomMeme> pageByGuildId(Long guildId, Pagination pagination);

    List<RandomMeme> findManyByData(Binary data);
}

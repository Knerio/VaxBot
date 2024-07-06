package de.derioo.module.predefined.suggestion;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import org.bson.types.ObjectId;

@Collection("suggestion")
public interface SuggestionRepo extends Repository<Suggestion, ObjectId> {
}

package de.derioo.module.predefined.giveaway.db;

import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import org.bson.types.ObjectId;

@Collection("giveaway")
public interface GiveawayRepo extends Repository<GiveAway, ObjectId> {
}

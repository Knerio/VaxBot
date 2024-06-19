package de.derioo.module.predefined.ticket;

import eu.koboo.en2do.repository.AsyncRepository;
import eu.koboo.en2do.repository.Collection;
import eu.koboo.en2do.repository.Repository;
import org.bson.types.ObjectId;

@Collection("tickets")
public interface TicketRepo extends Repository<Ticket, ObjectId>, AsyncRepository<Ticket, ObjectId> {
}

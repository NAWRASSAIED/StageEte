package tn.spring.pispring.Interfaces;

import tn.spring.pispring.Entities.Ticket;

import java.util.List;

public interface TicketInterface {
    Ticket addTicket(Ticket ticket);
    Ticket UpdateTicket(Long id, Ticket updatedTicket);
    void deleteTicket(long id);
    List<Ticket> findAllTickets();
    Ticket findTicketById(long id);
}

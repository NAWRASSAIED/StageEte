package tn.spring.pispring.config.JWT.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tn.spring.pispring.Entities.Ticket;
import tn.spring.pispring.Entities.TicketStatusHistory;
import tn.spring.pispring.Entities.User;
import tn.spring.pispring.ServiceIMP.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.spring.pispring.ServiceIMP.UserServiceIMP;
import tn.spring.pispring.repo.TicketRepository;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
public class TicketController {
    @Autowired
    TicketService ticketService;
    @PostMapping("/addTicket")
    public Ticket addTicket(@RequestBody Ticket ticket) {
        return ticketService.addTicket(ticket);
    }
    @PutMapping("/UpdateTicket/{id}")
    public Ticket UpdateTicket(@PathVariable("id") Long id, Ticket updatedTicket) {
        return ticketService.UpdateTicket(id, updatedTicket);
    }
    @DeleteMapping("/deleteTicket/{id}")
    public void deleteTicket(@PathVariable("id") long id) {
        ticketService.deleteTicket(id);
    }
    @GetMapping("/findAllTickets")
    public List<Ticket> findAllTickets() {
        return ticketService.findAllTickets();
    }
    @GetMapping("/findTicketById/{id}")
    public Ticket findTicketById(@PathVariable("id") long id) {
        return ticketService.findTicketById(id);
    }
    @PutMapping("/status/{ticketid}")
    public ResponseEntity<?> updateTicketStatusByAssetId(
            @PathVariable Long ticketid,
            @RequestParam Ticket.TicketStatus status) {
        Optional<Ticket> updatedTicket = ticketService.updateTicketStatusByticketId(ticketid, status);

        if (updatedTicket.isPresent()) {
            return ResponseEntity.ok(updatedTicket.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ticket not found");
        }
    }
    @PutMapping("/{ticketId}/status/{newStatus}")
    public ResponseEntity<String> updateTicketStatus(@PathVariable Long ticketId, @PathVariable Ticket.TicketStatus newStatus) {
        try {
            ticketService. updateTicketStatusByticketId(ticketId, newStatus);
            return ResponseEntity.ok("Ticket status updated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/{ticketId}/history")
    public ResponseEntity<List<TicketStatusHistory>> getTicketStatusHistory(@PathVariable Long ticketId) {
        List<TicketStatusHistory> statusHistory = ticketService.getTicketStatusHistory(ticketId);
        return ResponseEntity.ok(statusHistory);
    }

    // Endpoint pour récupérer l'historique des statuts d'un ticket par son assetId
    @GetMapping("/asset/{ticketId}/history")
    public ResponseEntity<List<TicketStatusHistory>> getTicketStatusHistoryByticketId(@PathVariable Long ticketId) {
        List<TicketStatusHistory> statusHistory = ticketService.getTicketStatusHistoryByTicketId(ticketId);
        return ResponseEntity.ok(statusHistory);
    }
    // Endpoint to get ticket status history


    // Archiver un ticket manuellement en changeant son statut à CLOSED
    @GetMapping("/closed")
    public ResponseEntity<List<Ticket>> getClosedTickets() {
        List<Ticket> closedTickets = ticketService.findClosedTickets();
        return ResponseEntity.ok(closedTickets);
    }
    @GetMapping("/bysitename/{siteName}")
    public List<Ticket> getTicketsBySiteName(@PathVariable String siteName) {
        return ticketService.getTicketsBySiteName(siteName);
    }
    UserServiceIMP userService;
    TicketRepository ticketRepository;
}

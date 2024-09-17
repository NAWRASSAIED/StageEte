package tn.spring.pispring.Entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class TicketStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Ticket.TicketStatus status;

    private LocalDateTime changeDate;

    // Relation ManyToOne avec Ticket
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    public TicketStatusHistory(Ticket ticket, Ticket.TicketStatus status, LocalDateTime changeDate) {
        this.ticket = ticket;
        this.status = status;
        this.changeDate = changeDate;
    }
}

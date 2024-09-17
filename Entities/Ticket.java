package tn.spring.pispring.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String assetId;//id de vulnerability
    private String description;//Cvss
    private String severity;
    private String remediation;
    private String siteName;


    @Enumerated(EnumType.STRING)
    private TicketStatus status= TicketStatus.OPEN;

    private LocalDateTime slaDeadline;
    private boolean slaNotificationSent = false;
    private boolean archived = false;


    // Relation OneToMany avec TicketStatusHistory
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TicketStatusHistory> statusHistory = new ArrayList<>();

    @JsonIgnore
    @OneToOne
    Vulnerability vulnerability;

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @ManyToOne
    User user;

    public enum TicketStatus {
        OPEN,
        IN_PROGRESS,
        RESOLVED,
        CLOSED
    }

    public Ticket(String ip, String severity, String description,String remediation) {
        this.assetId = ip;
        this.severity = severity;
        this.description = description;
        this.remediation=remediation;
        this.status = TicketStatus.OPEN;
        this.slaDeadline = calculateSlaDeadline(severity);
    }

    public String getRemediation() {
        return remediation;

    }

    public void setRemediation(String remediation) {
        this.remediation = remediation;
    }

    private LocalDateTime calculateSlaDeadline(String severity) {
        switch (severity) {
            case "Critical":
                return LocalDateTime.now().plusMinutes(2);
            case "Severe":
                return LocalDateTime.now().plusMinutes(5);
            case "Moderate":
                return LocalDateTime.now().plusMinutes(7);
            default:
                return LocalDateTime.now().plusMinutes(10);
        }
    }
    @Scheduled(fixedRate = 60000)
    public void addStatusChange(Ticket.TicketStatus newStatus) {

            TicketStatusHistory history = new TicketStatusHistory(this, newStatus, LocalDateTime.now());
            this.statusHistory.add(history);


    }
    public void setStatus(TicketStatus status) {
        this.status = status;
        if (status == TicketStatus.CLOSED) {
            this.archived = true;
        }
    }

}






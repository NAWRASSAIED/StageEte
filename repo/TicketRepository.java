package tn.spring.pispring.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import tn.spring.pispring.Entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.spring.pispring.Entities.User;

import javax.persistence.CascadeType;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket,Long> {
    List<Ticket> findTicketByUser(User user);

    List<Ticket> findTicketsByAssetIdIn(List<String> assetIds);
   // Optional<Ticket> findByAssetId(String assetId);
    Ticket findByAssetId(String assetId);

    List<Ticket> findByStatus(Ticket.TicketStatus status);
    List<Ticket> findByUserAndArchived(User user, boolean archived);
    List<Ticket> findBySiteName(String siteName);
    Ticket findTicketById(Long ticketId);



}

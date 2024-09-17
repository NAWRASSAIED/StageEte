package tn.spring.pispring.ServiceIMP;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.spring.pispring.Entities.Ticket;
import tn.spring.pispring.Entities.User;
import tn.spring.pispring.Entities.mailstructure;
import tn.spring.pispring.repo.TicketRepository;
import tn.spring.pispring.repo.UserRepository;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TicketRepository ticketRepo;

    @Value("$(SecureFlow)")
    private String fromMail;

    public void sendEmailWithSLA(Long ticketId, Long userId, mailstructure mailStructure) throws MessagingException {
        User user = userRepo.findUserById(userId);
        Ticket ticket = ticketRepo.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));

        int slaMinutes = getSLAMinutes(ticket.getSeverity());
        long slaTimeMillis = slaMinutes * 60 * 1000;

        // Convertir les minutes en jours pour le message
        int slaDays = slaMinutes / 1440; // 1440 minutes dans une journée

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Ticket ticket = ticketRepo.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));
                    if (isTicketNotClosed(ticket)) {
                        MimeMessage message = mailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(message, true);

                        helper.setFrom(fromMail);
                        helper.setTo(user.getEmail());
                        helper.setSubject("SLA Expiry Notification for Ticket " + ticketId);
                        helper.setText("The SLA for ticket " + ticketId + " has expired after " + slaDays + " days. Please CLOSE your ticket.", true);

                        mailSender.send(message);
                        System.out.println("Follow-up email sent to " + user.getEmail() + " after SLA time of " + slaDays + " days.");
                    }
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }, slaTimeMillis);
    }


    public void sendEmailOnTicketAssignment(Long ticketId, Long userId) {
        try {
            User user = userRepo.findUserById(userId);
            Ticket ticket = ticketRepo.findById(ticketId).orElseThrow(() -> new RuntimeException("Ticket not found"));

            String severity = ticket.getSeverity();
            int slaDays = getSLAMinutes(severity) / 1440; // Convertir les minutes en jours

            mailstructure mailStructure = new mailstructure();
            mailStructure.setSubject("Ticket Assigned: " + severity + " Severity");
            mailStructure.setMessage("A ticket with severity " + severity + " has been assigned to you. Please review it within the next " + slaDays + " days.");

            sendEmailImmediately(user.getEmail(), mailStructure);

            sendEmailWithSLA(ticketId, userId, mailStructure);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendEmailImmediately(String toEmail, mailstructure mailStructure) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom(fromMail);
        helper.setTo(toEmail);
        helper.setSubject(mailStructure.getSubject());
        helper.setText(mailStructure.getMessage(), true);

        mailSender.send(message);
        System.out.println("Immediate email sent to " + toEmail);
    }

    // Check if the ticket is not closed
    private boolean isTicketNotClosed(Ticket ticket) {
        return ticket.getStatus() != Ticket.TicketStatus.CLOSED;
    }
    // Example method to set SLA deadline based on severity
    public void setSLADeadline(Ticket ticket) {
        int slaMinutes = getSLAMinutes(ticket.getSeverity());
        LocalDateTime slaDeadline = LocalDateTime.now().plusMinutes(slaMinutes);
        ticket.setSlaDeadline(slaDeadline);
        ticketRepo.save(ticket);
    }


    // Scheduled task to check for tickets with expired SLA and status not closed
    @Scheduled(fixedRate = 60000) // Check every minute
    public void checkTicketsSLA() {
        List<Ticket> tickets = ticketRepo.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (Ticket ticket : tickets) {
            if (ticket.getSlaDeadline() != null && ticket.getSlaDeadline().isBefore(now) && isTicketNotClosed(ticket) && !ticket.isSlaNotificationSent()) {
                try {
                    mailstructure mailStructure = new mailstructure();
                    int slaDays = getSLAMinutes(ticket.getSeverity()) / 1440; // Convertir les minutes en jours
                    mailStructure.setSubject("SLA Expiry: Ticket " + ticket.getId());
                    mailStructure.setMessage("The SLA for ticket " + ticket.getId() + " has expired after " + slaDays + " days. Please review the ticket.");

                    sendEmailImmediately(ticket.getUser().getEmail(), mailStructure);

                    ticket.setSlaNotificationSent(true);
                    ticketRepo.save(ticket);
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private int getSLAMinutes(String severity) {
        switch (severity) {
            case "Critical":
                return 2880; // 2 jours pour la gravité Critique (2 jours * 24 heures * 60 minutes)
            case "Severe":
                return 2880; // 2 jours pour la gravité Sévère
            case "Moderate":
                return 5760; // 4 jours pour la gravité Modérée (4 jours * 24 heures * 60 minutes)
            default:
                return 10080; // 1 semaine pour toutes les autres gravités (7 jours * 24 heures * 60 minutes)
        }
    }

}

package tn.spring.pispring.Entities;

import lombok.Getter;
import lombok.Setter;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
@Getter
@Setter
public class mailstructure {

    private String subject;
    private String message;


}


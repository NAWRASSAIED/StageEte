package tn.spring.pispring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class OTP {
    String identification;
    @Temporal(TemporalType.DATE)
    Date expiredDate;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long id;


}

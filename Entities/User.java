package tn.spring.pispring.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String lastname;

    private String username;
    private String email;
    private String password;
    private String address;
    private int number;
    private boolean blocked ;
    private boolean valid ;
    private String token;
    private String image;




    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude

    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    //@JsonIgnore

    private Set <Role> roles = new HashSet<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> ticketlist = new ArrayList<>();



    public User(String name,String lastname, String username, String email, String password, boolean blocked, String address, boolean valid) {

        this.name = name;
        this.lastname=lastname;
        this.username = username;
        this.email = email;
        this.password = password;
        this.blocked = blocked;
        this.address = address;
        this.valid = valid;

    }


}

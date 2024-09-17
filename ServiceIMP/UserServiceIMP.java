package tn.spring.pispring.ServiceIMP;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import tn.spring.pispring.Entities.Role;
import tn.spring.pispring.Entities.Ticket;
import tn.spring.pispring.Entities.User;
import tn.spring.pispring.Interfaces.OTPInterface;
import tn.spring.pispring.Interfaces.UserServiceInterface;

import tn.spring.pispring.dto.ResetPass;
import tn.spring.pispring.dto.RoleName;
import tn.spring.pispring.repo.RoleRepository;
import tn.spring.pispring.repo.TicketRepository;
import tn.spring.pispring.repo.UserRepository;

import javax.validation.Valid;
import java.util.*;

@Service
public class UserServiceIMP implements UserServiceInterface {


    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;

    public List<User> getAllUser() {
        return userRepository.findAll();
    }


    public User getUserById(Long idUser) {
        return userRepository.findById(idUser).orElseThrow(() -> new IllegalArgumentException("Provider ID not Found"));
    }

    public List<User> getUserByRoles(RoleName roleName){
        Role role= roleRepository.findByName(roleName).get();
        return userRepository.findByRolesContains(role);
    }

    public User deleteUser(Long id) {
        User user = userRepository.findById(id).orElse(null);

        userRepository.delete(user);
        return user;

    }

    @Override
    public void bloqueUser(Long id) {

    }



  /* public void bloqueUser(Long id) {
        Optional<User> user = userRepository.findById(id);
        User user1 = user.get();
        String Newligne = System.getProperty("line.separator");
        String url = "http://localhost:4200/auth/verification/" ;
        String body = "compte bloque\n  consulter les superviseurs :" + Newligne + url;
        if (user.isPresent()) {

            user1.setBlocked(true);
            this.userRepository.save(user1);
            try {
                mailSending.send(user1.getEmail(), "bloque ", body);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }*/
    /*    public void  debloqueUser(Long idUser){
        Optional<User> user = userRepository.findById(idUser);
        User user1 = user.get();
        String Newligne = System.getProperty("line.separator");
        String body = "compte activer avec succes\n  use this link to verify your account is :" + Newligne ;
        if (user.isPresent()) {

            user1.setBlocked(false);
            this.userRepository.save(user1);
            try {
                mailSending.send(user1.getEmail(), "activation ", body);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }*/

    @Override
    public void updateUser(Long id) {

    }

/*
    public List<User> getUsersOrderBySum_totalAsc(){
        return userRepository.findUsersOrderByBilanSum_totalAsc();
    }
    public List<User> getAllUserByRoleOrderSum_total(RoleName role) {
        return userRepository.findAllUserByRoleOrderSum_total(role);
    }
*/

    public void validInscription(Long id) {
        Optional<User> user = userRepository.findById(id);
        User user1 = user.get();
        String Newligne = System.getProperty("line.separator");
        String url = "http://localhost:4200/auth/verification/" ;
        String body = "Votre compte est Activer avec succes Soyez le bienvenue dans notre platforme   \n  veuillez utuliser ce lien là pour s'authentifier :" + Newligne + url + Newligne + "verification"
               ;
        if (user.isPresent()) {

            user1.setBlocked(true);
            this.userRepository.save(user1);
           /*try {
                mailSending.send(user1.getEmail(), "Welcome ", body);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }*/
        }
    }

    public ResponseEntity<User> registerUser(User user1, String roleName) {
        if (userRepository.existsByUsername(user1.getUsername())) {
            return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
        }
        if (userRepository.existsByEmail(user1.getEmail())) {
            return new ResponseEntity<User>(HttpStatus.BAD_REQUEST);
        }
        User user = new User(user1.getName(), user1.getLastname(), user1.getUsername(), user1.getEmail(), passwordEncoder.encode(user1.getPassword()), false, user1.getAddress(), false);
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.valueOf(roleName.trim()))
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
        roles.add(userRole);
        user.setRoles(roles);
        user.setValid(true);
        user.setAddress(user1.getAddress());
        user.setNumber(user1.getNumber());
        User suser = userRepository.save(user);


     /*   if (suser != null) {
            //String Newligne = System.getProperty("line.separator");
            String url = "http://localhost:4200/verification" ;
            String verificationCode = otpInterface.GenerateOTp().getIdentification(); // Replace with your actual verification code
            String newLine = "<br/>"; // HTML line break
            String htmlMessage = "<div style='border: 1px solid #ccc; padding: 10px; margin-bottom: 10px;'>"
                    + "Soyez le bienvenue dans notre plateforme" + newLine
                    + "Veuillez utiliser ce lien pour vous authentifier : " + newLine
                    + "<a href='" + url + "'>" + url + "</a>" + newLine
                    + "<strong>Verification Code ! max 5 min ! :</strong> " + verificationCode + newLine
                    + "</div>";
            try {
                mailSending.send(user.getEmail(), "Welcome"+ user.getName() , htmlMessage);
                return new ResponseEntity<User>(user, HttpStatus.OK);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }*/


        return new ResponseEntity<User>(user, HttpStatus.OK);
    }




    public ResponseEntity<User> registerAdmin(@Valid @RequestBody User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return new ResponseEntity<User>(HttpStatus.NOT_FOUND);
        }
        String token = UUID.randomUUID().toString().replace("-", "");
        User user1 = new User(user.getName(), user.getLastname(), user.getUsername(), user.getEmail(), passwordEncoder.encode(user.getPassword()), false, user.getAddress(), false);
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Fail! -> Cause: User Role not find."));
        roles.add(userRole);
        user1.setRoles(roles);
        userRepository.save(user1);
        return new ResponseEntity<User>(user1, HttpStatus.OK);
    }

    public Optional<User> getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username);
    }
public ResponseEntity<?> userforgetpassword(String email) {
    Optional<User> user = userRepository.findByEmail(email);
    if (user.isPresent()) {
       // String url = "http://localhost:4200/verifCaptch" ;
        String verificationCode = otpInterface.GenerateOTp().getIdentification();
        String newLine = "<br/>"; // HTML line break
        String htmlMessage = "<div style='border: 1px solid #ccc; padding: 10px; margin-bottom: 10px;'>"
                + "Une tentative de Reset du Password à été effectuer " + newLine
                //+ "Veuillez utiliser ce lien pour vous authentifier : " + newLine
              //  + "<a href='" + url + "'>" + url + "</a>" + newLine
                + "<strong>Verification Code ! max 5 min ! :</strong> " + verificationCode + newLine
                + "</div>";
      /*  try {
            mailSending.send(user.get().getEmail(), "Did you Forget your password ?"+ user.get().getName() , htmlMessage);
            return new ResponseEntity<>( HttpStatus.OK);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity(e.getMessage(), HttpStatus.BAD_REQUEST);
        }*/
    } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>( HttpStatus.OK);
}

    public  ResponseEntity<?>  updatePassword(String username, ResetPass updatePasswordDto) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            String storedHashedPassword = user.get().getPassword();
            if (passwordEncoder.matches(updatePasswordDto.getOldPassword(), storedHashedPassword)) {
                user.get().setPassword(passwordEncoder.encode(updatePasswordDto.getNewPassword()));
                userRepository.save(user.get());
                return new ResponseEntity<>(HttpStatus.OK);

            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
    }
    public  ResponseEntity<?>  updatePasswordBymail(String email, ResetPass updatePasswordDto) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isPresent()) {
                Boolean verif = otpInterface.VerifOTP(updatePasswordDto.getCode());
                if (verif == false) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                else {
                    user.get().setPassword(passwordEncoder.encode(updatePasswordDto.getNewPassword()));
                    userRepository.save(user.get());
                    return new ResponseEntity<>(HttpStatus.OK);
                }

        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
    }


    OTPInterface otpInterface;
    TicketRepository ticketRepository;
}




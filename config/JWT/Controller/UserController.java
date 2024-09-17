package tn.spring.pispring.config.JWT.Controller;


import tn.spring.pispring.Entities.Ticket;
import tn.spring.pispring.ServiceIMP.InsightVMService;
import tn.spring.pispring.dto.ResetPass;
import tn.spring.pispring.dto.RoleName;
import tn.spring.pispring.Entities.User;
import tn.spring.pispring.ServiceIMP.UserServiceIMP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/user")
public class UserController {

@Autowired
UserServiceIMP userServiceIMP;
   /* @PutMapping("/debloque-user/{idUser}")
    public void debloqueUser(@PathVariable("idUser") Long idUser) {
        userServiceIMP.debloqueUser(idUser);
    }*/

    @GetMapping("/list-user")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> ListUser() {
        return userServiceIMP.getAllUser();
    }
    @GetMapping("Currentuser/{id}")
    public User CurrentUser(@PathVariable("id") Long id) {
        User currentuser = userServiceIMP.getUserById(id);
        return currentuser;
    }
    @PutMapping("/validate-user/{idUser}")
    @PreAuthorize("hasRole('ADMIN')")
    public void validInscription(@PathVariable("idUser") Long idUser) {
        userServiceIMP.validInscription(idUser);
    }

    @PutMapping("/bloque-user/{idUser}")
    @PreAuthorize("hasRole('ADMIN')")
    public void bloqueUser(@PathVariable("idUser") Long idUser) {
        userServiceIMP.bloqueUser(idUser);
    }
    @DeleteMapping("/delete-user/{idUser}")
    public void deleteAccount(@PathVariable("idUser") Long idUser) {
        userServiceIMP.deleteUser(idUser);
    }

    @GetMapping("/list-RolesName/{RolesName}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> ListUserByRoles(@PathVariable("RolesName") RoleName roleName) {
        return userServiceIMP.getUserByRoles(roleName);
    }
    @PutMapping("forgetpass/{username}")
    public ResponseEntity<?> forgetPassword(@PathVariable("username") String username, @RequestBody ResetPass resetPass) {
       return userServiceIMP.updatePassword(username,resetPass);
    }
    @PostMapping("forgetpassword/{email}")
    public ResponseEntity<?> userForgetPassword(@PathVariable("email") String email) {
        return userServiceIMP.userforgetpassword(email);
    }
    @PutMapping("forgetpassbyemail/{email}")
    public ResponseEntity<?> forgetPasswordbyemail(@PathVariable("email") String email, @RequestBody ResetPass resetPass) {
        return userServiceIMP.updatePasswordBymail(email,resetPass);
    }
    @Autowired
    private InsightVMService insightVMService;




}

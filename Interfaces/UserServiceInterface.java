package tn.spring.pispring.Interfaces;



import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import tn.spring.pispring.Entities.User;
import tn.spring.pispring.dto.RoleName;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

public interface UserServiceInterface {

    public List<User> getAllUser() ;

    public User getUserById(Long idUser);

    public List<User> getUserByRoles(RoleName roleName);

    public User deleteUser(Long id);


    public void bloqueUser(Long id);

    public void updateUser(Long id);

    public void validInscription(Long id) ;

    public ResponseEntity<User> registerUser(User user1, String roleName);




    public ResponseEntity<User> registerAdmin(@Valid @RequestBody User user);




    public Optional<User> getCurrentUser() ;

}

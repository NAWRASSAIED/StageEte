package tn.spring.pispring.config.JWT.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.pispring.Entities.Role;
import tn.spring.pispring.ServiceIMP.RoleServiceIMP;
import tn.spring.pispring.dto.RoleName;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/api/role")
public class RoleController  {
@Autowired
RoleServiceIMP roleServiceIMP;

    @PostMapping ("/addRole/{RolesName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public  void addRole(@PathVariable("RolesName") RoleName roleName){
        roleServiceIMP.addRole(roleName);
    }
    @DeleteMapping("/deleteRole/{RolesName}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteRole(@PathVariable("RolesName") RoleName roleName) {
        roleServiceIMP.deleteRole(roleName);
    }
    @GetMapping ("/getAllRoles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<Role> getAllRoles() {
        return roleServiceIMP.getAllRoles();
    }
    @PostMapping("/AddALLRoles")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void AddALLRoles() {
        roleServiceIMP.AddALLRoles();
    }


}

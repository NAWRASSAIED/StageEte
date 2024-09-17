package tn.spring.pispring.Interfaces;


import tn.spring.pispring.Entities.Role;
import tn.spring.pispring.dto.RoleName;

import java.util.List;

public interface RoleServiceInterface {
    void addRole(RoleName roleName);
    void deleteRole(RoleName roleName);
    List<Role> getAllRoles();
    void AddALLRoles();
}

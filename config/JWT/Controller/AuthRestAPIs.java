package tn.spring.pispring.config.JWT.Controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import tn.spring.pispring.Entities.User;
import tn.spring.pispring.ServiceIMP.UserServiceIMP;

import tn.spring.pispring.config.JWT.JwtAuthTokenFilter;
import tn.spring.pispring.config.JWT.JwtProvider;
import tn.spring.pispring.config.JWT.JwtResponse;
import tn.spring.pispring.config.JWT.NewTokensResponses;
import tn.spring.pispring.dto.SignIn;
import tn.spring.pispring.repo.RoleRepository;
import tn.spring.pispring.repo.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
public class AuthRestAPIs {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;
    @Autowired
    UserServiceIMP userServiceIMP;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtProvider jwtProvider;


    @Autowired
    JwtAuthTokenFilter jwtAuthTokenFilter;

    @PostMapping("/signIn")
    public ResponseEntity<JwtResponse> authenticateUser(@RequestBody SignIn login, HttpServletRequest request) {
        Optional<User> userByEmail = userRepository.findByEmail(login.getEmail());
        Optional<User> userByUsername = userRepository.findByUsername(login.getEmail());

        // Combine the results from both searches
        Optional<User> user = userByEmail.isPresent() ? userByEmail : userByUsername;

        if(user.get().isBlocked()&& user.get().isValid()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);


        List <String> jwt = jwtProvider.generateJwtTokens(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(new JwtResponse(jwt.get(0),jwt.get(1), userDetails.getUsername(),user.get().getId(), userDetails.getAuthorities()));
    }


    /* @PostMapping("/signIn")

    public ResponseEntity<JwtResponse> authenticateUser(@RequestBody SignIn login) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateJwtToken(authentication)
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities()));

    }*/
    // Create a new controller method for token refresh

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken( HttpServletRequest request) {
        String refreshToken = jwtAuthTokenFilter.extractRefreshToken(request);
        if (refreshToken != null && jwtAuthTokenFilter.isValidRefreshToken(refreshToken)) {
            String newAccessToken = jwtAuthTokenFilter.issueNewAccessToken(refreshToken);
           // String newRefreshToken = jwtProvider.generateRefreshToken();
            return ResponseEntity.ok(new NewTokensResponses(refreshToken, newAccessToken));
        } else {
            return ResponseEntity.badRequest().body("expired refresh token");
        }
    }


    @RequestMapping(value = "/signup/employee/{roleName}", method = RequestMethod.POST)
    public ResponseEntity<User> registerUser(@Validated @RequestBody User user1,@PathVariable ("roleName")String roleName) {
       return userServiceIMP.registerUser(user1,  roleName);
    }


    @RequestMapping(value = "/signupadmin", method = RequestMethod.POST)
    public ResponseEntity<User> registerAdmin(@Valid @RequestBody User user)  {
        return userServiceIMP.registerAdmin(user);
    }
}

















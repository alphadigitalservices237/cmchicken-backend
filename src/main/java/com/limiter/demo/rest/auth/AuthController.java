package com.limiter.demo.rest.auth;


import com.limiter.demo.dto.AuthResponseDto;
import com.limiter.demo.dto.LoginDto;
import com.limiter.demo.dto.RegisterDto;
import com.limiter.demo.models.Role;
import com.limiter.demo.models.UserEntity;
import com.limiter.demo.repositories.RoleRepository;
import com.limiter.demo.repositories.UserRepository;
import com.limiter.demo.rest.controllers.EmailService;
import com.limiter.demo.security.JWTGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Collections;
import java.util.Optional;

import javax.mail.MessagingException;

@RestController
@RequestMapping("api/v1/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JWTGenerator jwtGenerator;
    @Autowired
    private EmailService emailService;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JWTGenerator jwtGenerator) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator = jwtGenerator;
       
    }

    @PostMapping("employee/login")
    public ResponseEntity<AuthResponseDto> login(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken
                        (loginDto.getUsername(), loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Authentication usr = SecurityContextHolder.getContext().getAuthentication();
        String token = jwtGenerator.generateToken(authentication);
        return new ResponseEntity<>(new AuthResponseDto(token), HttpStatus.OK);
    }


@GetMapping("user/get")
public Object getCurrentUser()
{
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Authentication user = SecurityContextHolder.getContext().getAuthentication();
    Role waiter=roleRepository.findByName("WAITER").get();
    Role cook=roleRepository.findByName("COOK").get();
    Role client=roleRepository.findByName("CLIENT").get();
    Role admin=roleRepository.findByName("ADMIN").get();
    if(user.getAuthorities().toArray()[0].toString().equals(waiter.getName()))
    {
        return waiter.getName();
    }
    else if (user.getAuthorities().toArray()[0].toString().equals(cook.getName()))
    {
        return cook.getName();
    }
    else if(user.getAuthorities().toArray()[0].toString().equals(client.getName()))
    {
        return client.getName();
    }

    else if(user.getAuthorities().toArray()[0].toString().equals(admin.getName()))
    {
        return admin.getName();
    }
    return new ResponseEntity<String>("User Not Authenticated",HttpStatus.BAD_REQUEST);


}
@GetMapping("user/{id}/name")
public Object getCurrentUserById(@PathVariable long id)
{
    Optional<UserEntity> user = userRepository.findById(id);
    if(user.isPresent())
    {
        return new ResponseEntity<String>(user.get().getUsername(),HttpStatus.OK);
    }
    return new ResponseEntity<>("User not existing",HttpStatus.BAD_REQUEST);
    


}

    @PostMapping("admin/register")
    public ResponseEntity<String> adminRegister(@RequestBody RegisterDto registerDto) throws MessagingException
    {
        if(userRepository.existsByUsername(registerDto.getUsername()))
        {
            return new ResponseEntity<>("Username already taken", HttpStatus.BAD_REQUEST);
        }
        try{
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        Role roles= roleRepository.findByName("ADMIN").get();
        user.setRoles(Collections.singletonList(roles));
        emailService.sendEmail(registerDto.getUsername(), "CM CHICKEN REGISTRATION","<HTML><body><h1>Sucessfully registered as admin </h1>"+registerDto.getUsername()+"</body></HTML>" );
        userRepository.save(user);

        return new ResponseEntity<>("User registered success with Admin role",
                HttpStatus.OK);
        }
        catch(Exception ex)
        {
            return new ResponseEntity<>(ex.getLocalizedMessage(),HttpStatus.OK);
        }
    }
    @PostMapping("client/register")
    public ResponseEntity<String> clientRegister(@RequestBody RegisterDto registerDto) throws MessagingException
    {
        if(userRepository.existsByUsername(registerDto.getUsername()))
        {
            return new ResponseEntity<>("Username already taken", HttpStatus.BAD_REQUEST);
        }
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role roles= roleRepository.findByName("CLIENT").get();
        user.setRoles(Collections.singletonList(roles));
        emailService.sendEmail(registerDto.getUsername(), "CM CHICKEN REGISTRATION", "<HTML><body><h1>Sucessfully registered </h1>"+registerDto.getUsername()+"</body></HTML>");
        userRepository.save(user);

        return new ResponseEntity<>("User registered success with Client status",
                HttpStatus.OK);
    }
    @PostMapping("waiter/register")
    public ResponseEntity<String> waiterRegister(@RequestBody RegisterDto registerDto) throws MessagingException
    {
        if(userRepository.existsByUsername(registerDto.getUsername()))
        {
            return new ResponseEntity<>("Username already taken", HttpStatus.BAD_REQUEST);
        }
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role roles= roleRepository.findByName("WAITER").get();
        user.setRoles(Collections.singletonList(roles));
        emailService.sendEmail(registerDto.getUsername(), "CM CHICKEN REGISTRATION", "<HTML><body><h1>Sucessfully registered </h1>"+registerDto.getUsername()+"</body></HTML>");

        userRepository.save(user);

        return new ResponseEntity<>("User registered success with 'waiter' role",
                HttpStatus.OK);
    }

    @PostMapping("cook/register")
    public ResponseEntity<String> cookRegister(@RequestBody RegisterDto registerDto) throws MessagingException
    {
        if(userRepository.existsByUsername(registerDto.getUsername()))
        {
            return new ResponseEntity<>("Username already taken", HttpStatus.BAD_REQUEST);
        }
        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role roles= roleRepository.findByName("COOK").get();
        user.setRoles(Collections.singletonList(roles));
        emailService.sendEmail(registerDto.getUsername(), "CM CHICKEN REGISTRATION", "<HTML><body><h1>Sucessfully registered </h1>"+registerDto.getUsername()+"</body></HTML>");

        userRepository.save(user);

        return new ResponseEntity<>("User registered success with cook role",
                HttpStatus.OK);
    }
    @GetMapping("connected/see")
    public Object seeUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> user = userRepository.findByUsername(authentication.getName());
        if(user.isPresent())
        {
            return new ResponseEntity<>(user.get().getUsername(),HttpStatus.OK);
        }
        return new ResponseEntity<>("User not logged in",HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{id}/delete")
    public ResponseEntity<String> deleteUser(@PathVariable Long id)
    {
        Optional<UserEntity> user = userRepository.findById(id);
       if (user.isPresent())
       {
           userRepository.deleteById(id);
           return new ResponseEntity<>("User deleted",HttpStatus.OK);
       }
        return new ResponseEntity<>("User not found",HttpStatus.BAD_REQUEST);
    }
    @PutMapping("employee/{id}/update")
    public ResponseEntity<Object> updateEmployee(@PathVariable long id, @RequestBody UserEntity user)
    {
        
        Optional<UserEntity> user1 = userRepository.findById(id);
        if(user1.isPresent())
        {
            user1.get().setUsername(user.getUsername());
            user1.get().setPassword(user.getPassword());
            userRepository.save(user1.get());
            return new ResponseEntity<>("User modified", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("User not found", HttpStatus.BAD_REQUEST);
    }

    @PutMapping("user/credentials/reset")
    public ResponseEntity<Object> resetCredentials(@RequestBody RegisterDto registerDto)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<UserEntity> connectedUser = userRepository.findByUsername(authentication.getName());
        if(connectedUser.isPresent())
        {
            connectedUser.get().setPassword(passwordEncoder.encode(registerDto.getPassword()));
            connectedUser.get().setUsername(registerDto.getUsername());
            userRepository.save(connectedUser.get());
            return new ResponseEntity<>("CREDENTIALS SET WITH SUCCESS",HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>("PLEASE LOGIN",HttpStatus.UNAUTHORIZED);
        }
    }


}

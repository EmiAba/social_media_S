package app.user.service;

import app.exceprion.DomainException;
import app.user.model.User;
import app.user.model.UserRole;
import app.user.repoistory.UserRepository;
import app.web.dto.LoginRequest;
import app.web.dto.RegisterRequest;
import app.web.dto.UserEditRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;


@Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest registerRequest){

        Optional<User> userOptional = userRepository.findByUsername(registerRequest.getUsername());

        if(userOptional.isPresent()){
            throw new DomainException("Username [%s} already exist. ".formatted(registerRequest.getUsername()));
        }
        User user= userRepository.save(initializeUser(registerRequest));



        log.info("Successfully created new user profile for user name [%s} and id [%s]." . formatted(user.getUsername(), user. getId()));
        return user;
    }



    public void editUserDetails(UUID userId, UserEditRequest userEditRequest){

        User user=getById(userId);
        user.setFirstName(userEditRequest.getFirstName());
        user.setLastName(userEditRequest.getLastName());
         user.setProfilePicture(userEditRequest.getProfilePicture());
         user.setBio(userEditRequest.getBio());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);


    }



    private User initializeUser(RegisterRequest registerRequest){
        return User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .email(registerRequest.getEmail())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User login(LoginRequest loginRequest){

        Optional<User> optionUser = userRepository.findByUsername(loginRequest.getUsername());

        if(optionUser.isEmpty()){
            throw new DomainException("Username or password are incorrect.");
        }
        User user=optionUser.get();
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())){
            throw new DomainException("Username or password are incorrect.");
        }



        return user;
    }


    public User getById(UUID id) {


        return userRepository.findById(id)
                .orElseThrow(()-> new DomainException("User with id [%s] does not exist.". formatted(id)));
    }

    public Optional<User> getUserById(UUID userId) {
    return userRepository.findById(userId);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }


    public void setUserOnline(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnline(true);
            userRepository.save(user);
        });
    }

    public void setUserOffline(UUID userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setOnline(false);
            userRepository.save(user);
        });
    }
}

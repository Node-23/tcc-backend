package com.tcc.tccbackend.Service;

import com.tcc.tccbackend.DTO.LoginDTO;
import com.tcc.tccbackend.Exceptions.FieldAlreadyInUseException;
import com.tcc.tccbackend.Exceptions.InvalidEmailException;
import com.tcc.tccbackend.Exceptions.LoginIncorrectDataException;
import com.tcc.tccbackend.Exceptions.PasswordRulesException;
import com.tcc.tccbackend.Model.User;
import com.tcc.tccbackend.Repository.UserRepository;
import com.tcc.tccbackend.DTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, AuthenticationService authenticationService) {
        this.userRepository = userRepository;
        this.authenticationService = authenticationService;
    }

    public Iterable<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User Login(LoginDTO loginDTO){
        try {
            String encryptedPassword = GeneralService.encryptPasswords(loginDTO.password());
            User user = findUserByEmail(loginDTO.email()).get();
            if(encryptedPassword.equals(user.getPassword())){
                Authentication authentication = new UsernamePasswordAuthenticationToken(loginDTO.email(), encryptedPassword);
                String jwt = authenticationService.authenticate(authentication);
                user.setJwt(jwt);
                logger.info("User id: {}, User name: {} login correct - OS: {}", user.getId(), user.getName(), System.getProperty("os.name"));
                return user;
            }else{
                logger.info("User id: {}, User name: {} login incorrect - OS: {}", user.getId(), user.getName(), System.getProperty("os.name"));
                throw new LoginIncorrectDataException();
            }
        } catch (Exception e) {
            throw new LoginIncorrectDataException();
        }
    }

    public User createUser(UserDTO userDTO){
        User newUser = new User(userDTO);
        this.SaveUser(newUser);
        return newUser;
    }

    private void SaveUser(User user){
        ValidateUser(user);
        try{
            user.setPassword(GeneralService.encryptPasswords(user.getPassword()));
            logger.info("User id: {}, User name: {} saved - OS: {}", user.getId(), user.getName(), System.getProperty("os.name"));
            this.userRepository.save(user);
        }catch (DataIntegrityViolationException ex){
            String constrainField = GeneralService.getConstrainField(ex);
            String msg = "User id: "+user.getId()+", User name: "+user.getName()+" - Data integrity error: "+ ex.getMessage() +" - OS: " + System.getProperty("os.name") + "\n Stacktrace: " + ex;
            logger.error(msg);
//            emailService.sendEmail("User id: " + user.getId() + ", User name:" + user.getName()+ "- Data integrity error", msg);
            throw new FieldAlreadyInUseException(constrainField);
        } catch (Exception e) {
            String msg = "User id: "+user.getId()+", User name: "+user.getName()+" - Error: "+ e.getMessage() +" - OS: " + System.getProperty("os.name") + "\n Stacktrace: " + e;
            logger.error(msg);
//            emailService.sendEmail("User id: " + user.getId() + " , User name:" + user.getName() + " - Data integrity error", msg);
            throw new RuntimeException(e);
        }
    }

    private void ValidateUser(User user){
        ValidateEmail(user.getEmail());
        ValidatePassword(user.getPassword());
    }

    private void ValidateEmail(String email){
        if(!email.contains("@")){
            throw new InvalidEmailException("Insira um email válido");
        }
    }
    private void ValidatePassword(String password){
        if (password.length() < 6){
            throw new PasswordRulesException("Senha deve ter pelo menos 6 dígitos");
        }
        if(!containsUpperCase(password)){
            throw new PasswordRulesException("Senha deve ter pelo menos um caractere maiúsculo");
        }
        if(!containsLowerCase(password)){
            throw new PasswordRulesException("Senha deve ter pelo menos um caractere minúsculo");
        }
        if(!containsSpecialCharacter(password)){
            throw new PasswordRulesException("Senha deve ter pelo menos um caractere especial como: !@#$%&*");
        }
    }

    public static boolean containsSpecialCharacter(String password) {
        Pattern pattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");
        Matcher matcher = pattern.matcher(password);
        return matcher.find();
    }

    public static boolean containsUpperCase(String password) {
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsLowerCase(String password) {
        for (char ch : password.toCharArray()) {
            if (Character.isLowerCase(ch)) {
                return true;
            }
        }
        return false;
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> finduserById(Long id) {
        return userRepository.findById(id);
    }
}
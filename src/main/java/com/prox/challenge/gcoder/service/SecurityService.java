package com.prox.challenge.gcoder.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.prox.challenge.gcoder.model.*;
import com.prox.challenge.gcoder.repository.UserRepository;
import com.prox.challenge.gcoder.repository.UserSimpleRepository;
import com.prox.challenge.gcoder.security.GCoderSecurity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class SecurityService {
    @Autowired
    private GCoderSecurity tokenFilter;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserSimpleRepository userSimpleRepository;

    /**
     * get info of user, info about : email, role
     */
    public UserSimple userInfo(HttpServletRequest httpServletRequest){
        String email = (String) httpServletRequest.getAttribute("email");
        return userSimpleRepository.findById(email).orElseThrow(() -> new RuntimeException("User not exist!"));
    }

    /**
     * get list user
     */
    public List<UserSimple> userList(HttpServletRequest httpServletRequest){
        checkAdmin(httpServletRequest);
        return userSimpleRepository.findAll();
    }

    /**
     * Login by username and password
     */
    public String login(User user){
        Optional<User> result = userRepository.findByEmailAndPassword(user.getEmail(), user.getPassword());
        User user1 = result.orElseThrow(() -> new RuntimeException("Username of password wrong"));
        return tokenFilter.createJwt(user1.getEmail());
    }

    /**
     * login by email use service of firebase
     * @param tokenId : get token in firebase authentication service
     */
    public String loginFirebase(String tokenId) throws FirebaseAuthException {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(tokenId);
        String email = decodedToken.getEmail();
        userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email cant login"));
        return tokenFilter.createJwt(email);
    }

    /**
     * save of add new user
     */
    public UserSimple saveUser(HttpServletRequest httpServletRequest,UserSimple userSimple){
        checkAdmin(httpServletRequest);
        userSimple.setEmail(userSimple.getEmail().strip());
        return userSimpleRepository.save(userSimple);
    }

    /**
     *  just check admin, if not admin will throw Exception
     */
    public void checkAdmin(HttpServletRequest httpServletRequest) {
        String emailEdit = (String) httpServletRequest.getAttribute("email");
        if(userRepository.findByEmail(emailEdit).orElseThrow(() -> new RuntimeException("Email not found")).getRole() != Role.admin){
            throw new RuntimeException("Email cant admin");
        }
    }

    /**
     * just delete user, user admin requirement
     */
    public void deleteUser(HttpServletRequest httpServletRequest, String email){
        checkAdmin(httpServletRequest);
        userRepository.deleteById(email);
    }

    /**
     * Change password of user, just admin can change any user
     */
    public void changePassword(HttpServletRequest httpServletRequest, User user){
        checkAdmin(httpServletRequest);
        User user1 = userRepository.findByEmailAndPassword(user.getEmail(), user.getPassword()).orElseThrow(() -> new RuntimeException("User name or password is wrong"));
        user1.setPassword(user.getNewPass());
        userRepository.save(user1);
    }
}

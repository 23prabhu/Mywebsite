package com.mypackage.MyWebsite.service;

import com.mypackage.MyWebsite.dto.AuthenticationResponse;
import com.mypackage.MyWebsite.dto.LoginRequest;
import com.mypackage.MyWebsite.dto.RegisterRequest;
import com.mypackage.MyWebsite.exceptions.SpringMyWebsiteException;
import com.mypackage.MyWebsite.model.NotificationEmail;
import com.mypackage.MyWebsite.model.User;
import com.mypackage.MyWebsite.model.VerificationToken;
import com.mypackage.MyWebsite.repository.UserRepository;
import com.mypackage.MyWebsite.repository.VerificationTokenRepository;
import com.mypackage.MyWebsite.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

   private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    @Transactional
    public void signup(RegisterRequest registerRequest){
        User user=new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);
        userRepository.save(user);

        String token=generateVerificationToken(user);

        mailService.sendMail(new NotificationEmail("Please activate your account",user.getEmail(),
                "Thank you for signing up to Prabhu Math's website,"+
                        "please click on the below URL to activate your account:"+
                        "http://localhost:8080/api/auth/accountVerification/"+token));
    }

    private String generateVerificationToken(User user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken=new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(()->new SpringMyWebsiteException("Invalid Token"));
        fetchUserAndEnable(verificationToken.get());
    }
    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringMyWebsiteException("User not found with name -" + username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token= jwtProvider.generateToken(authenticate);
        return new AuthenticationResponse(token,loginRequest.getUsername());
    }
}

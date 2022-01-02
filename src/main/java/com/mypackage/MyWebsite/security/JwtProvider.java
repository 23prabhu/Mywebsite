package com.mypackage.MyWebsite.security;

import com.mypackage.MyWebsite.exceptions.SpringMyWebsiteException;
import com.mypackage.MyWebsite.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Jwts;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;

@Service
public class JwtProvider {

    private KeyStore keyStore;

    @PostConstruct
    public void init(){
        try {
            keyStore= KeyStore.getInstance("JKS");
            InputStream resourceAsStream=getClass().getResourceAsStream("/mywebsiteks.jks");
            keyStore.load(resourceAsStream,"Prabhu@23".toCharArray());
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            throw new SpringMyWebsiteException("Exception occurred while loading keystore");
        }

    }

    public String generateToken(Authentication authentication){
        User principal = (User) authentication.getPrincipal();
        return Jwts.builder()
                .setSubject(principal.getUsername())
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {
            return  (PrivateKey) keyStore.getKey("mywebsiteks","Prabhu@23".toCharArray());
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new SpringMyWebsiteException("Exception occurred while retrieving public key from keystore");
        }
    }
}

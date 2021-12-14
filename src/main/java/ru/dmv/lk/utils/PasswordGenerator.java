package ru.dmv.lk.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private final char[] possibleCharacters = (new String("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?")).toCharArray();

    public PasswordGenerator() {};

    public String getPassword(){

        return RandomStringUtils.random( 8, 0, 7, false, false, possibleCharacters, new SecureRandom() );

    }
}

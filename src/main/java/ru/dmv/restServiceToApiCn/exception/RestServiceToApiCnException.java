package ru.dmv.restServiceToApiCn.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestServiceToApiCnException extends Exception {
    private static Logger log = LoggerFactory.getLogger(RestServiceToApiCnException.class);

    public RestServiceToApiCnException(String s, Throwable throwable) {
        super(s, throwable);
        log.info(s);
        // и отправить по почте сообщение
    }


}

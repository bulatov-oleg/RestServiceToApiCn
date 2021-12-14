package ru.dmv.restServiceToApiCn.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestServiceToApiCnTransactionException extends Exception {
    private static Logger log = LoggerFactory.getLogger(RestServiceToApiCnTransactionException.class);

    public <T> RestServiceToApiCnTransactionException(T s, Throwable throwable) {
        super("Откат транзакции. " + s.toString() + " " + throwable.getMessage() +
                " " + throwable.getCause(), throwable);
        log.info("Откат транзакции. " + s.toString() + "/n");
        throwable.printStackTrace();
        // и отправить по почте сообщение
    }


}

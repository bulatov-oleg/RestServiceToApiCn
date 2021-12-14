package ru.dmv.cnNovosibirsk.factory;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.util.Arrays;


/*
Скрипт для генерации всех ключей связи. Пример https://habr.com/ru/post/593507/
#!/usr/bin/env bash

        CA_PRIVATE_KEY='CA-private-key.key'
        CA_SUBJECT='/CN=Certificate authority/'
        CA_CERTIFICATE_SIGNING_REQUEST='CA-certificate-signing-request.csr'
        CA_SELF_SIGNED_CERTIFICATE='CA-self-signed-certificate.pem'
        SERVER_PRIVATE_KEY='Server-private-key.key'
        SERVER_SUBJECT='/CN=localhost/'
        SERVER_CERTIFICATE_SIGNING_REQUEST='Server-certificate-signing-request.csr'
        SERVER_CERTIFICATE='Server-certificate.pem'
        SERVER_KEYSTORE='Server-keystore.p12'
        SERVER_TRUSTSTORE='Server-truststore.p12'
        CLIENT_PRIVATE_KEY='Client-private-key.key'
        CLIENT_SUBJECT='/CN=Client/'
        CLIENT_CERTIFICATE_SIGNING_REQUEST='Client-certificate-signing-request.csr'
        CLIENT_CERTIFICATE='Client-certificate.pem'
        DAYS=1
        PASSWORD='password'

        print_done() {
        echo "...done! $1"
        echo ''
        }

        private_key() {
        openssl genrsa -aes256 -passout pass:$PASSWORD -out $1 4096 2>/dev/null
        }

        certificate_signing_request() {
        openssl req -new -key $1 -passin pass:$PASSWORD -subj "$2" -out $3 >/dev/null
        }

        sign_request() {
        openssl x509 -req -in $1 -CA $2 -CAkey $3 -passin pass:$PASSWORD -CAcreateserial -days $DAYS -out $4 2>/dev/null}

        echo 'Remove any key, pem, csr, p12 files'
        rm -f *.key *.pem *.csr *.p12
        print_done

        echo 'Create private key for CA'
        private_key $CA_PRIVATE_KEY
        print_done $CA_PRIVATE_KEY

        echo 'Create certificate signing request for CA'
        certificate_signing_request $CA_PRIVATE_KEY "$CA_SUBJECT" $CA_CERTIFICATE_SIGNING_REQUEST
        print_done $CA_CERTIFICATE_SIGNING_REQUEST

        echo 'Create self signed certificate for CA'
        openssl x509 -req -in $CA_CERTIFICATE_SIGNING_REQUEST -signkey $CA_PRIVATE_KEY -passin pass:$PASSWORD -days $DAYS -out $CA_SELF_SIGNED_CERTIFICATE 2>/dev/null
        print_done $CA_SELF_SIGNED_CERTIFICATE

        echo 'Create private key for Server'
        private_key $SERVER_PRIVATE_KEY
        print_done $SERVER_PRIVATE_KEY

        echo 'Create certificate signing request for Server'
        certificate_signing_request $SERVER_PRIVATE_KEY "$SERVER_SUBJECT" $SERVER_CERTIFICATE_SIGNING_REQUEST
        print_done $SERVER_CERTIFICATE_SIGNING_REQUEST

        echo 'Sign Server'\''s certificate signing request with CA'\''s self signed certificate'
        sign_request $SERVER_CERTIFICATE_SIGNING_REQUEST $CA_SELF_SIGNED_CERTIFICATE $CA_PRIVATE_KEY $SERVER_CERTIFICATE
        print_done $SERVER_CERTIFICATE

        echo 'Create private key for Client'
        private_key $CLIENT_PRIVATE_KEY
        print_done $CLIENT_PRIVATE_KEY

        echo 'Create certificate signing request for Client'
        certificate_signing_request $CLIENT_PRIVATE_KEY "$CLIENT_SUBJECT" $CLIENT_CERTIFICATE_SIGNING_REQUEST
        print_done $CLIENT_CERTIFICATE_SIGNING_REQUEST

        echo 'Sign Client'\''s certificate signing request with CA'\''s self signed certificate'
        sign_request $CLIENT_CERTIFICATE_SIGNING_REQUEST $CA_SELF_SIGNED_CERTIFICATE $CA_PRIVATE_KEY $CLIENT_CERTIFICATE
        print_done $CLIENT_CERTIFICATE

        echo 'Create PKCS12 keystore for Server'
        openssl pkcs12 -export -in $SERVER_CERTIFICATE -inkey $SERVER_PRIVATE_KEY -passin pass:$PASSWORD -passout pass:$PASSWORD -out $SERVER_KEYSTORE >/dev/null
        print_done $SERVER_KEYSTORE

        echo 'Create PKCS12 truststore for Server'
        keytool -import -file $CA_SELF_SIGNED_CERTIFICATE -keystore $SERVER_TRUSTSTORE -storetype PKCS12 -storepass $PASSWORD -noprompt 2>/dev/null
        print_done $SERVER_TRUSTSTORE
*/


public class CnCustomeRestTemplateFactory {
    private final String destinationUrl;
    private String trustStoreFile;
    private char[] trustStorePassword;
    private String keyStoreFile;
    private char[] keyStorePassword;
    private char[] keyPairPassword;
    private String keyAlias;
    private String protocol;

    public CnCustomeRestTemplateFactory(String destinationUrlIn) {
        this.destinationUrl = destinationUrlIn;
    }


    public RestTemplate build() throws IllegalStateException {

        switch (destinationUrl){
            case "https://www.kvartplata.ru:4433":
                setKvartplata();
                break;
            case "https://www.kvartplata.ru:7437":
                setKvartplata7437();
                break;

            default:
                throw new IllegalStateException(destinationUrl + " не обслуживается CnCustomeRestTemplateFactory");
        }
        return customize();
    }

    /***
     * Возвращает
     * @return кастомизированный по destinationUrl RestTemplate
     */
    private RestTemplate customize() throws IllegalStateException  {

        final SSLContext sslContext;
        try {

            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(new File(trustStoreFile), trustStorePassword,new TrustSelfSignedStrategy())    //, (chain, authType) -> true)
                    .loadKeyMaterial(new File(keyStoreFile), keyStorePassword, keyPairPassword, (aliases, socket) -> keyAlias)
                    .setProtocol(protocol)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Ошибка установки SSL context для клиента "+destinationUrl, e);
        } finally {
            // it's good security practice to zero out passwords,
            // which is why they're char[]
            Arrays.fill(trustStorePassword, (char) 0);
            Arrays.fill(keyStorePassword, (char) 0);
        }

        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();

        HttpComponentsClientHttpRequestFactory factory =
                new HttpComponentsClientHttpRequestFactory(httpClient);
        //factory.setConnectTimeout(1000000);
        //factory.setReadTimeout(10000000);
        //factory.setConnectionRequestTimeout(1000000);
        return new RestTemplate(factory);
    }

    /**
     * Устанавливает свойства класса для кастомизации RestTemplate для "www.kvartplata.ru:4433"
     */
    private void setKvartplata(){
        trustStoreFile = System.getProperty("user.dir") + "/privateForProject/truststore/cnTrustStore.p12";
        //cnTrustStore.p12 создан keytool -import -file ./CAsign.pem  -keystore cnTrustStore.p12
        //CAsign.pem  получен из ЦН как корневой доверенный сертификат
        trustStorePassword = "IJN456_ijn456".toCharArray();
        keyStoreFile = System.getProperty("user.dir") + "/privateForProject/keystore/cnKeyStore.pfx";
        //Преобразовать pfx: openssl pkcs12 -in prv_key.pfx -nocerts -out prv_key.pem -nodes
        //Получить пару ключей в одном файле: openssl pkcs12 -export -out pairKeys.pfx -inkey prv_key.pem -in sign.crt -certfile CAsign.pem
        // далее через kse создан cnKeyStore.pfx и с alias cnpairkeys добавлен pairKeys.pfx
        // При попытке внести вторую пару в ketstore связь нарушается, поэтому один keystore на пару
        // sing.crt для prv_key.pfx(password IJN456_ijn456) (или prv_key.pem) направлен в ЦН для включения в хранилище
        //Программа для работы с разными типами хранилищ, сертификатами и тд https://keystore-explorer.org/downloads.html
        //Запускаем в терминале командой kse

        keyStorePassword = "IJN456_ijn456".toCharArray();
        keyPairPassword = "IJN123".toCharArray();
        keyAlias = "cnpairkeys";
        protocol = "TLSv1.2";
    }

    /**
     * Устанавливает свойства класса для кастомизации RestTemplate для "www.kvartplata.ru:7437"
     */
    private void setKvartplata7437(){
        trustStoreFile = System.getProperty("user.dir") + "/privateForProject/truststore/cnTrustStore.p12";
        //cnTrustStore.p12 создан keytool -import -file ./CAsign.pem  -keystore cnTrustStore.p12
        //CAsign.pem  получен из ЦН как корневой доверенный сертификат
        trustStorePassword = "IJN456_ijn456".toCharArray();
        keyStoreFile = System.getProperty("user.dir") + "/privateForProject/keystore/cnKeyStoreWork.pfx";
        //Преобразовать pfx: openssl pkcs12 -in prv_key.pfx -nocerts -out prv_key.pem -nodes
        //Получить пару ключей в одном файле: openssl pkcs12 -export -out pairKeys.pfx -inkey prv_key.pem -in sign.crt -certfile CAsign.pem
        // далее через kse создан cnKeyStoreWork.pfx и с alias workpaircn добавлен pairKeys.pfx
        // При попытке внести вторую пару в ketstore связь нарушается, поэтому один keystore на пару
        //sing.crt для prv_key.pfx(password IJN456_ijn456) (или prv_key.pem) направлен в ЦН для включения в хранилище
        //Программа для работы с разными типами хранилищ, сертификатами и тд https://keystore-explorer.org/downloads.html
        //Запускаем в терминале командой kse

        keyStorePassword = "IJN456_ijn456".toCharArray();
        keyPairPassword = "ijn123".toCharArray();
        keyAlias = "workpaircn";
        protocol = "TLSv1.2";
    }

}

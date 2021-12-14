package ru.dmv.lk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import ru.dmv.restServiceToApiCn.service.SinkServiceCnSideAndCommonImpl;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Properties;


@Service
@Primary
@Qualifier("main")
public class EmailServiceImp implements EmailService {

    private JavaMailSender mailSender;


    @Autowired
    public EmailServiceImp(JavaMailSender mailSender){
        this.mailSender=mailSender;

    }

    @Value("${spring.mail.username}")
    private String senderEmail;
    @Value("${spring.mail.host}")
    private String hostEmail;
    @Value("${spring.mail.password}")
    private String passwordSenderEmail;
    @Value("${spring.mail.port}")
    private int portEmail;
    @Value("${spring.mail.protocol}")
    private String protocolEmail;
//    @Value("${mail.debug}")
//    private String debug;


    private final Logger log = LoggerFactory.getLogger(SinkServiceCnSideAndCommonImpl.class);

  /*  // Отправить простое письмо
    public void send(String emailTo, String subject, String message) throws MailException {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(senderEmail);
        mailMessage.setTo(emailTo);
        mailMessage.setSubject(subject);
        mailMessage.setText(message); // будет в Base64 и приходит с нормальной кодировкой
        mailSender.send(mailMessage);
    }
*/
    public void send(String emailTo, String subject, String body, boolean isHtml) {


        // Здесь используем не стандартный mailSender, а его расширенную
        // имплементацию, поскольку она позволяет добавить properties,
        // которые нормализуют кодовую страницу при отправке до utf-8
        JavaMailSenderImpl mimeMailSender = new JavaMailSenderImpl();
        mimeMailSender.setHost(hostEmail);
        mimeMailSender.setUsername(senderEmail);
        mimeMailSender.setPassword(passwordSenderEmail);
        mimeMailSender.setPort(portEmail);
        mimeMailSender.setProtocol(protocolEmail);
        mimeMailSender.setDefaultEncoding("utf8");
        Properties properties = new Properties();
          //properties.setProperty("mail.transport.protocol", protocol);
        //properties.setProperty("mail.debug", debug);
        mimeMailSender.setJavaMailProperties(properties);
        //далее, подготовка собственно тела Mime сообщения
        MimeMessage message = mimeMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = null;
        try {
            // use the true flag to indicate you need a multipart message
            messageHelper = new MimeMessageHelper(message, true);
            messageHelper.setFrom(senderEmail);
            messageHelper.setTo(emailTo);
            messageHelper.setSubject(subject);
            messageHelper.setText(body, isHtml);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        try {
        mimeMailSender.send(message);
        } catch (MailException e) {
            e.printStackTrace();
        }
    }


}
/*
Пример, с якобы решенным использованием Utf8/не проверял
import java.io.IOException;
        import java.util.Properties;

        import javax.mail.MessagingException;
        import javax.mail.Session;
        import javax.mail.internet.MimeMessage;

public class MailDemo {

    public static void main(String[] args) throws MessagingException, IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);
        message.setSubject("Your new InCites\u2122 subscription", "UTF-8");
        message.setContent("hello", "text/plain");
        message.writeTo(System.out);
    }
}
Можно попробовать
msg.setSubject(MimeUtility.encodeText("string", "UTF-8", "Q"));

*/

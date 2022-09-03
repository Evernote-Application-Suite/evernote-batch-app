package in.net.sudhir.evernotebatchapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/***
 Package Name: in.net.sudhir.evernotebatchapp.service
 User Name: SUDHIR
 Created Date: 03-09-2022 at 18:19
 Description:
 */
@Service
public class EmailSvc {

    @Autowired
    Environment environment;

    @Autowired
    private JavaMailSender emailSender;


    public void sendEvernoteInformationToEmail(String mailContent) {

        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(mailContent, true);
            helper.setTo(environment.getProperty("reporting.mail.sendto"));
            helper.setSubject(environment.getProperty("reporting.mail.subject"));
            helper.setFrom(environment.getProperty("reporting.mail.sendfrom"));
            emailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

    }
}

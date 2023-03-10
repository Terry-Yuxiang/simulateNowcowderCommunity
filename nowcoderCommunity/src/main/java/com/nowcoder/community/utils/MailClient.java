package com.nowcoder.community.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;


@Component
public class MailClient {
   private static final Logger logger = LoggerFactory.getLogger(MailClient.class);


   @Autowired
   private JavaMailSender mailSender;


   @Value("${spring.mail.username}")
   private String from;


      public void sendMail(String to,String subject,String content){
         try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(new InternetAddress(from, "From nowcoder community java project"));
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content,true); //允许支持html文本
            mailSender.send(helper.getMimeMessage());
         } catch (MessagingException e) {
            logger.error("发送邮件失败："+e.getMessage());
         } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
         }
      }
}

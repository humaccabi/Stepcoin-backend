package org.opengts.util;

import java.util.Properties;

import org.simplejavamail.email.Email;
import org.simplejavamail.email.EmailBuilder;
import org.simplejavamail.mailer.Mailer;
import org.simplejavamail.mailer.config.TransportStrategy;

import spark.Request;
import spark.Response;

public class EmailSender {
	public static void send(String subject, String text) {
		Email email = new EmailBuilder()
    		    .from("backend", "coinsteps@gmail.com")
    		    .to("Eli", "humaccabi@gmail.com")
    		    .to("Oriel", "OrielBelzer@gmail.com")
    		    .subject(subject)
    		    .text(text)
    		    .build();
  	
     	Mailer mailer = new Mailer("smtp.gmail.com", 587, "coinsteps@gmail.com", "Eliandoriel1!", TransportStrategy.SMTP_TLS);
    	Properties props = new Properties();
    	props.put("mail.smtp.ssl.trust", "smtp.gmail.com");
    	mailer.applyProperties(props);
    	mailer.sendMail(email);
	}
}

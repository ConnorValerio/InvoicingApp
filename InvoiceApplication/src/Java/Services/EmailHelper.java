package Java.Services;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import Java.Models.Company;
import Java.Models.Contact;

public class EmailHelper {

	public enum EmailResponse {
		FILE_ERROR, EMAIL_SENT, EMAIL_NOT_SENT
	}

	public static EmailResponse sendEmail(Company company, Contact contact, String filepath) {

		if (company == null || contact == null || filepath == null) {
			// return NULL_POINTER_EXCEPTION;
		}

		if (!new File(filepath).exists()) {
			return EmailResponse.FILE_ERROR;
		}

		String from = getEmailName(company.getEmail());
		String password = company.getPassword();
		String to = contact.getEmail();
		String subject = "Your Invoice from " + company.getName();
		String body;
		
		// generate body for the invoice
		LocalDate today = LocalDate.now().plusDays(7);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
		String dateString = formatter.format(today);
		body = "Please find your invoice from " + company.getName() + " attached. Your invoice due date is 7 days from now (" + dateString
				+ "). Thank you for your business.";

		Properties props = System.getProperties();
		String host = "smtp.gmail.com";

		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.user", from);
		props.put("mail.smtp.password", password);
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

		Session session = Session.getDefaultInstance(props);

		try {

			// create Message object
			MimeMessage message = new MimeMessage(session);

			// Set header 'from' field
			message.setFrom(new InternetAddress(from));

			// set header 'to' field
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

			// set header 'subject' field
			message.setSubject(subject);

			// create a multipart message
			Multipart multipart = new MimeMultipart();

			// create message body part 1/2 parts
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText(body);
			messageBodyPart.setContent(body, "text/html");

			// create attachment bodypart 2/2 parts
			BodyPart attachmentBodyPart = new MimeBodyPart();

			// add invoice attachment
			File invoiceFile = new File(filepath);
			DataSource source = new FileDataSource(invoiceFile);
			attachmentBodyPart.setDataHandler(new DataHandler(source));
			attachmentBodyPart.setFileName(invoiceFile.getName());

			// add message and attachment body parts to multipart
			multipart.addBodyPart(messageBodyPart);
			multipart.addBodyPart(attachmentBodyPart);

			// set multipart as content of MimeMessage
			message.setContent(multipart);

			Transport transport = session.getTransport("smtp");
			transport.connect(host, from, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
			
			return EmailResponse.EMAIL_SENT;

		} catch (MessagingException e) {
			return EmailResponse.EMAIL_NOT_SENT;
		}
	}

	// removes domain name from address
	private static String getEmailName(String emailAddress) {
		return emailAddress.split("@")[0];
	}
}

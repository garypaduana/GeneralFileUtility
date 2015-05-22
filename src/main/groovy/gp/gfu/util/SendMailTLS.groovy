package gp.gfu.util

import java.util.Properties

import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

public class SendMailTLS {

	public static void sendEmail(String username, String password, String toAddress,
			String subject, String text, List<File> attachments) throws MessagingException {

		Properties props = new Properties()
		props.put("mail.smtp.auth", "true")
		props.put("mail.smtp.starttls.enable", "true")
		props.put("mail.smtp.host", "smtp.gmail.com")
		props.put("mail.smtp.port", "587")

		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(username, password)
					}
				})

		Message message = new MimeMessage(session)
		message.setFrom(new InternetAddress(username))
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress))
		message.setSubject(subject)

		MimeBodyPart messageBodyPart = new MimeBodyPart()
		messageBodyPart.setText(text)

		Multipart multipart = new MimeMultipart()
		multipart.addBodyPart(messageBodyPart)

		for(File attachment : attachments){
			messageBodyPart = new MimeBodyPart()
			DataSource source = new FileDataSource(attachment)

			messageBodyPart.setDataHandler(new DataHandler(source))
			messageBodyPart.setFileName(attachment)
			multipart.addBodyPart(messageBodyPart)
		}

		message.setContent(multipart)
		Transport.send(message)
	}
}
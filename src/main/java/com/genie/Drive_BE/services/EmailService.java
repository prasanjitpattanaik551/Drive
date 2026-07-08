//package com.genie.Drive_BE.services;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromEmail;
//
//    public EmailService(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    public void sendOtp(String toEmail, String otp) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(toEmail);
//        message.setSubject("Your Drive Login OTP");
//        message.setText(
//                "Hello!\n\n" +
//                        "Your one-time password (OTP) to login to Drive is:\n\n" +
//                        "  " + otp + "\n\n" +
//                        "This OTP is valid for 10 minutes.\n\n" +
//                        "If you did not request this, please ignore this email.\n\n" +
//                        "– Drive Team"
//        );
//        mailSender.send(message);
//    }
//}


package com.genie.Drive_BE.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class EmailService {

    // You can generate a free API key instantly at Brevo.com or Resend.com
    @Value("${email.api.key:mock_key}")
    private String apiKey;

    public void sendOtp(String toEmail, String otp) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            // JSON payload structured for a transactional email API
            String jsonPayload = """
                {
                  "sender": {"email": "prasanjitpattanaik936@gmail.com"},
                  "to": [{"email": "%s"}],
                  "subject": "Your Drive Login OTP",
                  "htmlContent": "<p>Your one-time password (OTP) to login to Drive is: <strong>%s</strong></p>"
                }
                """.formatted(toEmail, otp);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email")) // Brevo HTTP Endpoint
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 300) {
                throw new RuntimeException("Email API Error: " + response.body());
            }
            System.out.println("🚀 Email dispatched successfully via HTTP API port 443!");

        } catch (Exception e) {
            throw new RuntimeException("Failed to send transactional email via API", e);
        }
    }
}

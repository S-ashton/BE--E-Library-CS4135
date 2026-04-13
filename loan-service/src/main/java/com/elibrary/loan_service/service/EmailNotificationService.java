package com.elibrary.loan_service.service;

import com.elibrary.loan_service.domain.Loan;
import com.elibrary.loan_service.domain.NotificationTask;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm");

    private final SendGrid sendGrid;

    @Value("${SENDGRID_FROM_EMAIL}")
    private String fromEmail;

    @Value("${SENDGRID_FROM_NAME:E-Library}")
    private String fromName;

    public EmailNotificationService(SendGrid sendGrid) {
        this.sendGrid = sendGrid;
    }

    public void sendBorrowConfirmation(String to, Loan loan) {
        String subject = "E-Library: Book Borrowed Successfully";

        String body = """
                Hello,

                Your loan has been created successfully.

                Loan details:
                - Loan ID: %s
                - Book ID: %s
                - Copy ID: %s
                - Borrowed on: %s
                - Due date: %s

                Please return the book on time to avoid overdue fines.

                Regards,
                %s
                """.formatted(
                loan.getId(),
                loan.getBookId(),
                loan.getCopyId(),
                loan.getBorrowDate().format(DATE_FORMAT),
                loan.getDueDate().format(DATE_FORMAT),
                fromName
        );

        sendEmail(to, subject, body);
    }

    public void sendNotification(NotificationTask task, Loan loan) {
        switch (task.getType()) {
            case DUE_DATE_REMINDER -> sendDueDateReminder(task.getRecipientEmail(), loan);
            case OVERDUE_ALERT -> sendOverdueAlert(task.getRecipientEmail(), loan);
            default -> throw new IllegalArgumentException("Unsupported notification type: " + task.getType());
        }
    }

    public void sendDueDateReminder(String to, Loan loan) {
        String subject = "E-Library Reminder: Book Due Soon";

        String body = """
                Hello,

                This is a reminder that your borrowed book is due soon.

                Loan details:
                - Loan ID: %s
                - Book ID: %s
                - Copy ID: %s
                - Due date: %s

                Please return the book before the due date to avoid fines.

                Regards,
                %s
                """.formatted(
                loan.getId(),
                loan.getBookId(),
                loan.getCopyId(),
                loan.getDueDate().format(DATE_FORMAT),
                fromName
        );

        sendEmail(to, subject, body);
    }

    public void sendOverdueAlert(String to, Loan loan) {
        BigDecimal fine = loan.getFineAmount() == null ? BigDecimal.ZERO : loan.getFineAmount();

        String subject = "E-Library Alert: Book Overdue";

        String body = """
                Hello,

                Your borrowed book is now overdue.

                Loan details:
                - Loan ID: %s
                - Book ID: %s
                - Copy ID: %s
                - Due date: %s
                - Current fine due: €%s

                Please return the book as soon as possible. The fine may continue to increase while the item remains overdue.

                Regards,
                %s
                """.formatted(
                loan.getId(),
                loan.getBookId(),
                loan.getCopyId(),
                loan.getDueDate().format(DATE_FORMAT),
                fine,
                fromName
        );

        sendEmail(to, subject, body);
    }

    private void sendEmail(String recipientEmail, String subject, String body) {
        try {
            log.info("Sending email to={} subject={}", recipientEmail, subject);

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(recipientEmail);
            Content content = new Content("text/plain", body);
            Mail mail = new Mail(from, subject, to, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            log.info("SendGrid response status={}", response.getStatusCode());
            log.info("SendGrid response body={}", response.getBody());

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid email failed with status " + response.getStatusCode());
            }
        } catch (IOException ex) {
            throw new RuntimeException("Failed to send email", ex);
        }
    }
}
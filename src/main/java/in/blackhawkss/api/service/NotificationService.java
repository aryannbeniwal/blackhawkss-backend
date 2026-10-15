package in.blackhawkss.api.service;

import in.blackhawkss.api.config.AppProperties;
import in.blackhawkss.api.domain.ContactMessage;
import in.blackhawkss.api.domain.Enquiry;
import in.blackhawkss.api.domain.JobApplication;
import in.blackhawkss.api.repository.ContactMessageRepository;
import in.blackhawkss.api.repository.EnquiryRepository;
import in.blackhawkss.api.repository.JobApplicationRepository;
import in.blackhawkss.api.util.PhoneNumbers;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Sends owner alerts and prospect acknowledgements. Runs asynchronously after the request commits.
 * Failures are logged and flagged on the enquiry (notifyFailed) — never surfaced to the website visitor.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final String OFFICE_DISPLAY = "+91 88010 00054";
    private static final List<Map<String, String>> CONTACTS = List.of(
            Map.of("label", "Office", "display", "+91 88010 00054", "e164", "918801000054"),
            Map.of("label", "Sandeep Beniwal", "display", "+91 92555 34554", "e164", "919255534554"),
            Map.of("label", "Aryan Beniwal", "display", "+91 94668 80164", "e164", "919466880164"));

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final AppProperties props;
    private final EnquiryRepository enquiries;
    private final JobApplicationRepository applications;
    private final ContactMessageRepository messages;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onNewEnquiry(Long enquiryId) {
        if (!props.mail().enabled()) return;
        Enquiry e = enquiries.findById(enquiryId).orElse(null);
        if (e == null) return;
        boolean ok = true;
        Context ctx = base();
        ctx.setVariable("e", e);
        ctx.setVariable("phoneDisplay", PhoneNumbers.display(e.getPhone()));
        ctx.setVariable("preferredDate", e.getPreferredDate() == null ? "Not specified" : DATE.format(e.getPreferredDate()));
        ctx.setVariable("waLink", "https://wa.me/" + PhoneNumbers.toE164(e.getPhone()) + "?text=" + URLEncoder.encode(
                "Hello " + e.getFullName() + ", this is Black Hawk Security Solution regarding your enquiry "
                        + e.getReferenceCode() + ". When is a good time to talk?", StandardCharsets.UTF_8));
        ctx.setVariable("adminLink", props.adminBaseUrl() + "/enquiries/" + e.getId());
        ok &= send(props.mail().ownerTo(), "[New enquiry " + e.getReferenceCode() + "] " + e.getServiceCategory() + " · "
                + e.getFullName() + " · " + e.getLocation(), "mail/owner-new-enquiry", ctx, null);
        if (e.getEmail() != null) {
            Context ack = base();
            ack.setVariable("e", e);
            ok &= send(List.of(e.getEmail()), "We received your enquiry " + e.getReferenceCode() + " — Black Hawk Security Solution",
                    "mail/prospect-ack", ack, props.mail().replyTo());
        }
        if (!ok) {
            e.setNotifyFailed(true);
            enquiries.save(e);
        }
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onNewApplication(Long applicationId) {
        if (!props.mail().enabled()) return;
        JobApplication a = applications.findById(applicationId).orElse(null);
        if (a == null) return;
        Context ctx = base();
        ctx.setVariable("a", a);
        ctx.setVariable("phoneDisplay", PhoneNumbers.display(a.getPhone()));
        ctx.setVariable("waLink", "https://wa.me/" + PhoneNumbers.toE164(a.getPhone()));
        ctx.setVariable("adminLink", props.adminBaseUrl() + "/applications");
        send(props.mail().ownerTo(), "[New job application] " + a.getRoleApplied() + " · " + a.getFullName() + " · " + a.getCity(),
                "mail/owner-new-application", ctx, null);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onNewContactMessage(Long messageId) {
        if (!props.mail().enabled()) return;
        ContactMessage m = messages.findById(messageId).orElse(null);
        if (m == null) return;
        Context ctx = base();
        ctx.setVariable("m", m);
        ctx.setVariable("phoneDisplay", PhoneNumbers.display(m.getPhone()));
        ctx.setVariable("waLink", "https://wa.me/" + PhoneNumbers.toE164(m.getPhone()));
        send(props.mail().ownerTo(), "[Website message] " + (m.getSubject() == null ? m.getFullName() : m.getSubject()),
                "mail/owner-new-message", ctx, null);
    }

    /** Called by the scheduler inside its own transaction. */
    public boolean sendFollowUpReminder(Enquiry e, int ageHours) {
        if (!props.mail().enabled()) return false;
        Context ctx = base();
        ctx.setVariable("e", e);
        ctx.setVariable("ageHours", ageHours);
        ctx.setVariable("phoneDisplay", PhoneNumbers.display(e.getPhone()));
        ctx.setVariable("waLink", "https://wa.me/" + PhoneNumbers.toE164(e.getPhone()));
        ctx.setVariable("adminLink", props.adminBaseUrl() + "/enquiries/" + e.getId());
        return send(props.mail().ownerTo(), "[Reminder] Enquiry " + e.getReferenceCode() + " is still NEW after " + ageHours + "h",
                "mail/owner-followup-reminder", ctx, null);
    }

    private Context base() {
        Context ctx = new Context();
        ctx.setVariable("siteUrl", props.publicBaseUrl());
        ctx.setVariable("logoUrl", props.publicBaseUrl() + "/brand/black-hawk-logo-512.png");
        ctx.setVariable("officeDisplay", OFFICE_DISPLAY);
        ctx.setVariable("contacts", CONTACTS);
        return ctx;
    }

    private boolean send(List<String> to, String subject, String template, Context ctx, String replyTo) {
        if (to == null || to.isEmpty()) return true;
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(props.mail().from());
            helper.setTo(to.toArray(String[]::new));
            if (replyTo != null && !replyTo.isBlank()) helper.setReplyTo(replyTo);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, ctx), true);
            mailSender.send(message);
            return true;
        } catch (Exception ex) {
            log.error("Mail '{}' to {} failed: {}", subject, to, ex.getMessage());
            return false;
        }
    }
}

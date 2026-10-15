package in.blackhawkss.api.service;

import in.blackhawkss.api.config.AppProperties;
import in.blackhawkss.api.domain.Enquiry;
import in.blackhawkss.api.domain.EnquiryStatus;
import in.blackhawkss.api.repository.EnquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/** Every day at 09:00 IST: nudge the owners about enquiries still NEW after 24h (1st) and 72h (2nd). */
@Slf4j
@Component
@RequiredArgsConstructor
public class FollowUpReminderJob {

    private final EnquiryRepository enquiries;
    private final NotificationService notifications;
    private final AppProperties props;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    @Transactional
    public void run() {
        if (props.reminders() != null && !props.reminders().enabled()) return;
        Instant now = Instant.now();
        int sent = 0;
        for (Enquiry e : enquiries.findByStatusAndCreatedAtBeforeAndReminderCountLessThan(
                EnquiryStatus.NEW, now.minus(Duration.ofHours(24)), 2)) {
            long ageHours = Duration.between(e.getCreatedAt(), now).toHours();
            boolean due = (e.getReminderCount() == 0) || (e.getReminderCount() == 1 && ageHours >= 72);
            if (!due) continue;
            if (notifications.sendFollowUpReminder(e, (int) ageHours)) {
                e.setReminderCount(e.getReminderCount() + 1);
                enquiries.save(e);
                sent++;
            }
        }
        if (sent > 0) log.info("Sent {} follow-up reminder(s)", sent);
    }
}

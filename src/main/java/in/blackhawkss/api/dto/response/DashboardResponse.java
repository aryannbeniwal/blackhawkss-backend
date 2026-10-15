package in.blackhawkss.api.dto.response;

import java.util.Map;

public record DashboardResponse(
        Map<String, Long> enquiriesByStatus,
        long enquiriesThisWeek,
        long followUpsDueToday,
        long newApplications,
        long unreadMessages,
        long totalEnquiries
) {}

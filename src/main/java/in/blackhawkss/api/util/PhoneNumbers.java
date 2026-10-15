package in.blackhawkss.api.util;

import java.util.regex.Pattern;

/** Indian mobile numbers: accepts +91 / 91 / 0 prefixes and spaces, stores 10 digits. */
public final class PhoneNumbers {

    /** Strict form of an Indian mobile number (documentation / reference). */
    public static final String INDIAN_MOBILE_STRICT_REGEX = "^(\\+91[\\-\\s]?)?[6-9]\\d{9}$";
    /** Lenient input pattern used on DTOs: digits with optional +91 / 0 prefix, spaces and dashes; {@link #normalise} does the strict check. */
    public static final String INDIAN_MOBILE_REGEX = "^\\s*(\\+?91[\\-\\s]?|0)?[6-9][\\d\\s\\-]{9,13}\\s*$";
    private static final Pattern DIGITS = Pattern.compile("\\D");

    private PhoneNumbers() {}

    /** @return 10-digit number or null when not a valid Indian mobile. */
    public static String normalise(String raw) {
        if (raw == null) return null;
        String digits = DIGITS.matcher(raw.trim()).replaceAll("");
        if (digits.startsWith("91") && digits.length() == 12) digits = digits.substring(2);
        if (digits.startsWith("0") && digits.length() == 11) digits = digits.substring(1);
        if (digits.length() != 10 || digits.charAt(0) < '6') return null;
        return digits;
    }

    public static boolean isValid(String raw) {
        return normalise(raw) != null;
    }

    public static String toE164(String tenDigits) {
        return "91" + tenDigits;
    }

    public static String display(String tenDigits) {
        if (tenDigits == null || tenDigits.length() != 10) return tenDigits;
        return "+91 " + tenDigits.substring(0, 5) + " " + tenDigits.substring(5);
    }
}

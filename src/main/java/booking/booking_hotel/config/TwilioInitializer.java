package booking.booking_hotel.config;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TwilioInitializer {

    private final TwilioConfig twilioConfig;

    @PostConstruct
    public void init() {
        if (isBlank(twilioConfig.getAccountSid()) || isBlank(twilioConfig.getAuthToken())) {
            log.warn("Twilio credentials are not configured. SMS sending will fail until twilio.account-sid and twilio.auth-token are set.");
            return;
        }
        Twilio.init(twilioConfig.getAccountSid(), twilioConfig.getAuthToken());
        log.info("Twilio initialized");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
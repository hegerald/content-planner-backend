package at.contentplanner.backend.common;

import java.time.Clock;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

/**
 * Stellt die Uhr als injizierbare Abhängigkeit bereit, damit zeitabhängige
 * Regeln ("Termin muss in der Zukunft liegen") deterministisch testbar sind.
 */
@ApplicationScoped
public class ClockProducer {

    @Produces
    @Singleton
    Clock clock() {
        return Clock.systemUTC();
    }
}

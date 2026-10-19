package at.contentplanner.backend.post;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import at.contentplanner.backend.channel.Channel;
import at.contentplanner.backend.common.BusinessRuleException;

/**
 * Schnelle Unit-Tests der Fachregeln – ohne Quarkus, ohne Datenbank, mit fester Uhr.
 */
class PostRulesTest {

    private static final Instant NOW = Instant.parse("2026-10-20T10:00:00Z");
    private final PostRules rules = new PostRules(Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void textWithinChannelLimitIsAccepted() {
        rules.checkContent("a".repeat(280), Channel.X);
    }

    @Test
    void textOverChannelLimitIsRejected() {
        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> rules.checkContent("a".repeat(281), Channel.X));
        assertEquals("TEXT_TOO_LONG", e.code());
    }

    @Test
    void draftCanBeScheduledWithFutureDate() {
        rules.checkTransition(PostStatus.DRAFT, PostStatus.SCHEDULED, NOW.plusSeconds(60));
    }

    @Test
    void schedulingRequiresDate() {
        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> rules.checkTransition(PostStatus.DRAFT, PostStatus.SCHEDULED, null));
        assertEquals("SCHEDULED_AT_REQUIRED", e.code());
    }

    @Test
    void schedulingInThePastIsRejected() {
        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> rules.checkTransition(PostStatus.DRAFT, PostStatus.SCHEDULED, NOW));
        assertEquals("SCHEDULED_AT_IN_PAST", e.code());
    }

    @Test
    void draftCannotBePublishedDirectly() {
        BusinessRuleException e = assertThrows(BusinessRuleException.class,
                () -> rules.checkTransition(PostStatus.DRAFT, PostStatus.PUBLISHED, NOW.plusSeconds(60)));
        assertEquals("INVALID_STATUS_TRANSITION", e.code());
    }

    @Test
    void publishedIsFinal() {
        assertEquals(List.of(), rules.allowedTransitions(PostStatus.PUBLISHED));
        assertFalse(rules.isEditable(PostStatus.PUBLISHED));
        assertFalse(rules.isDeletable(PostStatus.PUBLISHED));
        assertThrows(BusinessRuleException.class, () -> rules.checkUpdate(PostStatus.PUBLISHED, null));
        assertThrows(BusinessRuleException.class, () -> rules.checkDelete(PostStatus.PUBLISHED));
    }

    @Test
    void scheduledPostMustKeepFutureDateWhenEdited() {
        assertThrows(BusinessRuleException.class, () -> rules.checkUpdate(PostStatus.SCHEDULED, null));
        rules.checkUpdate(PostStatus.SCHEDULED, NOW.plusSeconds(60));
    }

    @Test
    void draftIsEditableAndDeletable() {
        assertTrue(rules.isEditable(PostStatus.DRAFT));
        assertTrue(rules.isDeletable(PostStatus.DRAFT));
        rules.checkUpdate(PostStatus.DRAFT, null);
    }
}

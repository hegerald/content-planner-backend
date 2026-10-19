package at.contentplanner.backend.post;

import java.time.Duration;
import java.time.Instant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;

import at.contentplanner.backend.channel.Channel;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;

/**
 * Legt im Dev-Modus ein paar Beispiel-Posts an, falls die Datenbank leer ist.
 * Da H2 im File-Modus läuft, bleiben die Daten zwischen Neustarts erhalten.
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class DemoDataSeeder {

    @Transactional
    void onStart(@Observes StartupEvent event, PostRules rules) {
        if (Post.count() > 0) {
            return;
        }
        Instant now = rules.now();
        persist("Herbst-Kampagne Teaser", Channel.INSTAGRAM, PostStatus.SCHEDULED,
                now.plus(Duration.ofDays(3)),
                "Die Blätter fallen, unsere Preise auch 🍂 Ab nächster Woche gibt's die Herbst-Aktion. #herbst #aktion");
        persist("Stellenausschreibung Junior Dev", Channel.LINKEDIN, PostStatus.DRAFT, null,
                "Wir suchen Verstärkung! Du hast gerade die HTL abgeschlossen und willst mit modernen Tools durchstarten? Melde dich bei uns.");
        persist("Kurz-Update Webinar", Channel.X, PostStatus.DRAFT,
                now.plus(Duration.ofDays(7)),
                "Nächste Woche: Live-Webinar zu KI im Social-Media-Marketing. Link folgt!");
        persist("Rückblick Sommerfest", Channel.FACEBOOK, PostStatus.PUBLISHED,
                now.minus(Duration.ofDays(30)),
                "Danke an alle, die beim Sommerfest dabei waren – die Fotos sind jetzt online!");
        Log.info("Demo-Daten angelegt (4 Posts)");
    }

    private static void persist(String title, Channel channel, PostStatus status, Instant scheduledAt, String text) {
        Post post = new Post();
        post.title = title;
        post.text = text;
        post.channel = channel;
        post.status = status;
        post.scheduledAt = scheduledAt;
        post.createdAt = Instant.now();
        post.updatedAt = post.createdAt;
        if (status == PostStatus.PUBLISHED) {
            post.publishedAt = scheduledAt;
        }
        post.persist();
    }
}

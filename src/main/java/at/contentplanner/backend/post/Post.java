package at.contentplanner.backend.post;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import at.contentplanner.backend.channel.Channel;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

/**
 * Persistente Repräsentation eines geplanten Social-Media-Posts.
 * Panache-Stil: öffentliche Felder, Hibernate erzeugt die Zugriffe.
 */
@Entity
@Table(name = "post")
public class Post extends PanacheEntityBase {

    @Id
    @GeneratedValue
    public Long id;

    @Column(nullable = false, length = 100)
    public String title;

    @Column(nullable = false, length = 65_000)
    public String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public PostStatus status;

    @Column(name = "scheduled_at")
    public Instant scheduledAt;

    @Column(name = "published_at")
    public Instant publishedAt;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;
}

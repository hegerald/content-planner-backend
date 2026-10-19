package at.contentplanner.backend.post;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import at.contentplanner.backend.channel.Channel;
import at.contentplanner.backend.common.NotFoundException;
import io.quarkus.panache.common.Sort;

/**
 * Anwendungslogik: lädt und speichert Posts und wendet dabei {@link PostRules} an.
 */
@ApplicationScoped
public class PostService {

    private final PostRules rules;

    public PostService(PostRules rules) {
        this.rules = rules;
    }

    public List<PostResponse> list(PostStatus status, Channel channel) {
        Sort sort = Sort.by("scheduledAt", Sort.Direction.Ascending, Sort.NullPrecedence.NULLS_LAST)
                .and("id");
        List<Post> posts;
        if (status != null && channel != null) {
            posts = Post.list("status = ?1 and channel = ?2", sort, status, channel);
        } else if (status != null) {
            posts = Post.list("status", sort, status);
        } else if (channel != null) {
            posts = Post.list("channel", sort, channel);
        } else {
            posts = Post.listAll(sort);
        }
        return posts.stream().map(p -> PostResponse.from(p, rules)).toList();
    }

    public PostResponse get(long id) {
        return PostResponse.from(find(id), rules);
    }

    @Transactional
    public PostResponse create(PostRequest request) {
        rules.checkContent(request.text(), request.channel());

        Post post = new Post();
        post.title = request.title().strip();
        post.text = request.text();
        post.channel = request.channel();
        post.scheduledAt = request.scheduledAt();
        post.status = PostStatus.DRAFT;
        post.createdAt = rules.now();
        post.updatedAt = post.createdAt;
        post.persist();
        return PostResponse.from(post, rules);
    }

    @Transactional
    public PostResponse update(long id, PostRequest request) {
        Post post = find(id);
        rules.checkUpdate(post.status, request.scheduledAt());
        rules.checkContent(request.text(), request.channel());

        post.title = request.title().strip();
        post.text = request.text();
        post.channel = request.channel();
        post.scheduledAt = request.scheduledAt();
        post.updatedAt = rules.now();
        return PostResponse.from(post, rules);
    }

    @Transactional
    public PostResponse changeStatus(long id, PostStatus targetStatus) {
        Post post = find(id);
        rules.checkTransition(post.status, targetStatus, post.scheduledAt);

        post.status = targetStatus;
        post.updatedAt = rules.now();
        if (targetStatus == PostStatus.PUBLISHED) {
            post.publishedAt = post.updatedAt;
        }
        return PostResponse.from(post, rules);
    }

    @Transactional
    public void delete(long id) {
        Post post = find(id);
        rules.checkDelete(post.status);
        post.delete();
    }

    private Post find(long id) {
        Post post = Post.findById(id);
        if (post == null) {
            throw new NotFoundException("Post %d existiert nicht.".formatted(id));
        }
        return post;
    }
}

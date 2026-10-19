package at.contentplanner.backend.channel;

/**
 * Social-Media-Kanal, auf dem ein Post veröffentlicht wird.
 * Das Zeichenlimit ist Business-Wissen und lebt deshalb ausschließlich im Backend.
 */
public enum Channel {

    INSTAGRAM("Instagram", 2_200),
    LINKEDIN("LinkedIn", 3_000),
    X("X", 280),
    FACEBOOK("Facebook", 63_206);

    private final String displayName;
    private final int maxLength;

    Channel(String displayName, int maxLength) {
        this.displayName = displayName;
        this.maxLength = maxLength;
    }

    public String displayName() {
        return displayName;
    }

    public int maxLength() {
        return maxLength;
    }
}

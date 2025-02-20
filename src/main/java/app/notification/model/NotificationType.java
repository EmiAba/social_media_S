package app.notification.model;

public enum NotificationType {
    MESSAGE("💬"),
    LIKE("❤️"),
    FOLLOW("👤"),
    COMMENT("💭"),
    USER_ONLINE("🟢"),
    USER_OFFLINE("🔴");

    private final String icon;

    NotificationType(String icon) {
        this.icon = icon;
    }

    public String getIcon() {
        return icon;
    }

    public String formatMessage(String username) {
        return switch (this) {
            case MESSAGE -> username + " sent you a new message";
            case LIKE -> username + " liked your post";
            case FOLLOW -> username + " started following you";
            case COMMENT -> username + " commented on your post";
            case USER_ONLINE -> username + " is now online";
            case USER_OFFLINE -> username + " went offline";
        };
    }
}
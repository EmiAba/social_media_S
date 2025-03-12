package app.notification.model;

public enum NotificationType {
    MESSAGE("💬"),
    LIKE("❤️"),
    FOLLOW("👤"),
    COMMENT("💭");




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


        };
    }
}
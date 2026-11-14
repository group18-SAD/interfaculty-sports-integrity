package sportsintegrity;

public final class CurrentUser {
    private static String username;
    private static String role;

    private CurrentUser() {}

    public static void set(String user, String userRole) {
        username = user;
        role = userRole;
    }

    public static void clear() {
        username = null;
        role = null;
    }

    public static String username() {
        return username == null ? "unknown" : username;
    }

    public static String role() {
        return role == null ? "" : role;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
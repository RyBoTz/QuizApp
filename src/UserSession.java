public class UserSession {
    private static String username;

    // Method to set the username
    public static void setUsername(String username) {
        UserSession.username = username;
    }

    // Method to get the username
    public static String getUsername() {
        return username;
    }
}

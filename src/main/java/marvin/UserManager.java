package marvin;

public class UserManager {
    private String authorizedUser;
    private String adminPassword;

    public UserManager(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getAuthorizedUser() {
        return authorizedUser;
    }

    public void setAuthorizedUser(String authorizedUser) {
        this.authorizedUser = authorizedUser;
    }

    public boolean isAuthorized(String nick) {
        return nick != null && nick.equals(authorizedUser);
    }

    public boolean authenticate(String user, String password) {
        if (password != null && password.equals(adminPassword)) {
            authorizedUser = user;
            return true;
        }
        return false;
    }
}

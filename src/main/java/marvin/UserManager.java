package marvin;

import marvin.config.AdminPassword;

import javax.inject.Inject;

public class UserManager {
    private String authorizedUser;
    private String adminPassword;

    @Inject
    public UserManager(@AdminPassword String adminPassword) {
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

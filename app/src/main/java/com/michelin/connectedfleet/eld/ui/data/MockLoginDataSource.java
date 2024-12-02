package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.model.LoggedInUser;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class MockLoginDataSource implements LoginDataSource {

    public Result<LoggedInUser> login(String username, String password) {

        try {
            if (
                    username.equalsIgnoreCase("user@example.com")
                            && password.equals("aSamplePassword")
            ) {
                // TODO: handle loggedInUser authentication
                LoggedInUser fakeUser =
                        new LoggedInUser(
                                java.util.UUID.randomUUID().toString(),
                                "Jane Doe");
                return new Result.Success<>(fakeUser);
            } else {
                return new Result.Error(new IOException("Incorrect username or password"));
            }
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void logout() {
        // TODO: revoke authentication
    }
}

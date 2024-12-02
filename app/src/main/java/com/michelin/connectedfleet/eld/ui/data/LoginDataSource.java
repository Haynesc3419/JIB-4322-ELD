package com.michelin.connectedfleet.eld.ui.data;

import com.michelin.connectedfleet.eld.ui.data.model.LoggedInUser;

public interface LoginDataSource {
    Result<LoggedInUser> login(String username, String password);
    void logout();
}

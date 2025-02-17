package com.michelin.connectedfleet.eld.ui.ui.login;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.ui.data.LoginRepository;
import com.michelin.connectedfleet.eld.ui.data.Result;
import com.michelin.connectedfleet.eld.ui.data.model.LoggedInUser;

public class LoginViewModel extends AndroidViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(Application application, LoginRepository loginRepository) {
        super(application);
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Result<LoggedInUser> result = loginRepository.login(username, password);

        if (result instanceof Result.Success) {
            LoggedInUser data = ((Result.Success<LoggedInUser>) result).getData();
            SharedPreferences prefs = getApplication().getSharedPreferences("tokens", Context.MODE_PRIVATE);
            prefs.edit().putString("token", data.getToken()).apply();
            loginResult.setValue(new LoginResult(new LoggedInUserView(data.getUsername())));
        } else {
            loginResult.setValue(new LoginResult(R.string.login_failed));
        }
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private static boolean isUserNameValid(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(username).matches();
    }

    // A placeholder password validation check
    private static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
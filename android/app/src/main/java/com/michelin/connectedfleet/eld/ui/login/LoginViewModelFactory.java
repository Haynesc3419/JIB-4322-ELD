package com.michelin.connectedfleet.eld.ui.login;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import android.app.Application;
import android.content.Context;
import com.michelin.connectedfleet.eld.ui.data.WebLoginDataSource;
import com.michelin.connectedfleet.eld.ui.data.LoginRepository;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {
    private Application application;
    private Context context;
    public LoginViewModelFactory(Application application) {
        this.application = application;
        this.context = application.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(application, LoginRepository.getInstance(new WebLoginDataSource(this.context)
            ));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
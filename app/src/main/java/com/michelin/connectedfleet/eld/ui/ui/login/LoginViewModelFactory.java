package com.michelin.connectedfleet.eld.ui.ui.login;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;

import com.michelin.connectedfleet.eld.ui.data.MockLoginDataSource;
import com.michelin.connectedfleet.eld.ui.data.LoginRepository;
import com.michelin.connectedfleet.eld.ui.data.MongoLoginDataSource;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(LoginRepository.getInstance(new MockLoginDataSource()
            ));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
package com.michelin.connectedfleet.eld.ui.ui.login;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.NonNull;
import android.content.Context;
import com.michelin.connectedfleet.eld.ui.data.WebLoginDataSource;
import com.michelin.connectedfleet.eld.ui.data.LoginRepository;
import com.michelin.connectedfleet.eld.ui.data.MongoLoginDataSource;

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
public class LoginViewModelFactory implements ViewModelProvider.Factory {
    private Context context;
    public LoginViewModelFactory(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            return (T) new LoginViewModel(LoginRepository.getInstance(new WebLoginDataSource(this.context)
            ));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
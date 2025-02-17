package com.michelin.connectedfleet.eld.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentProfileBinding;
import com.michelin.connectedfleet.eld.ui.data.UserService;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetUserInfoResponse;

import java.time.LocalDateTime;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Fragment that demonstrates:
 *  - Toggling color-blind mode in SharedPreferences
 *  - Retrieving user info via a Retrofit call
 *  - Opening a webpage in a Custom Tab
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserService userService;
    private boolean isColorBlindMode;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        // Inflate the layout using View Binding
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Grab references to the TextViews
        TextView usernameView = binding.profileUsername;
        TextView nameView = binding.profileName;

        // 1) Set up a button to open the FMCSA webpage in a Custom Tab
        binding.regulationsButton.setOnClickListener(v -> {
            String url = "https://www.fmcsa.dot.gov/hours-service/elds/electronic-logging-devices";
            CustomTabsIntent intent = new CustomTabsIntent.Builder()
                    .build();
            intent.launchUrl(requireContext(), Uri.parse(url));
        });

        // 2) Toggling color-blind mode
        //    - We'll read the setting from "settings" SharedPreferences (same as HomeFragment).
        SharedPreferences settingsPrefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        isColorBlindMode = settingsPrefs.getBoolean("colorBlindMode", false);

        // Update the button text based on the current mode
        updateButtonText();

        // When user taps this button, flip colorBlindMode and save it
        binding.colorBlindToggleButton.setOnClickListener(view -> {
            isColorBlindMode = !isColorBlindMode; // Flip the boolean
            settingsPrefs.edit()
                    .putBoolean("colorBlindMode", isColorBlindMode)
                    .apply();
            updateButtonText();
        });

        // 3) Set up Retrofit to fetch user information
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                LocalDateTime.parse(json.getAsString())
        );

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/users/")  // Adjust as needed for your environment
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build();
        userService = retrofit.create(UserService.class);

        // 4) Retrieve the JSESSIONID token from "tokens" SharedPreferences
        SharedPreferences tokenPrefs = requireContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = tokenPrefs.getString("token", null);
        String cookieHeader = String.format("JSESSIONID=%s", token);

        // 5) Make the API call to fetch user info
        Call<GetUserInfoResponse> userInfoRequest = userService.getInfo(cookieHeader);
        userInfoRequest.enqueue(new Callback<GetUserInfoResponse>() {
            @Override
            public void onResponse(Call<GetUserInfoResponse> call, Response<GetUserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    usernameView.setText(response.body().username());
                    nameView.setText(response.body().firstName() + " " + response.body().lastName());
                } else {
                    Log.d("ProfileFragment", "Failed to retrieve user data (null or unsuccessful).");
                }
            }

            @Override
            public void onFailure(Call<GetUserInfoResponse> call, Throwable throwable) {
                Log.d("ProfileFragment", "API call failed: " + throwable.getMessage());
            }
        });

        return root;
    }

    private void updateButtonText() {
        if (isColorBlindMode) {
            binding.colorBlindToggleButton.setText("Color Blind Mode");
        } else {
            binding.colorBlindToggleButton.setText("Regular Mode");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

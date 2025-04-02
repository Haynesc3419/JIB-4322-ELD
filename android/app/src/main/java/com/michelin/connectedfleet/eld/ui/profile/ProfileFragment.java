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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import java.time.LocalDateTime;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.michelin.connectedfleet.eld.MainActivity;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentProfileBinding;
import com.michelin.connectedfleet.eld.ui.data.UserService;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetUserInfoResponse;
import com.michelin.connectedfleet.eld.ui.data.util.UnitSettings;

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private UserService userService;
    private UnitSettings unitSettings;
    private double distanceKm = 60491.5; // TODO: Link to driver information

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize basic UI elements that don't depend on login state
        initializeBasicUI();

        // Set up API client for user data
        setupApiClient();

        // Get token and make API call if logged in
        SharedPreferences prefs = requireContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        
        if (token != null) {
            // User is logged in, fetch their data
            fetchUserData(token);
        } else {
            // User is not logged in, show default state
            Log.d(TAG, "User is not logged in");
            binding.profileName.setText(R.string.default_profile_name);
            binding.profileUsername.setText(R.string.default_profile_email);
        }

        return root;
    }

    private void initializeBasicUI() {
        // Set up timezone display
        TimeZone timezone = TimeZone.getDefault();
        SimpleDateFormat sdf = new SimpleDateFormat("zzz XXX");
        sdf.setTimeZone(timezone);
        if (binding.timeZone != null) {
            binding.timeZone.setText(getString(R.string.current_timezone_format, sdf.format(new Date())));
        }

        // Set up regulations button
        binding.regulationsButton.setOnClickListener(v -> {
            String url = "https://www.fmcsa.dot.gov/regulations/hours-service/summary-hours-service-regulations";
            CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
            intent.launchUrl(requireContext(), Uri.parse(url));
        });

        // Initialize UnitSettings and distance display if available
        try {
            MainActivity activity = (MainActivity) requireActivity();
            unitSettings = activity.getUnitSettings();
            if (unitSettings != null) {
                // Set initial switch state
                binding.switchMetricUnits.setChecked(unitSettings.isMetric());
                
                // Set up switch listener
                binding.switchMetricUnits.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    unitSettings.setUseMetric(isChecked);
                    updateDistanceDisplay();
                });

                // Update initial display
                updateDistanceDisplay();

                // Observe unit changes from other parts of the app
                unitSettings.getUseMetric().observe(getViewLifecycleOwner(), isMetric -> {
                    if (binding.switchMetricUnits.isChecked() != isMetric) {
                        binding.switchMetricUnits.setChecked(isMetric);
                    }
                    updateDistanceDisplay();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UnitSettings", e);
            // Show distance in default format if UnitSettings is not available
            if (binding.distanceDriven != null) {
                binding.distanceDriven.setText(getString(R.string.distance_driven_format, distanceKm));
            }
        }
    }

    private void updateDistanceDisplay() {
        if (binding.distanceDriven != null && unitSettings != null) {
            binding.distanceDriven.setText(unitSettings.formatDistance(distanceKm));
        }
    }

    private void setupApiClient() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, 
            (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString()));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/users/")
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build();

        userService = retrofit.create(UserService.class);
    }

    private void fetchUserData(String token) {
        String cookieHeader = String.format("JSESSIONID=%s", token);
        Call<GetUserInfoResponse> userInfoRequest = userService.getInfo(cookieHeader);
        userInfoRequest.enqueue(new Callback<GetUserInfoResponse>() {
            @Override
            public void onResponse(Call<GetUserInfoResponse> call, Response<GetUserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null && binding != null) {
                    GetUserInfoResponse userData = response.body();
                    binding.profileUsername.setText(userData.username());
                    binding.profileName.setText(String.format("%s %s", userData.firstName(), userData.lastName()));
                } else {
                    Log.d(TAG, "Failed to retrieve user data");
                }
            }

            @Override
            public void onFailure(Call<GetUserInfoResponse> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

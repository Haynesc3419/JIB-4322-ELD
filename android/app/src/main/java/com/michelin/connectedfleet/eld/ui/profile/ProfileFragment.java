package com.michelin.connectedfleet.eld.ui.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.bumptech.glide.Glide;

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final int PICK_IMAGE_REQUEST = 1;
    private FragmentProfileBinding binding;
    private UserService userService;
    private UnitSettings unitSettings;
    private double distanceKm = 60491.5; // TODO: Link to driver information

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Set up profile image click listener
        binding.profileImage.setOnClickListener(v -> selectImage());

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
                // Update initial display
                updateDistanceDisplay();

                // Observe unit changes from settings
                unitSettings.getUseMetric().observe(getViewLifecycleOwner(), isMetric -> {
                    updateDistanceDisplay();
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing UnitSettings", e);
            // Show distance in default format if UnitSettings is not available
            if (binding.distanceDriven != null) {
                String formattedDistance = String.format("%.2f km", distanceKm);
                binding.distanceDriven.setText(formattedDistance);
            }
        }
    }

    private void updateDistanceDisplay() {
        if (binding.distanceDriven != null && unitSettings != null) {
            // Convert the stored imperial distance to the user's preferred unit
            double distanceInMiles = distanceKm * 0.621371; // Convert km to miles
            binding.distanceDriven.setText(unitSettings.formatDistance(distanceInMiles));
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

    private void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Load the selected image into the ImageView
                Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(binding.profileImage);

                // Here you would typically upload the image to your server
                // uploadImageToServer(imageUri);
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading profile image", e);
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Add this method if you want to implement server upload
    private void uploadImageToServer(Uri imageUri) {
        // TODO: Implement image upload to your server
        // This would typically involve:
        // 1. Compressing the image
        // 2. Converting to Base64 or multipart form data
        // 3. Making an API call to your server
        // 4. Handling success/failure
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

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

import com.michelin.connectedfleet.eld.databinding.FragmentProfileBinding;
import com.michelin.connectedfleet.eld.ui.data.UserService;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetUserInfoResponse;

/**
 * Fragment that demonstrates a responsive layout pattern where the format of the content
 * transforms depending on the size of the screen. Specifically this Fragment shows items in
 * the [RecyclerView] using LinearLayoutManager in a small screen
 * and shows items using GridLayoutManager in a large screen.
 */
public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserService userService;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ProfileViewModel profileViewModel =
                new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        TextView usernameView = binding.profileUsername;
        TextView nameView = binding.profileName;

        // Set up the button to open a webpage
        binding.regulationsButton.setOnClickListener(v -> {
            String url = "https://www.fmcsa.dot.gov/hours-service/elds/electronic-logging-devices";
            CustomTabsIntent intent = new CustomTabsIntent.Builder()
                    .build();
            intent.launchUrl(requireContext(), Uri.parse(url));  // Use requireContext() for non-null context
        });

        // Set units and distance driven
        binding.distanceDriven.setText("Miles Driven: 60491.5");
        binding.switchMetricUnits.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.distanceDriven.setText("Kilometers Driven: 60491.5");
            } else {
                binding.distanceDriven.setText("Miles Driven: 37587.68");
            }
        });

        // Set up Retrofit and Gson for API call
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
            return LocalDateTime.parse(json.getAsString());
        });

        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/users/")  // Localhost for Android Emulator
                .addConverterFactory(gsonConverterFactory)
                .build();

        userService = retrofit.create(UserService.class);

        // Get token from SharedPreferences and make API call
        SharedPreferences prefs = getContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String cookieHeader = String.format("JSESSIONID=%s", token);

        // Make the API call to fetch user information
        Call<GetUserInfoResponse> userInfoRequest = userService.getInfo(cookieHeader);
        userInfoRequest.enqueue(new Callback<GetUserInfoResponse>() {
            @Override
            public void onResponse(Call<GetUserInfoResponse> call, Response<GetUserInfoResponse> response) {
                if (response.body() != null) {
                    usernameView.setText(response.body().username());
                    nameView.setText(response.body().firstName() + " " + response.body().lastName());
                } else {
                    Log.d("ProfileFragment", "Failed to retrieve user data.");
                }
            }

            @Override
            public void onFailure(Call<GetUserInfoResponse> call, Throwable throwable) {
                Log.d("ProfileFragment", "API call failed: " + throwable.getMessage());
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

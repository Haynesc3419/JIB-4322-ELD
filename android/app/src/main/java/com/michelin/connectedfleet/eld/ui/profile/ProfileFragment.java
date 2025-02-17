package com.michelin.connectedfleet.eld.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

        String username = "foo@email.com";
        String full_name = "bar foo";

        TextView usernameView = binding.profileUsername;
        TextView nameView = binding.profileName;
        binding.regulationsButton.setOnClickListener(v -> {

            // TODO add click activity
            return;
        });


        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
            return LocalDateTime.parse(json.getAsString());
        });

        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/users/")
                .addConverterFactory(gsonConverterFactory)
                .build();

        userService = retrofit.create(UserService.class);

        SharedPreferences prefs = getContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String cookieHeader = String.format("JSESSIONID=%s", token);
        Call<GetUserInfoResponse> userInfoRequest = userService.getInfo(cookieHeader);
        userInfoRequest.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GetUserInfoResponse> call, Response<GetUserInfoResponse> response) {
                usernameView.setText(response.body().username());
                nameView.setText(response.body().firstName() + " " + response.body().lastName());
            }

            @Override
            public void onFailure(Call<GetUserInfoResponse> call, Throwable throwable) {
                Log.d("uh oh", "gee willikers");
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
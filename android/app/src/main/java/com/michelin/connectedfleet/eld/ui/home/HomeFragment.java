package com.michelin.connectedfleet.eld.ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.michelin.connectedfleet.eld.DriverStatus;
import com.michelin.connectedfleet.eld.MainActivity;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentHomeBinding;
import com.michelin.connectedfleet.eld.databinding.ItemLogsBinding;
import com.michelin.connectedfleet.eld.ui.data.LogEntry;
import com.michelin.connectedfleet.eld.ui.data.LogEntryService;
import com.michelin.connectedfleet.eld.ui.data.LoggedDay;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.CreateLogEntryRequest;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.CreateLogEntryResponse;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetLogEntryResponseItem;
import com.michelin.connectedfleet.eld.ui.data.util.TimerManager;
import com.michelin.connectedfleet.eld.ui.status.StatusViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private LogEntryService logsService;

    public HomeFragment() {
        super();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> {
            return LocalDateTime.parse(json.getAsString());
        });

        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/logs/")
                .addConverterFactory(gsonConverterFactory)
                .build();

        logsService = retrofit.create(LogEntryService.class);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TimerManager timer = TimerManager.getInstance();
        timer.startTimer("break", 1 * 60 * 60 * 1000);
        timer.pauseTimer("break");
        timer.startTimer("driving", 1 * 60 * 60 * 1000);
        timer.pauseTimer("driving");
        timer.startTimer("dayReset", 5 * 60 * 60 * 1000);

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.timer = timer;

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        SharedPreferences prefs = getContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String cookieHeader = String.format("JSESSIONID=%s", token);
        Call<List<GetLogEntryResponseItem>> logEntriesRequest = logsService.getLogEntries(cookieHeader);


        homeViewModel.hoursRemaining.breakHoursRemaining.observe(
                getViewLifecycleOwner(),
                homeViewModel.hoursRemaining.createObserver(binding.progressHoursRemainingBreak, binding.textHoursRemainingBreak)
        );
        homeViewModel.hoursRemaining.drivingHoursRemaining.observe(
                getViewLifecycleOwner(),
                homeViewModel.hoursRemaining.createObserver(binding.progressHoursRemainingDriving, binding.textHoursRemainingDriving)
        );
        homeViewModel.hoursRemaining.dayResetHoursRemaining.observe(
                getViewLifecycleOwner(),
                homeViewModel.hoursRemaining.createObserver(binding.progressHoursRemainingDayReset, binding.textHoursRemainingDayReset)
        );

        TextView currentStatus = binding.textviewHomeCurrentStatus;
        StatusViewModel.getStatus().observe(getViewLifecycleOwner(), driverStatus -> {
            currentStatus.setText(getResources().getTextArray(R.array.statuses)[driverStatus.ordinal()]);
            if (currentStatus.getText().equals(getResources().getTextArray(R.array.statuses)[0])) {
                timer.resumeTimer("driving");
                timer.pauseTimer("break");
            } else if (currentStatus.getText().equals(getResources().getTextArray(R.array.statuses)[1])) {
                timer.resumeTimer("break");
                timer.pauseTimer("driving");
            } else {
                timer.pauseTimer("break");
                timer.pauseTimer("driving");
            };
        });

        Button changeStatusButton = binding.homeButtonChangeStatus;
        changeStatusButton.setOnClickListener(v -> {
            MainActivity.bottomNavigationView.setSelectedItemId(R.id.nav_status);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Change Status");
            builder.setItems(R.array.statuses, (dialog, which) -> {
                dialog.dismiss();
                DriverStatus status = DriverStatus.values()[which];
                StatusViewModel.setStatus(status);

                SharedPreferences prefs2 = getContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
                String token2 = prefs.getString("token", null);
                String cookieHeader2 = String.format("JSESSIONID=%s", token);

                logsService.insert(new CreateLogEntryRequest(status.toString(), cookieHeader2)).enqueue(new Callback<CreateLogEntryResponse>() {
                    @Override
                    public void onResponse(Call<CreateLogEntryResponse> call, Response<CreateLogEntryResponse> response) {
                        return;
                    }

                    @Override
                    public void onFailure(Call<CreateLogEntryResponse> call, Throwable throwable) {
                        return;
                    }
                });
            });
            builder.show();
            // MainActivity.bottomNavigationView.setSelectedItemId(R.id.nav_status);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

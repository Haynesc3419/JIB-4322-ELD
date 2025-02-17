package com.michelin.connectedfleet.eld.ui.home;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.michelin.connectedfleet.eld.DriverStatus;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentHomeBinding;
import com.michelin.connectedfleet.eld.databinding.ItemLogsBinding;
import com.michelin.connectedfleet.eld.ui.data.LogEntryService;
import com.michelin.connectedfleet.eld.ui.data.LoggedDay;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetLogEntryResponseItem;
import com.michelin.connectedfleet.eld.ui.status.StatusViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private boolean isColorBlindMode;
    private SharedPreferences.OnSharedPreferenceChangeListener prefsListener;
    private LogEntryService logsService;

    public HomeFragment() {
        super();

        // Set up Retrofit & GSON to parse LocalDateTime if needed
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(
                LocalDateTime.class,
                (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) -> LocalDateTime.parse(json.getAsString())
        );

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/logs/")
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
                .build();

        logsService = retrofit.create(LogEntryService.class);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // 1) Read colorBlindMode from SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
        isColorBlindMode = prefs.getBoolean("colorBlindMode", false);

        // 2) Set up a listener so we know when colorBlindMode changes
        prefsListener = (sharedPrefs, key) -> {
            if ("colorBlindMode".equals(key)) {
                isColorBlindMode = sharedPrefs.getBoolean(key, false);
                applyColorBlindMode();
                Log.d("HomeFragment", "colorBlindMode changed -> " + isColorBlindMode);
            }
        };
        prefs.registerOnSharedPreferenceChangeListener(prefsListener);

        // Set up RecyclerView and adapter
        RecyclerView recyclerView = binding.recyclerviewLogs;
        ListAdapter<LoggedDay, LogsViewHolder> adapter =
                new LogsAdapter(getResources().getConfiguration().getLocales().get(0), null);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Retrieve token, fetch logs from server if needed
        SharedPreferences pref = getContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = pref.getString("token", null);
        String cookieHeader = String.format("JSESSIONID=%s", token);

        Call<List<GetLogEntryResponseItem>> logEntriesRequest = logsService.getLogEntries(cookieHeader);
        logEntriesRequest.enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    Call<List<GetLogEntryResponseItem>> call,
                    Response<List<GetLogEntryResponseItem>> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e("HomeFragment", "Failed to retrieve log entries: null or unsuccessful response.");
                    return;
                }

                // Sort logs by date
                Map<LocalDate, LoggedDay> logsByDate = new HashMap<>();
                for (GetLogEntryResponseItem item : response.body()) {
                    LocalDate date = item.dateTime().toLocalDate();
                    LoggedDay day = logsByDate.getOrDefault(
                            date,
                            new LoggedDay(date, new ArrayList<>(), Locale.getDefault())
                    );
                    logsByDate.put(date, day);
                    day.logEntries().add(item);
                }

                List<LoggedDay> sorted = new ArrayList<>(logsByDate.values());
                sorted.sort(Comparator.reverseOrder());
                adapter.submitList(sorted);
            }

            @Override
            public void onFailure(Call<List<GetLogEntryResponseItem>> call, Throwable t) {
                Log.d("HomeFragment", "Could not fetch log entries: " + t.getMessage());
            }
        });

        // Setup Observers for the circular progress bars
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

        // Display the driver's current status
        TextView currentStatus = binding.textviewHomeCurrentStatus;
        StatusViewModel.getStatus().observe(getViewLifecycleOwner(), driverStatus -> {
            currentStatus.setText(getResources().getTextArray(R.array.statuses)[driverStatus.ordinal()]);
        });

        // Button to change status
        Button changeStatusButton = binding.homeButtonChangeStatus;
        changeStatusButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Change Status");
            builder.setItems(R.array.statuses, (dialog, which) -> {
                dialog.dismiss();
                StatusViewModel.setStatus(DriverStatus.values()[which]);
            });
            builder.show();
        });

        // Make sure the circles are colored properly on first load
        applyColorBlindMode();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Unregister the SharedPreferences listener
        if (prefsListener != null) {
            requireContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
                    .unregisterOnSharedPreferenceChangeListener(prefsListener);
        }
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        // If the user toggled color blind mode on another screen, re-check
        isColorBlindMode = requireContext()
                .getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getBoolean("colorBlindMode", false);

        applyColorBlindMode();
    }

    /**
     * Applies the appropriate colors to the 3 CircularProgressIndicator circles
     * based on whether color-blind mode is on or off.
     */
    private void applyColorBlindMode() {
        if (binding == null) return; // safety check

        CircularProgressIndicator breakIndicator = binding.progressHoursRemainingBreak;
        CircularProgressIndicator drivingIndicator = binding.progressHoursRemainingDriving;
        CircularProgressIndicator dayResetIndicator = binding.progressHoursRemainingDayReset;

        Context context = requireContext();
        if (isColorBlindMode) {
            // Color-blind friendly colors
            breakIndicator.setIndicatorColor(
                    ContextCompat.getColor(context, R.color.color_blind_status_valid)
            );
            drivingIndicator.setIndicatorColor(
                    ContextCompat.getColor(context, R.color.color_blind_status_warning)
            );
            dayResetIndicator.setIndicatorColor(
                    ContextCompat.getColor(context, R.color.color_blind_status_danger)
            );
        } else {
            // Default colors
            breakIndicator.setIndicatorColor(
                    ContextCompat.getColor(context, R.color.status_valid)
            );
            drivingIndicator.setIndicatorColor(
                    ContextCompat.getColor(context, R.color.status_warning)
            );
            dayResetIndicator.setIndicatorColor(
                    ContextCompat.getColor(context, R.color.status_danger)
            );
        }
    }

    // -----------------------
    // RecyclerView Adapter
    // -----------------------
    private static class LogsAdapter extends ListAdapter<LoggedDay, LogsViewHolder> {

        private List<LoggedDay> days;
        private static Locale locale;

        protected LogsAdapter(Locale locale, List<LoggedDay> days) {
            super(new DiffUtil.ItemCallback<LoggedDay>() {
                @Override
                public boolean areItemsTheSame(@NonNull LoggedDay oldItem, @NonNull LoggedDay newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull LoggedDay oldItem, @NonNull LoggedDay newItem) {
                    return oldItem.equals(newItem);
                }
            });
            LogsAdapter.locale = locale;
            if (days != null) {
                this.days = days;
            }
        }

        @Override
        public void submitList(@Nullable List<LoggedDay> list) {
            days = list;
            super.submitList(list);
        }

        @Override
        public int getItemCount() {
            return (days == null) ? 0 : days.size();
        }

        @NonNull
        @Override
        public LogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLogsBinding binding = ItemLogsBinding.inflate(
                    LayoutInflater.from(parent.getContext()),
                    parent,
                    false
            );
            return new LogsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
            LoggedDay item = days.get(position);
            holder.calendarDayView.setText(String.valueOf(item.date().getDayOfMonth()));
            holder.calendarMonthView.setText(item.getMonthAbbreviation());
            holder.detailsView.setText("Drove for " + item.getTimeDriven());
        }
    }

    private static class LogsViewHolder extends RecyclerView.ViewHolder {
        private final TextView calendarDayView;
        private final TextView calendarMonthView;
        private final TextView detailsView;

        public LogsViewHolder(ItemLogsBinding binding) {
            super(binding.getRoot());
            calendarDayView = binding.textViewItemLogsDateNum;
            calendarMonthView = binding.textViewItemLogsDateMonth;
            detailsView = binding.textViewDetails;

            binding.getRoot().setLayoutParams(
                    new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    )
            );
        }
    }
}

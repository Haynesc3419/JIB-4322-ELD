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

        RecyclerView recyclerView = binding.recyclerviewLogs;
        ListAdapter<LoggedDay, LogsViewHolder> adapter = new LogsAdapter(getResources().getConfiguration().getLocales().get(0), null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        SharedPreferences prefs = getContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String cookieHeader = String.format("JSESSIONID=%s", token);
        Call<List<GetLogEntryResponseItem>> logEntriesRequest = logsService.getLogEntries(cookieHeader);

        logEntriesRequest.enqueue(new Callback<>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<GetLogEntryResponseItem>> call, Response<List<GetLogEntryResponseItem>> response) {
                Map<LocalDate, LoggedDay> logsByDate = new HashMap<>();
                for (GetLogEntryResponseItem item : response.body()) {
                    LocalDate date = item.dateTime().toLocalDate();
                    LoggedDay day = logsByDate.getOrDefault(date, new LoggedDay(date, new ArrayList<>(), Locale.getDefault()));
                    logsByDate.put(date, day);
                    day.logEntries().add(item);
                }

                List<LoggedDay> sorted = new ArrayList<>(logsByDate.values());
                sorted.sort(Comparator.reverseOrder());
                adapter.submitList(sorted);
            }

            @Override
            public void onFailure(Call<List<GetLogEntryResponseItem>> call, Throwable throwable) {
                Log.d("uh oh", "oops!!!");
            }
        });
        // homeViewModel.getDates().observe(getViewLifecycleOwner(), adapter::submitList);


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
                StatusViewModel.setStatus(DriverStatus.values()[which]);
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

    private static class LogsAdapter extends androidx.recyclerview.widget.ListAdapter<LoggedDay, LogsViewHolder> {
        private List<LoggedDay> days;
        private static Locale locale;

        private static String convertDate(Date date) {
            String convertedDate = DateFormat.getDateInstance().format(date);

            long timeSince = new Date().getTime() - date.getTime();
            if (TimeUnit.DAYS.convert(timeSince, TimeUnit.MILLISECONDS) < 7) {
                String dayOfWeek = new SimpleDateFormat("EEEE", locale).format(date);
                convertedDate = dayOfWeek + " (" + convertedDate + ")";
            }

            return convertedDate;
        }

        protected LogsAdapter(Locale locale, List<LoggedDay> days) {
            super(new DiffUtil.ItemCallback<>() {
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
            if (days == null) {
                return 0;
            }
            return days.size();
        }

        @NonNull
        @Override
        public LogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLogsBinding binding = ItemLogsBinding.inflate(LayoutInflater.from(parent.getContext()));
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

            binding.getRoot().setLayoutParams(new ConstraintLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            );
        }
    }
}

package com.michelin.connectedfleet.eld.ui.home;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.michelin.connectedfleet.eld.MainActivity;
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentHomeBinding;
import com.michelin.connectedfleet.eld.databinding.ItemLogsBinding;
import com.michelin.connectedfleet.eld.ui.data.LogEntry;
import com.michelin.connectedfleet.eld.ui.data.LogEntryService;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetLogEntryResponseItem;
import com.michelin.connectedfleet.eld.ui.status.StatusViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

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
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerviewLogs;
        ListAdapter<GetLogEntryResponseItem, LogsViewHolder> adapter = new LogsAdapter(getResources().getConfiguration().getLocales().get(0), null);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
        SharedPreferences prefs = getContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String cookieHeader = String.format("JSESSIONID=%s", token);
        Call<List<GetLogEntryResponseItem>> logEntriesRequest = logsService.getLogEntries(cookieHeader);
        logEntriesRequest.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<GetLogEntryResponseItem>> call, Response<List<GetLogEntryResponseItem>> response) {
                adapter.submitList(response.body());
            }

            @Override
            public void onFailure(Call<List<GetLogEntryResponseItem>> call, Throwable throwable) {
                Log.d("uh oh", "shit.");
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
        });

        Button changeStatusButton = binding.homeButtonChangeStatus;
        changeStatusButton.setOnClickListener(v -> {
            MainActivity.bottomNavigationView.setSelectedItemId(R.id.nav_status);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class LogsAdapter extends androidx.recyclerview.widget.ListAdapter<GetLogEntryResponseItem, LogsViewHolder> {
        private List<GetLogEntryResponseItem> entries;
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

        protected LogsAdapter(Locale locale, List<GetLogEntryResponseItem> entries) {
            super(new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull GetLogEntryResponseItem oldItem, @NonNull GetLogEntryResponseItem newItem) {
                    return oldItem.equals(newItem);
                }

                @Override
                public boolean areContentsTheSame(@NonNull GetLogEntryResponseItem oldItem, @NonNull GetLogEntryResponseItem newItem) {
                    return oldItem.equals(newItem);
                }
            });

            LogsAdapter.locale = locale;
            if (entries != null) {
                this.entries = entries;
            }
        }

        @Override
        public void submitList(@Nullable List<GetLogEntryResponseItem> list) {
            entries = list;
            super.submitList(list);
        }

        @Override
        public int getItemCount() {
            if (entries == null) {
                return 0;
            }
            return entries.size();
        }

        @NonNull
        @Override
        public LogsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLogsBinding binding = ItemLogsBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new LogsViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull LogsViewHolder holder, int position) {
            GetLogEntryResponseItem item = entries.get(position);
            String text = String.format("%s (%s)", convertDate(Date.from(item.dateTime().atZone(ZoneId.systemDefault()).toInstant())), item.status());
            holder.textView.setText(text);
        }
    }

    private static class LogsViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;

        public LogsViewHolder(ItemLogsBinding binding) {
            super(binding.getRoot());
            textView = binding.textViewItemLogsDate;
        }
    }
}

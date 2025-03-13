package com.michelin.connectedfleet.eld.ui.logbook;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.michelin.connectedfleet.eld.R;
import com.michelin.connectedfleet.eld.databinding.FragmentLogBookBinding;
import com.michelin.connectedfleet.eld.databinding.ItemLogsBinding;
import com.michelin.connectedfleet.eld.ui.data.LogEntryService;
import com.michelin.connectedfleet.eld.ui.data.LoggedDay;
import com.michelin.connectedfleet.eld.ui.data.retrofitinterface.GetLogEntryResponseItem;
import com.michelin.connectedfleet.eld.ui.data.util.TimerManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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

public class LogBookFragment extends Fragment {

    private FragmentLogBookBinding binding;
    private LogEntryService logsService;

    public LogBookFragment() {
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
        binding = FragmentLogBookBinding.inflate(inflater, container, false);
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

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class LogsAdapter extends ListAdapter<LoggedDay, LogsViewHolder> {
        private List<LoggedDay> days;
        private static Locale locale;

        private static String convertDate(java.util.Date date) {
            String convertedDate = java.text.DateFormat.getDateInstance().format(date);

            long timeSince = new java.util.Date().getTime() - date.getTime();
            if (TimeUnit.DAYS.convert(timeSince, TimeUnit.MILLISECONDS) < 7) {
                String dayOfWeek = new java.text.SimpleDateFormat("EEEE", locale).format(date);
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
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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LogBookFragment extends Fragment {

    private FragmentLogBookBinding binding;
    private LogEntryService logsService;

    public LogBookFragment() {
        super();

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                LocalDateTime.parse(json.getAsString()));

        GsonConverterFactory gsonConverterFactory = GsonConverterFactory.create(gsonBuilder.create());

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/logs/")
                .addConverterFactory(gsonConverterFactory)
                .build();

        logsService = retrofit.create(LogEntryService.class);
    }

    private final OnLogClickListener logClickListener = loggedDay -> {
        LogEntryFragment logEntryFragment = new LogEntryFragment();
        Bundle args = new Bundle();

        args.putString("date", loggedDay.date().toString());
        args.putString("log_entries", loggedDay.logEntries().toString()); // A// djust as needed
        logEntryFragment.setArguments(args);
        logEntryFragment.show(getParentFragmentManager(), "LogEntryFragment");
    };

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLogBookBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerviewLogs;
        LogsAdapter adapter = new LogsAdapter(getResources().getConfiguration().getLocales().get(0), logClickListener);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        SharedPreferences prefs = getContext().getSharedPreferences("tokens", Context.MODE_PRIVATE);
        String token = prefs.getString("token", null);
        String cookieHeader = String.format("JSESSIONID=%s", token);
        Call<List<GetLogEntryResponseItem>> logEntriesRequest = logsService.getLogEntries(cookieHeader);

        logEntriesRequest.enqueue(new Callback<>() {
            @Override
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
}
